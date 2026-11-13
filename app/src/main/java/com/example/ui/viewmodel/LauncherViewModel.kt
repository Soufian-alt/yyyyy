package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppUsageEntity
import com.example.data.db.DesktopItemEntity
import com.example.data.db.FolderAppEntity
import com.example.data.db.LauncherDatabase
import com.example.data.model.ActiveOverlay
import com.example.data.model.AppInfo
import com.example.data.model.AppSortOrder
import com.example.data.model.ControlCenterState
import com.example.data.model.DockItem
import com.example.data.model.HomeItem
import com.example.data.model.IconSizeOption
import com.example.data.model.LauncherSettings
import com.example.data.model.LockClockStyle
import com.example.data.model.MotionLevel
import com.example.data.model.ThemeMode
import com.example.data.model.WallpaperPreset
import com.example.data.model.WidgetType
import com.example.data.prefs.SettingsDataStore
import com.example.data.system.InstalledAppsRepository
import com.example.data.system.SystemControlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = LauncherDatabase.getInstance(application).launcherDao()
    private val settingsStore = SettingsDataStore(application)
    val appsRepo = InstalledAppsRepository(application)
    val systemRepo = SystemControlRepository(application)

    private val rawInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(AppSortOrder.NAME_ASC)
    val sortOrder: StateFlow<AppSortOrder> = _sortOrder.asStateFlow()

    private val _activeOverlay = MutableStateFlow<ActiveOverlay>(ActiveOverlay.None)
    val activeOverlay: StateFlow<ActiveOverlay> = _activeOverlay.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _extraPageCount = MutableStateFlow(2)

    private val _controlCenterState = MutableStateFlow(systemRepo.readControlCenterState())
    val controlCenterState: StateFlow<ControlCenterState> = _controlCenterState.asStateFlow()

    private val _windowBrightnessOverride = MutableStateFlow<Float?>(null)
    val windowBrightnessOverride: StateFlow<Float?> = _windowBrightnessOverride.asStateFlow()

    private val _statusMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val statusMessages: SharedFlow<String> = _statusMessages.asSharedFlow()

    val settings: StateFlow<LauncherSettings> = settingsStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LauncherSettings()
        )

    val installedApps: StateFlow<List<AppInfo>> = combine(
        rawInstalledApps,
        dao.observeAppUsage()
    ) { apps, usageList ->
        val usageMap = usageList.associateBy { it.componentKey }
        apps.map { app ->
            val usage: AppUsageEntity? = usageMap[app.componentKey]
            if (usage != null) {
                app.copy(launchCount = usage.launchCount, lastLaunchedAt = usage.lastLaunchedAt)
            } else {
                app
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val drawerApps: StateFlow<List<AppInfo>> = combine(
        installedApps,
        _searchQuery,
        _sortOrder
    ) { apps, query, order ->
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.label.contains(trimmed, ignoreCase = true) ||
                    it.packageName.contains(trimmed, ignoreCase = true)
            }
        }
        when (order) {
            AppSortOrder.NAME_ASC -> filtered.sortedBy { it.label.lowercase() }
            AppSortOrder.NAME_DESC -> filtered.sortedByDescending { it.label.lowercase() }
            AppSortOrder.MOST_USED -> filtered.sortedWith(
                compareByDescending<AppInfo> { it.launchCount }
                    .thenBy { it.label.lowercase() }
            )
            AppSortOrder.NEWEST -> filtered.sortedWith(
                compareByDescending<AppInfo> { it.firstInstallTime }
                    .thenBy { it.label.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val homePages: StateFlow<List<List<HomeItem>>> = combine(
        dao.observeHomeItems(),
        dao.observeFolderApps(),
        installedApps,
        _extraPageCount
    ) { entities, folderApps, apps, minPages ->
        val appByKey = apps.associateBy { it.componentKey }
        val appByPkg = apps.associateBy { it.packageName }
        val folderAppsGrouped = folderApps.groupBy { it.folderId }

        val mapped = entities.mapNotNull { entity ->
            when (entity.itemType) {
                "APP" -> {
                    val pkg = entity.packageName ?: return@mapNotNull null
                    val act = entity.activityName ?: return@mapNotNull null
                    val resolved = appByKey["$pkg/$act"] ?: appByPkg[pkg] ?: return@mapNotNull null
                    HomeItem.AppTile(
                        id = entity.id,
                        page = entity.page,
                        position = entity.position,
                        app = resolved
                    )
                }
                "FOLDER" -> {
                    val fid = entity.folderId ?: return@mapNotNull null
                    val fApps = folderAppsGrouped[fid].orEmpty()
                        .sortedBy { it.orderIndex }
                        .mapNotNull { fa ->
                            appByKey["${fa.packageName}/${fa.activityName}"] ?: appByPkg[fa.packageName]
                        }
                    HomeItem.FolderTile(
                        id = entity.id,
                        page = entity.page,
                        position = entity.position,
                        folderId = fid,
                        name = entity.folderName ?: "Folder",
                        apps = fApps
                    )
                }
                "WIDGET" -> {
                    val wType = entity.widgetType?.let {
                        runCatching { WidgetType.valueOf(it) }.getOrNull()
                    } ?: return@mapNotNull null
                    HomeItem.WidgetTile(
                        id = entity.id,
                        page = entity.page,
                        position = entity.position,
                        widgetType = wType
                    )
                }
                else -> null
            }
        }

        val maxPage = maxOf(
            minPages - 1,
            mapped.maxOfOrNull { it.page } ?: 0
        )
        (0..maxPage).map { pageIdx ->
            mapped.filter { it.page == pageIdx }.sortedBy { it.position }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(emptyList())
    )

    val dockItems: StateFlow<List<DockItem>> = combine(
        dao.observeDockItems(),
        installedApps,
        settings
    ) { entities, apps, currentSettings ->
        val appByKey = apps.associateBy { it.componentKey }
        val appByPkg = apps.associateBy { it.packageName }
        entities
            .sortedBy { it.dockOrder }
            .mapNotNull { entity ->
                val pkg = entity.packageName ?: return@mapNotNull null
                val act = entity.activityName ?: return@mapNotNull null
                val resolved = appByKey["$pkg/$act"] ?: appByPkg[pkg] ?: return@mapNotNull null
                DockItem(id = entity.id, dockOrder = entity.dockOrder, app = resolved)
            }
            .take(currentSettings.dockMaxSlots)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshInstalledAppsAndInitHome()
        observePackageUpdates()
        observeTorchChanges()
    }

    private fun refreshInstalledAppsAndInitHome() {
        viewModelScope.launch {
            val loaded = appsRepo.loadInstalledApps()
            rawInstalledApps.value = loaded
            initializeHomeIfFirstRun(loaded)
        }
    }

    private fun observePackageUpdates() {
        viewModelScope.launch {
            appsRepo.observePackageChanges().collect {
                val loaded = appsRepo.loadInstalledApps()
                rawInstalledApps.value = loaded
                cleanupUninstalledPackages(loaded)
            }
        }
    }

    private fun observeTorchChanges() {
        viewModelScope.launch {
            systemRepo.observeTorchState().collect { torchOn ->
                _controlCenterState.value = _controlCenterState.value.copy(isTorchOn = torchOn)
            }
        }
    }

    private suspend fun cleanupUninstalledPackages(currentApps: List<AppInfo>) {
        withContext(Dispatchers.IO) {
            val validPackages = currentApps.map { it.packageName }.toSet()
            val homePackages = dao.getHomeItemsOnce().mapNotNull { it.packageName }.distinct()
            val dockPackages = dao.getDockItemsOnce().mapNotNull { it.packageName }.distinct()
            (homePackages + dockPackages).distinct().forEach { pkg ->
                if (pkg !in validPackages) {
                    dao.deleteDesktopItemsByPackage(pkg)
                    dao.deleteFolderAppsByPackage(pkg)
                }
            }
        }
    }

    private suspend fun initializeHomeIfFirstRun(apps: List<AppInfo>) {
        val currentSettings = settingsStore.settingsFlow.first()
        if (currentSettings.hasInitializedHome) return

        withContext(Dispatchers.IO) {
            val existingHome = dao.getHomeItemsOnce()
            val existingDock = dao.getDockItemsOnce()
            if (existingHome.isNotEmpty() || existingDock.isNotEmpty()) {
                settingsStore.markHomeInitialized(true)
                return@withContext
            }

            if (apps.isEmpty()) return@withContext

            val dockCandidates = pickDefaultDockApps(apps)
            dockCandidates.forEachIndexed { index, app ->
                dao.insertDesktopItem(
                    DesktopItemEntity(
                        itemType = "APP",
                        packageName = app.packageName,
                        activityName = app.activityName,
                        isInDock = true,
                        dockOrder = index
                    )
                )
            }

            val dockKeys = dockCandidates.map { it.componentKey }.toSet()
            val remainingApps = apps.filter { it.componentKey !in dockKeys }

            // Page 0: Clock Widget + Quick Search Widget + top 8 real apps
            val initialItems = mutableListOf<DesktopItemEntity>()
            initialItems += DesktopItemEntity(
                itemType = "WIDGET",
                page = 0,
                position = 0,
                widgetType = WidgetType.CLOCK_DATE.name
            )
            initialItems += DesktopItemEntity(
                itemType = "WIDGET",
                page = 0,
                position = 1,
                widgetType = WidgetType.QUICK_SEARCH.name
            )

            val page0Apps = remainingApps.take(8)
            page0Apps.forEachIndexed { idx, app ->
                initialItems += DesktopItemEntity(
                    itemType = "APP",
                    page = 0,
                    position = idx + 2,
                    packageName = app.packageName,
                    activityName = app.activityName
                )
            }

            // Page 1: Device Pulse (Battery & RAM) Widget + next 12 real apps
            initialItems += DesktopItemEntity(
                itemType = "WIDGET",
                page = 1,
                position = 0,
                widgetType = WidgetType.DEVICE_STATS.name
            )
            val page1Apps = remainingApps.drop(8).take(12)
            page1Apps.forEachIndexed { idx, app ->
                initialItems += DesktopItemEntity(
                    itemType = "APP",
                    page = 1,
                    position = idx + 1,
                    packageName = app.packageName,
                    activityName = app.activityName
                )
            }

            dao.insertDesktopItems(initialItems)
            settingsStore.markHomeInitialized(true)
        }
    }

    private fun pickDefaultDockApps(apps: List<AppInfo>): List<AppInfo> {
        val preferredKeywords = listOf(
            listOf("dialer", "phone", "contacts", "telecom"),
            listOf("messaging", "mms", "sms", "messages"),
            listOf("chrome", "browser", "sbrowser", "firefox"),
            listOf("camera")
        )
        val selected = mutableListOf<AppInfo>()
        for (keywords in preferredKeywords) {
            val match = apps.firstOrNull { app ->
                app !in selected && keywords.any { kw ->
                    app.packageName.contains(kw, ignoreCase = true) ||
                        app.label.contains(kw, ignoreCase = true)
                }
            }
            if (match != null) selected += match
        }
        for (app in apps) {
            if (selected.size >= 4) break
            if (app !in selected) selected += app
        }
        return selected.take(4)
    }

    suspend fun loadIcon(app: AppInfo, iconPx: Int = 128): ImageBitmap? {
        return appsRepo.getAppIcon(app.packageName, app.activityName, iconPx)
    }

    fun launchApp(app: AppInfo) {
        val result = appsRepo.launchApp(app.packageName, app.activityName)
        if (result.isSuccess) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.recordAppLaunch(app.componentKey, System.currentTimeMillis())
            }
            closeOverlays()
        } else {
            _statusMessages.tryEmit("Unable to launch ${app.label}")
        }
    }

    fun openAppInfo(app: AppInfo) {
        appsRepo.openAppInfo(app.packageName)
            .onFailure { _statusMessages.tryEmit("Unable to open info for ${app.label}") }
    }

    fun uninstallApp(app: AppInfo) {
        appsRepo.uninstallApp(app.packageName)
            .onFailure { _statusMessages.tryEmit(it.message ?: "Unable to uninstall ${app.label}") }
    }

    // Navigation & Overlays
    fun setCurrentPage(page: Int) {
        _currentPage.value = page.coerceAtLeast(0)
    }

    fun openOverlay(overlay: ActiveOverlay) {
        if (overlay is ActiveOverlay.ControlCenter || overlay is ActiveOverlay.Settings || overlay is ActiveOverlay.LockPreview) {
            refreshSystemControls()
        }
        _activeOverlay.value = overlay
    }

    fun closeOverlays() {
        _activeOverlay.value = ActiveOverlay.None
        _searchQuery.value = ""
    }

    fun onHomePressed() {
        _isEditMode.value = false
        closeOverlays()
        _currentPage.value = 0
    }

    fun toggleEditMode(enabled: Boolean? = null) {
        _isEditMode.value = enabled ?: !_isEditMode.value
    }

    fun addEmptyPage() {
        val target = homePages.value.size
        _extraPageCount.value = maxOf(_extraPageCount.value, target + 1)
        _currentPage.value = target
    }

    // Drawer search & sort
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(order: AppSortOrder) {
        _sortOrder.value = order
    }

    // Home Desktop & Folder Operations
    fun pinAppToHome(app: AppInfo, page: Int = _currentPage.value) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentOnPage = dao.getHomeItemsOnce().filter { it.page == page }
            val nextPos = (currentOnPage.maxOfOrNull { it.position } ?: -1) + 1
            dao.insertDesktopItem(
                DesktopItemEntity(
                    itemType = "APP",
                    page = page,
                    position = nextPos,
                    packageName = app.packageName,
                    activityName = app.activityName
                )
            )
            _statusMessages.tryEmit("Pinned ${app.label} to Home")
        }
    }

    fun pinAppToDock(app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDock = dao.getDockItemsOnce()
            if (currentDock.any { it.packageName == app.packageName && it.activityName == app.activityName }) {
                _statusMessages.tryEmit("${app.label} is already in Dock")
                return@launch
            }
            val maxSlots = settings.value.dockMaxSlots
            if (currentDock.size >= maxSlots) {
                val last = currentDock.last()
                dao.deleteDesktopItemById(last.id)
            }
            val nextOrder = (currentDock.maxOfOrNull { it.dockOrder } ?: -1) + 1
            dao.insertDesktopItem(
                DesktopItemEntity(
                    itemType = "APP",
                    packageName = app.packageName,
                    activityName = app.activityName,
                    isInDock = true,
                    dockOrder = nextOrder
                )
            )
            _statusMessages.tryEmit("Pinned ${app.label} to Dock")
        }
    }

    fun removeDockItem(item: DockItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteDesktopItemById(item.id)
        }
    }

    fun removeHomeItem(item: HomeItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteDesktopItemById(item.id)
            if (item is HomeItem.FolderTile) {
                dao.deleteAllAppsInFolder(item.folderId)
            }
        }
    }

    fun moveHomeItemRelative(item: HomeItem, delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val pageItems = dao.getHomeItemsOnce()
                .filter { it.page == item.page }
                .sortedBy { it.position }
            val index = pageItems.indexOfFirst { it.id == item.id }
            val targetIdx = index + delta
            if (index == -1 || targetIdx !in pageItems.indices) return@launch
            dao.swapItems(pageItems[index], pageItems[targetIdx])
        }
    }

    fun moveHomeItemToPage(item: HomeItem, targetPage: Int) {
        val safePage = targetPage.coerceAtLeast(0)
        viewModelScope.launch(Dispatchers.IO) {
            if (safePage >= _extraPageCount.value) {
                _extraPageCount.value = safePage + 1
            }
            val targetItems = dao.getHomeItemsOnce().filter { it.page == safePage }
            val nextPos = (targetItems.maxOfOrNull { it.position } ?: -1) + 1
            dao.updateItemPositionAndPage(item.id, safePage, nextPos)
            _currentPage.value = safePage
        }
    }

    fun createFolderWithApp(app: AppInfo, folderName: String, page: Int = _currentPage.value) {
        val cleanName = folderName.trim().ifEmpty { "Glass Folder" }
        val folderId = "folder_${UUID.randomUUID()}"
        viewModelScope.launch(Dispatchers.IO) {
            val pageItems = dao.getHomeItemsOnce().filter { it.page == page }
            val nextPos = (pageItems.maxOfOrNull { it.position } ?: -1) + 1
            dao.insertDesktopItem(
                DesktopItemEntity(
                    itemType = "FOLDER",
                    page = page,
                    position = nextPos,
                    folderId = folderId,
                    folderName = cleanName
                )
            )
            dao.insertFolderApp(
                FolderAppEntity(
                    folderId = folderId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    orderIndex = 0
                )
            )
            _statusMessages.tryEmit("Created folder \"$cleanName\"")
        }
    }

    fun addAppToFolder(folderId: String, app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getAppsInFolder(folderId)
            if (existing.any { it.packageName == app.packageName && it.activityName == app.activityName }) {
                _statusMessages.tryEmit("${app.label} is already in folder")
                return@launch
            }
            val nextIdx = (existing.maxOfOrNull { it.orderIndex } ?: -1) + 1
            dao.insertFolderApp(
                FolderAppEntity(
                    folderId = folderId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    orderIndex = nextIdx
                )
            )
            _statusMessages.tryEmit("Added ${app.label} to folder")
        }
    }

    fun removeAppFromFolder(folderId: String, app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.removeAppFromFolder(folderId, app.packageName, app.activityName)
            val remaining = dao.getAppsInFolder(folderId)
            if (remaining.isEmpty()) {
                dao.getFolderItem(folderId)?.let { dao.deleteDesktopItemById(it.id) }
                if (_activeOverlay.value is ActiveOverlay.Folder) {
                    _activeOverlay.value = ActiveOverlay.None
                }
            }
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        val clean = newName.trim().ifEmpty { "Folder" }
        viewModelScope.launch(Dispatchers.IO) {
            dao.renameFolder(folderId, clean)
        }
    }

    fun addWidgetToPage(widgetType: WidgetType, page: Int = _currentPage.value) {
        viewModelScope.launch(Dispatchers.IO) {
            val pageItems = dao.getHomeItemsOnce().filter { it.page == page }
            // Place widgets neatly near top by shifting items or appending
            val nextPos = (pageItems.maxOfOrNull { it.position } ?: -1) + 1
            dao.insertDesktopItem(
                DesktopItemEntity(
                    itemType = "WIDGET",
                    page = page,
                    position = nextPos,
                    widgetType = widgetType.name
                )
            )
            _statusMessages.tryEmit("Added ${widgetType.title} widget")
        }
    }

    // Control Center Actions
    fun refreshSystemControls() {
        _controlCenterState.value = systemRepo.readControlCenterState()
    }

    fun toggleTorch() {
        val next = !_controlCenterState.value.isTorchOn
        systemRepo.toggleTorch(next)
            .onSuccess {
                _controlCenterState.value = _controlCenterState.value.copy(isTorchOn = it)
            }
            .onFailure {
                _statusMessages.tryEmit(it.message ?: "Flashlight unavailable")
            }
    }

    fun toggleDnd() {
        systemRepo.toggleDoNotDisturb()
            .onSuccess { dndOn ->
                _controlCenterState.value = _controlCenterState.value.copy(
                    isDndOn = dndOn,
                    hasDndPolicyAccess = true
                )
            }
            .onFailure {
                _statusMessages.tryEmit("Grant Do Not Disturb access in System Settings")
            }
    }

    fun updateBrightness(fraction: Float) {
        val clamped = fraction.coerceIn(0.05f, 1f)
        _windowBrightnessOverride.value = clamped
        val wroteSystem = systemRepo.setSystemBrightness(clamped)
        _controlCenterState.value = _controlCenterState.value.copy(
            brightnessFraction = clamped,
            canWriteSystemSettings = wroteSystem
        )
    }

    fun updateVolume(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        systemRepo.setMediaVolume(clamped)
        _controlCenterState.value = _controlCenterState.value.copy(volumeFraction = clamped)
    }

    fun requestDefaultHomeIntent(): Intent = systemRepo.createRequestDefaultHomeIntent()

    // Settings Updates
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsStore.updateThemeMode(mode) }

    fun setWallpaper(preset: WallpaperPreset, customUri: String? = null) = viewModelScope.launch {
        settingsStore.updateWallpaperPreset(preset, customUri)
    }

    fun setGlassOpacity(opacity: Float) = viewModelScope.launch {
        settingsStore.updateGlassOpacity(opacity)
    }

    fun setBlurRadius(radiusDp: Float) = viewModelScope.launch {
        settingsStore.updateBlurRadius(radiusDp)
    }

    fun setMotionLevel(level: MotionLevel) = viewModelScope.launch {
        settingsStore.updateMotionLevel(level)
    }

    fun setIconSize(size: IconSizeOption) = viewModelScope.launch {
        settingsStore.updateIconSize(size)
    }

    fun setShowIconLabels(show: Boolean) = viewModelScope.launch {
        settingsStore.updateShowIconLabels(show)
    }

    fun setShowDock(show: Boolean) = viewModelScope.launch {
        settingsStore.updateShowDock(show)
    }

    fun setDockMaxSlots(slots: Int) = viewModelScope.launch {
        settingsStore.updateDockMaxSlots(slots)
    }

    fun setPerformanceMode(enabled: Boolean) = viewModelScope.launch {
        settingsStore.updatePerformanceMode(enabled)
        if (enabled) {
            appsRepo.trimIconCache()
        }
    }

    fun setLockClockStyle(style: LockClockStyle) = viewModelScope.launch {
        settingsStore.updateLockClockStyle(style)
    }

    fun resetHomeLayoutToDefaults() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.clearAllDesktopItems()
                dao.clearAllFolderApps()
                settingsStore.markHomeInitialized(false)
            }
            val loaded = appsRepo.loadInstalledApps()
            rawInstalledApps.value = loaded
            initializeHomeIfFirstRun(loaded)
            _currentPage.value = 0
            _statusMessages.tryEmit("Home layout restored")
        }
    }

    fun onTrimMemory() {
        appsRepo.trimIconCache()
    }
}
