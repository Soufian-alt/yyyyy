package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppInfo
import com.example.data.model.ControlCenterState
import com.example.data.model.DockItem
import com.example.data.model.HomeItem
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppIconTile
import com.example.ui.components.FolderTile
import com.example.ui.components.GlassDock
import com.example.ui.components.HomeWidgetCard
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMint
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homePages: List<List<HomeItem>>,
    dockItems: List<DockItem>,
    settings: LauncherSettings,
    controlState: ControlCenterState,
    currentPage: Int,
    isEditMode: Boolean,
    loadIcon: suspend (AppInfo) -> ImageBitmap?,
    cachedProvider: (AppInfo) -> ImageBitmap?,
    onPageChanged: (Int) -> Unit,
    onToggleEditMode: () -> Unit,
    onAddPage: () -> Unit,
    onOpenWidgetPicker: (Int) -> Unit,
    onOpenDrawer: (Boolean) -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLockPreview: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onOpenFolder: (HomeItem.FolderTile) -> Unit,
    onLongPressHomeItem: (HomeItem) -> Unit,
    onRemoveHomeItem: (HomeItem) -> Unit,
    onRemoveDockItem: (DockItem) -> Unit,
    onOpenClock: () -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safePages = homePages.ifEmpty { listOf(emptyList()) }
    val pagerState = rememberPagerState(
        initialPage = currentPage.coerceIn(0, safePages.lastIndex),
        pageCount = { safePages.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentPage, safePages.size) {
        val target = currentPage.coerceIn(0, safePages.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentPage) {
            onPageChanged(pagerState.currentPage)
        }
    }

    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulated += dragAmount
                    },
                    onDragEnd = {
                        if (dragAccumulated < -95f) {
                            onOpenDrawer(false)
                        } else if (dragAccumulated > 95f) {
                            onOpenControlCenter()
                        }
                        dragAccumulated = 0f
                    }
                )
            }
            .testTag("home_screen_root")
    ) {
        // Top Liquid Glass Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                LiquidGlassClickable(
                    onClick = onToggleEditMode,
                    shape = CircleShape,
                    accentTint = if (isEditMode) LiquidCyan.copy(alpha = 0.28f) else null,
                    modifier = Modifier.testTag("toggle_edit_mode_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                            contentDescription = if (isEditMode) "Done editing" else "Edit home screen",
                            tint = if (isEditMode) LiquidMint else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "Done" else "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }

                if (isEditMode) {
                    LiquidGlassClickable(
                        onClick = { onOpenWidgetPicker(pagerState.currentPage) },
                        shape = CircleShape,
                        modifier = Modifier.testTag("add_widget_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Rounded.Widgets, contentDescription = "Add widget", tint = LiquidCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Widget", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }

                    LiquidGlassClickable(
                        onClick = onAddPage,
                        shape = CircleShape,
                        modifier = Modifier.testTag("add_page_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add page", tint = LiquidCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Page", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                LiquidGlassClickable(
                    onClick = onOpenLockPreview,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_lock_preview_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.cd_lock_preview),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LiquidGlassClickable(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LiquidGlassClickable(
                    onClick = onOpenControlCenter,
                    shape = CircleShape,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("open_control_center_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = stringResource(R.string.cd_control_center),
                            tint = LiquidCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Control",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Multi-Page Workspace
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val columnCount = when {
                maxWidth >= 840.dp -> 6
                maxWidth >= 600.dp -> 5
                else -> 4
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val pageItems = safePages.getOrElse(pageIndex) { emptyList() }

                if (pageItems.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        LiquidGlassSurface(
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Page ${pageIndex + 1} is Ready",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Swipe up for App Drawer to pin apps, or add a Liquid Glass widget.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LiquidGlassClickable(onClick = { onOpenWidgetPicker(pageIndex) }) {
                                        Text("Add Widget", color = LiquidCyan, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                                    }
                                    LiquidGlassClickable(onClick = { onOpenDrawer(false) }) {
                                        Text("Open Apps", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = pageItems,
                            key = { "home_${it.id}" },
                            span = { item ->
                                if (item is HomeItem.WidgetTile) {
                                    GridItemSpan(maxLineSpan)
                                } else {
                                    GridItemSpan(1)
                                }
                            }
                        ) { item ->
                            when (item) {
                                is HomeItem.WidgetTile -> HomeWidgetCard(
                                    widgetType = item.widgetType,
                                    controlState = controlState,
                                    isEditMode = isEditMode,
                                    onRemove = { onRemoveHomeItem(item) },
                                    onOpenClock = onOpenClock,
                                    onOpenCalendar = onOpenCalendar,
                                    onOpenSearch = { onOpenDrawer(true) },
                                    onOpenControlCenter = onOpenControlCenter
                                )
                                is HomeItem.AppTile -> AppIconTile(
                                    app = item.app,
                                    iconSize = settings.iconSize.iconDp.dp,
                                    showLabel = settings.showIconLabels,
                                    isEditMode = isEditMode,
                                    loadIcon = loadIcon,
                                    cachedProvider = cachedProvider,
                                    onClick = {
                                        if (isEditMode) onLongPressHomeItem(item) else onLaunchApp(item.app)
                                    },
                                    onLongClick = { onLongPressHomeItem(item) },
                                    onRemoveClick = { onRemoveHomeItem(item) }
                                )
                                is HomeItem.FolderTile -> FolderTile(
                                    folder = item,
                                    iconSize = settings.iconSize.iconDp.dp,
                                    showLabel = settings.showIconLabels,
                                    isEditMode = isEditMode,
                                    loadIcon = loadIcon,
                                    cachedProvider = cachedProvider,
                                    onClick = {
                                        if (isEditMode) onLongPressHomeItem(item) else onOpenFolder(item)
                                    },
                                    onLongClick = { onLongPressHomeItem(item) },
                                    onRemoveClick = { onRemoveHomeItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Liquid Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            repeat(safePages.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(7.dp)
                        .width(if (selected) 22.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) LiquidCyan else Color.White.copy(alpha = 0.35f)
                        )
                        .clickableNoRippleCompat {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                )
            }
        }

        if (settings.showDock) {
            GlassDock(
                dockItems = dockItems,
                iconSize = settings.iconSize.iconDp.dp,
                isEditMode = isEditMode,
                loadIcon = loadIcon,
                cachedProvider = cachedProvider,
                onLaunchApp = onLaunchApp,
                onRemoveDockItem = onRemoveDockItem,
                onOpenDrawer = { onOpenDrawer(false) }
            )
        } else {
            // Subtle pill hint when dock is hidden
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                LiquidGlassClickable(
                    onClick = { onOpenDrawer(false) },
                    shape = CircleShape,
                    modifier = Modifier.testTag("hidden_dock_drawer_pill")
                ) {
                    Text(
                        text = "Swipe up or tap for Apps",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoRippleCompat(onClick: () -> Unit): Modifier {
    return this.clip(CircleShape).clickable(onClick = onClick)
}
