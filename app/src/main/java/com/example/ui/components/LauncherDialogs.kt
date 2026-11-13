package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppInfo
import com.example.data.model.HomeItem
import com.example.data.model.WidgetType
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.StatusDanger

@Composable
fun HomeItemActionsDialog(
    item: HomeItem,
    existingFolders: List<HomeItem.FolderTile>,
    totalPages: Int,
    onDismiss: () -> Unit,
    onMoveBackward: () -> Unit,
    onMoveForward: () -> Unit,
    onMoveToPage: (Int) -> Unit,
    onCreateFolderWithApp: ((AppInfo, String) -> Unit)? = null,
    onAddAppToFolder: ((String, AppInfo) -> Unit)? = null,
    onPinToDock: ((AppInfo) -> Unit)? = null,
    onOpenAppInfo: ((AppInfo) -> Unit)? = null,
    onUninstallApp: ((AppInfo) -> Unit)? = null,
    onRemoveFromHome: () -> Unit
) {
    var showFolderCreator by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("Glass Folder") }

    val title = when (item) {
        is HomeItem.AppTile -> item.app.label
        is HomeItem.FolderTile -> item.name
        is HomeItem.WidgetTile -> item.widgetType.title
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable(onClick = onDismiss)
            .padding(20.dp)
    ) {
        LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .clickable(enabled = false, onClick = {})
                .testTag("home_item_actions_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.defaultMinSize(48.dp, 48.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close menu", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    LiquidGlassClickable(
                        onClick = {
                            onMoveBackward()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = LiquidCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Move Up/Left", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LiquidGlassClickable(
                        onClick = {
                            onMoveForward()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text("Move Down/Right", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = LiquidCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page switcher pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Rounded.DriveFileMove, null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(18.dp))
                    Text("Page:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                    for (p in 0 until maxOf(totalPages, 2)) {
                        val selected = p == item.page
                        LiquidGlassClickable(
                            onClick = {
                                onMoveToPage(p)
                                onDismiss()
                            },
                            accentTint = if (selected) LiquidCyan.copy(alpha = 0.35f) else null,
                            modifier = Modifier.defaultMinSize(44.dp, 40.dp)
                        ) {
                            Text(
                                text = "${p + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (item is HomeItem.AppTile) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    ActionRow(
                        icon = Icons.Rounded.Dock,
                        label = "Pin to Glass Dock",
                        onClick = {
                            onPinToDock?.invoke(item.app)
                            onDismiss()
                        }
                    )

                    ActionRow(
                        icon = Icons.Rounded.CreateNewFolder,
                        label = "Create New Folder with App",
                        onClick = { showFolderCreator = !showFolderCreator }
                    )

                    if (showFolderCreator) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            OutlinedTextField(
                                value = folderName,
                                onValueChange = { folderName = it },
                                label = { Text("Folder Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LiquidCyan,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                    focusedLabelColor = LiquidCyan,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LiquidGlassClickable(
                                onClick = {
                                    onCreateFolderWithApp?.invoke(item.app, folderName)
                                    onDismiss()
                                },
                                accentTint = LiquidCyan.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Create Folder",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }

                    if (existingFolders.isNotEmpty()) {
                        Text(
                            text = "Add to Existing Folder",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                        )
                        existingFolders.forEach { folder ->
                            ActionRow(
                                icon = Icons.Rounded.Folder,
                                label = folder.name,
                                onClick = {
                                    onAddAppToFolder?.invoke(folder.folderId, item.app)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ActionRow(
                        icon = Icons.Rounded.Info,
                        label = "App Info",
                        onClick = {
                            onOpenAppInfo?.invoke(item.app)
                            onDismiss()
                        }
                    )

                    if (!item.app.isSystemApp) {
                        ActionRow(
                            icon = Icons.Rounded.DeleteOutline,
                            label = "Uninstall ${item.app.label}",
                            tint = StatusDanger,
                            onClick = {
                                onUninstallApp?.invoke(item.app)
                                onDismiss()
                            }
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ActionRow(
                    icon = Icons.Rounded.Close,
                    label = "Remove from Home Screen",
                    tint = StatusDanger,
                    onClick = {
                        onRemoveFromHome()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerAppActionsDialog(
    app: AppInfo,
    existingFolders: List<HomeItem.FolderTile>,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onPinToHome: () -> Unit,
    onPinToDock: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onAddToFolder: (String) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    var showFolderInput by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("Apps") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable(onClick = onDismiss)
            .padding(20.dp)
    ) {
        LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .clickable(enabled = false, onClick = {})
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.defaultMinSize(48.dp, 48.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                ActionRow(Icons.AutoMirrored.Rounded.Launch, "Open App", onClick = {
                    onLaunch()
                    onDismiss()
                })
                ActionRow(Icons.Rounded.Home, "Pin to Home Screen", onClick = {
                    onPinToHome()
                    onDismiss()
                })
                ActionRow(Icons.Rounded.Dock, "Pin to Glass Dock", onClick = {
                    onPinToDock()
                    onDismiss()
                })
                ActionRow(Icons.Rounded.CreateNewFolder, "Create New Folder", onClick = {
                    showFolderInput = !showFolderInput
                })

                if (showFolderInput) {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LiquidGlassClickable(
                        onClick = {
                            onCreateFolder(folderName)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create & Add App", color = LiquidCyan, modifier = Modifier.padding(12.dp))
                    }
                }

                if (existingFolders.isNotEmpty()) {
                    existingFolders.forEach { folder ->
                        ActionRow(Icons.Rounded.Folder, "Add to ${folder.name}", onClick = {
                            onAddToFolder(folder.folderId)
                            onDismiss()
                        })
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.14f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ActionRow(Icons.Rounded.Info, "App Info", onClick = {
                    onAppInfo()
                    onDismiss()
                })

                if (!app.isSystemApp) {
                    ActionRow(
                        icon = Icons.Rounded.DeleteOutline,
                        label = "Uninstall",
                        tint = StatusDanger,
                        onClick = {
                            onUninstall()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetPickerDialog(
    targetPage: Int,
    onDismiss: () -> Unit,
    onSelectWidget: (WidgetType) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable(onClick = onDismiss)
            .padding(20.dp)
    ) {
        LiquidGlassSurface(
            elevated = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .clickable(enabled = false, onClick = {})
                .testTag("widget_picker_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Widgets, null, tint = LiquidCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Widget (Page ${targetPage + 1})",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.defaultMinSize(48.dp, 48.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                WidgetType.entries.forEach { type ->
                    LiquidGlassClickable(
                        onClick = {
                            onSelectWidget(type)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(type.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    type.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                            Icon(Icons.Rounded.Add, contentDescription = "Add ${type.title}", tint = LiquidCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
