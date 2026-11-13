package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppInfo
import com.example.data.model.DockItem
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidBlue
import com.example.ui.theme.LiquidCyan

@Composable
fun GlassDock(
    dockItems: List<DockItem>,
    iconSize: Dp,
    isEditMode: Boolean,
    loadIcon: suspend (AppInfo) -> ImageBitmap?,
    cachedProvider: (AppInfo) -> ImageBitmap?,
    onLaunchApp: (AppInfo) -> Unit,
    onRemoveDockItem: (DockItem) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .testTag("glass_dock")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                dockItems.forEach { item ->
                    AppIconTile(
                        app = item.app,
                        iconSize = (iconSize - 4.dp).coerceAtLeast(46.dp),
                        showLabel = false,
                        isEditMode = isEditMode,
                        loadIcon = loadIcon,
                        cachedProvider = cachedProvider,
                        onClick = { onLaunchApp(item.app) },
                        onLongClick = { onRemoveDockItem(item) },
                        onRemoveClick = { onRemoveDockItem(item) }
                    )
                }

                // Always present: Liquid Glass App Drawer launcher button
                LiquidGlassClickable(
                    onClick = onOpenDrawer,
                    shape = RoundedCornerShape(17.dp),
                    modifier = Modifier
                        .size((iconSize - 4.dp).coerceAtLeast(48.dp))
                        .defaultMinSize(48.dp, 48.dp)
                        .testTag("dock_app_drawer_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size((iconSize - 4.dp).coerceAtLeast(48.dp))
                            .clip(RoundedCornerShape(17.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        LiquidCyan.copy(alpha = 0.35f),
                                        LiquidBlue.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = stringResource(R.string.cd_app_drawer),
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
