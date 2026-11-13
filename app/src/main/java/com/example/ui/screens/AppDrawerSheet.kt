package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppInfo
import com.example.data.model.AppSortOrder
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppIconTile
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalLiquidGlassConfig

@Composable
fun AppDrawerSheet(
    apps: List<AppInfo>,
    searchQuery: String,
    sortOrder: AppSortOrder,
    focusSearch: Boolean,
    settings: LauncherSettings,
    loadIcon: suspend (AppInfo) -> ImageBitmap?,
    cachedProvider: (AppInfo) -> ImageBitmap?,
    onSearchChange: (String) -> Unit,
    onSortChange: (AppSortOrder) -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onLongPressApp: (AppInfo) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val glassConfig = LocalLiquidGlassConfig.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusSearch) {
        if (focusSearch) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = if (glassConfig.isDarkGlass) 0.62f else 0.42f)
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_drawer_sheet")
    ) {
        LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 14.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Apps, null, tint = LiquidCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Library",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${apps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidCyan,
                            modifier = Modifier
                                .background(LiquidCyan.copy(alpha = 0.16f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .defaultMinSize(48.dp, 48.dp)
                            .testTag("close_drawer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text("Search installed apps...", color = Color.White.copy(alpha = 0.65f))
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.cd_search), tint = LiquidCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_clear_search), tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(focusRequester)
                        .testTag("drawer_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sort Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppSortOrder.entries) { order ->
                        val selected = order == sortOrder
                        LiquidGlassClickable(
                            onClick = { onSortChange(order) },
                            shape = CircleShape,
                            accentTint = if (selected) LiquidCyan.copy(alpha = 0.30f) else null,
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        ) {
                            Text(
                                text = order.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // App Grid
                if (apps.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = LiquidCyan.copy(alpha = 0.7f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "No launchable apps detected" else "No apps matching \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val columns = when {
                            maxWidth >= 840.dp -> 6
                            maxWidth >= 600.dp -> 5
                            else -> 4
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("drawer_apps_grid")
                        ) {
                            items(apps, key = { it.componentKey }) { app ->
                                AppIconTile(
                                    app = app,
                                    iconSize = settings.iconSize.iconDp.dp,
                                    showLabel = true,
                                    isEditMode = false,
                                    loadIcon = loadIcon,
                                    cachedProvider = cachedProvider,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
