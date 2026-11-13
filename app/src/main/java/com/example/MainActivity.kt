package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActiveOverlay
import com.example.data.model.AppInfo
import com.example.data.model.HomeItem
import com.example.ui.components.DrawerAppActionsDialog
import com.example.ui.components.FolderOverlaySheet
import com.example.ui.components.HomeItemActionsDialog
import com.example.ui.components.WallpaperBackground
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.screens.AppDrawerSheet
import com.example.ui.screens.ControlCenterSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockPreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalLiquidGlassConfig
import com.example.ui.theme.MyLiquidOSTheme
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val windowBrightness by viewModel.windowBrightnessOverride.collectAsStateWithLifecycle()

            LaunchedEffect(windowBrightness) {
                windowBrightness?.let { fraction ->
                    val lp = window.attributes
                    lp.screenBrightness = fraction.coerceIn(0.05f, 1f)
                    window.attributes = lp
                }
            }

            MyLiquidOSTheme(
                themeMode = settings.themeMode,
                glassOpacity = settings.glassOpacity,
                blurRadiusDp = settings.blurRadiusDp,
                motionLevel = settings.motionLevel,
                performanceMode = settings.performanceMode
            ) {
                LiquidLauncherRoot(
                    viewModel = viewModel,
                    onLaunchDefaultHomePicker = {
                        runCatching { startActivity(viewModel.requestDefaultHomeIntent()) }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.onHomePressed()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            viewModel.onTrimMemory()
        }
    }
}

@Composable
fun LiquidLauncherRoot(
    viewModel: LauncherViewModel,
    onLaunchDefaultHomePicker: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val homePages by viewModel.homePages.collectAsStateWithLifecycle()
    val dockItems by viewModel.dockItems.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val drawerApps by viewModel.drawerApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val controlState by viewModel.controlCenterState.collectAsStateWithLifecycle()
    val activeOverlay by viewModel.activeOverlay.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val motionConfig = LocalLiquidGlassConfig.current.motionConfig
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedHomeItemForMenu by remember { mutableStateOf<HomeItem?>(null) }
    var selectedDrawerAppForMenu by remember { mutableStateOf<AppInfo?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSystemControls()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.statusMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val allFolders = remember(homePages) {
        homePages.flatten().filterIsInstance<HomeItem.FolderTile>()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperBackground(settings = settings)

            HomeScreen(
                homePages = homePages,
                dockItems = dockItems,
                settings = settings,
                controlState = controlState,
                currentPage = currentPage,
                isEditMode = isEditMode,
                loadIcon = { app -> viewModel.loadIcon(app) },
                cachedProvider = { app -> viewModel.appsRepo.getCachedIcon(app.packageName, app.activityName) },
                onPageChanged = viewModel::setCurrentPage,
                onToggleEditMode = { viewModel.toggleEditMode() },
                onAddPage = viewModel::addEmptyPage,
                onOpenWidgetPicker = { page -> viewModel.openOverlay(ActiveOverlay.WidgetPicker(page)) },
                onOpenDrawer = { focusSearch -> viewModel.openOverlay(ActiveOverlay.AppDrawer(focusSearch)) },
                onOpenControlCenter = { viewModel.openOverlay(ActiveOverlay.ControlCenter) },
                onOpenSettings = { viewModel.openOverlay(ActiveOverlay.Settings) },
                onOpenLockPreview = { viewModel.openOverlay(ActiveOverlay.LockPreview) },
                onLaunchApp = viewModel::launchApp,
                onOpenFolder = { folder -> viewModel.openOverlay(ActiveOverlay.Folder(folder.folderId)) },
                onLongPressHomeItem = { item -> selectedHomeItemForMenu = item },
                onRemoveHomeItem = viewModel::removeHomeItem,
                onRemoveDockItem = viewModel::removeDockItem,
                onOpenClock = { viewModel.systemRepo.openClockApp() },
                onOpenCalendar = { viewModel.systemRepo.openCalendarApp() }
            )

            // App Drawer Overlay
            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.AppDrawer,
                enter = motionConfig.drawerEnter(),
                exit = motionConfig.drawerExit()
            ) {
                val focusSearch = (activeOverlay as? ActiveOverlay.AppDrawer)?.focusSearch == true
                AppDrawerSheet(
                    apps = drawerApps,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    focusSearch = focusSearch,
                    settings = settings,
                    loadIcon = { app -> viewModel.loadIcon(app) },
                    cachedProvider = { app -> viewModel.appsRepo.getCachedIcon(app.packageName, app.activityName) },
                    onSearchChange = viewModel::updateSearchQuery,
                    onSortChange = viewModel::updateSortOrder,
                    onLaunchApp = viewModel::launchApp,
                    onLongPressApp = { app -> selectedDrawerAppForMenu = app },
                    onClose = viewModel::closeOverlays
                )
            }

            // Control Center Overlay
            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.ControlCenter,
                enter = motionConfig.controlCenterEnter(),
                exit = motionConfig.controlCenterExit()
            ) {
                ControlCenterSheet(
                    state = controlState,
                    onRefresh = viewModel::refreshSystemControls,
                    onWifiClick = { viewModel.systemRepo.openWifiPanel() },
                    onBluetoothClick = { viewModel.systemRepo.openBluetoothSettings() },
                    onAirplaneClick = { viewModel.systemRepo.openAirplaneSettings() },
                    onToggleTorch = viewModel::toggleTorch,
                    onToggleDnd = viewModel::toggleDnd,
                    onBrightnessChange = viewModel::updateBrightness,
                    onRequestWriteSettings = { viewModel.systemRepo.openWriteSettingsPermission() },
                    onVolumeChange = viewModel::updateVolume,
                    onOpenVolumePanel = { viewModel.systemRepo.openVolumePanel() },
                    onOpenSystemSettings = { viewModel.systemRepo.openSystemSettings() },
                    onOpenDisplaySettings = { viewModel.systemRepo.openDisplaySettings() },
                    onOpenBatterySettings = { viewModel.systemRepo.openBatterySettings() },
                    onOpenLauncherSettings = { viewModel.openOverlay(ActiveOverlay.Settings) },
                    onOpenLockPreview = { viewModel.openOverlay(ActiveOverlay.LockPreview) },
                    onRequestDefaultHome = onLaunchDefaultHomePicker,
                    onClose = viewModel::closeOverlays
                )
            }

            // Expanded Folder Overlay
            val openFolderId = (activeOverlay as? ActiveOverlay.Folder)?.folderId
            val openFolder = remember(allFolders, openFolderId) {
                openFolderId?.let { id -> allFolders.firstOrNull { it.folderId == id } }
            }
            AnimatedVisibility(
                visible = openFolder != null,
                enter = motionConfig.folderEnter(),
                exit = motionConfig.folderExit()
            ) {
                openFolder?.let { folder ->
                    FolderOverlaySheet(
                        folder = folder,
                        installedApps = installedApps,
                        loadIcon = { app -> viewModel.loadIcon(app) },
                        cachedProvider = { app -> viewModel.appsRepo.getCachedIcon(app.packageName, app.activityName) },
                        onDismiss = viewModel::closeOverlays,
                        onRename = { newName -> viewModel.renameFolder(folder.folderId, newName) },
                        onLaunchApp = viewModel::launchApp,
                        onRemoveAppFromFolder = { app -> viewModel.removeAppFromFolder(folder.folderId, app) },
                        onAddAppToFolder = { app -> viewModel.addAppToFolder(folder.folderId, app) }
                    )
                }
            }

            // Settings Screen
            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.Settings,
                enter = fadeIn(motionConfig.standardSpring()),
                exit = fadeOut(motionConfig.standardSpring())
            ) {
                SettingsScreen(
                    settings = settings,
                    isDefaultHome = controlState.isDefaultHomeLauncher,
                    onRequestDefaultHome = onLaunchDefaultHomePicker,
                    onThemeChange = viewModel::setThemeMode,
                    onWallpaperChange = viewModel::setWallpaper,
                    onGlassOpacityChange = viewModel::setGlassOpacity,
                    onBlurRadiusChange = viewModel::setBlurRadius,
                    onMotionLevelChange = viewModel::setMotionLevel,
                    onIconSizeChange = viewModel::setIconSize,
                    onShowLabelsChange = viewModel::setShowIconLabels,
                    onShowDockChange = viewModel::setShowDock,
                    onDockSlotsChange = viewModel::setDockMaxSlots,
                    onPerformanceModeChange = viewModel::setPerformanceMode,
                    onLockStyleChange = viewModel::setLockClockStyle,
                    onOpenLockPreview = { viewModel.openOverlay(ActiveOverlay.LockPreview) },
                    onResetLayout = viewModel::resetHomeLayoutToDefaults,
                    onClose = viewModel::closeOverlays
                )
            }

            // Optional Lock Screen Preview
            AnimatedVisibility(
                visible = activeOverlay is ActiveOverlay.LockPreview,
                enter = fadeIn(motionConfig.standardSpring()),
                exit = fadeOut(motionConfig.standardSpring())
            ) {
                LockPreviewScreen(
                    settings = settings,
                    controlState = controlState,
                    onToggleTorch = viewModel::toggleTorch,
                    onOpenCamera = { viewModel.systemRepo.openCameraApp() },
                    onUnlock = viewModel::closeOverlays
                )
            }

            // Widget Picker Modal
            val widgetTargetPage = (activeOverlay as? ActiveOverlay.WidgetPicker)?.targetPage
            if (widgetTargetPage != null) {
                WidgetPickerDialog(
                    targetPage = widgetTargetPage,
                    onDismiss = viewModel::closeOverlays,
                    onSelectWidget = { widgetType -> viewModel.addWidgetToPage(widgetType, widgetTargetPage) }
                )
            }

            // Long-press Home Item Context Menu
            selectedHomeItemForMenu?.let { item ->
                HomeItemActionsDialog(
                    item = item,
                    existingFolders = allFolders.filter {
                        item !is HomeItem.FolderTile || it.folderId != item.folderId
                    },
                    totalPages = homePages.size,
                    onDismiss = { selectedHomeItemForMenu = null },
                    onMoveBackward = { viewModel.moveHomeItemRelative(item, -1) },
                    onMoveForward = { viewModel.moveHomeItemRelative(item, +1) },
                    onMoveToPage = { page -> viewModel.moveHomeItemToPage(item, page) },
                    onCreateFolderWithApp = { app, name -> viewModel.createFolderWithApp(app, name, item.page) },
                    onAddAppToFolder = { fid, app -> viewModel.addAppToFolder(fid, app) },
                    onPinToDock = { app -> viewModel.pinAppToDock(app) },
                    onOpenAppInfo = { app -> viewModel.openAppInfo(app) },
                    onUninstallApp = { app -> viewModel.uninstallApp(app) },
                    onRemoveFromHome = { viewModel.removeHomeItem(item) }
                )
            }

            // Long-press Drawer App Context Menu
            selectedDrawerAppForMenu?.let { app ->
                DrawerAppActionsDialog(
                    app = app,
                    existingFolders = allFolders,
                    onDismiss = { selectedDrawerAppForMenu = null },
                    onLaunch = { viewModel.launchApp(app) },
                    onPinToHome = { viewModel.pinAppToHome(app, currentPage) },
                    onPinToDock = { viewModel.pinAppToDock(app) },
                    onCreateFolder = { name -> viewModel.createFolderWithApp(app, name, currentPage) },
                    onAddToFolder = { fid -> viewModel.addAppToFolder(fid, app) },
                    onAppInfo = { viewModel.openAppInfo(app) },
                    onUninstall = { viewModel.uninstallApp(app) }
                )
            }
        }
    }
}
