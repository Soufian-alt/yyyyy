package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppInfo
import com.example.data.model.HomeItem
import com.example.ui.glass.LiquidGlassClickable
import com.example.ui.glass.LiquidGlassSurface
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalLiquidGlassConfig

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderTile(
    folder: HomeItem.FolderTile,
    iconSize: Dp,
    showLabel: Boolean,
    isEditMode: Boolean,
    loadIcon: suspend (AppInfo) -> ImageBitmap?,
    cachedProvider: (AppInfo) -> ImageBitmap?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassConfig = LocalLiquidGlassConfig.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && glassConfig.motionConfig.isEnabled) 0.91f else 1f,
        animationSpec = glassConfig.motionConfig.bouncySpring(),
        label = "folderPressScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 36.dp, color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("folder_tile_${folder.folderId}")
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            LiquidGlassSurface(
                modifier = Modifier.size(iconSize),
                shape = RoundedCornerShape(18.dp),
                elevated = true
            ) {
                val previewApps = folder.apps.take(4)
                if (previewApps.isEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = stringResource(R.string.cd_folder_icon, folder.name),
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(iconSize * 0.5f)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ) {
                        for (rowIndex in 0 until 2) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.weight(1f)
                            ) {
                                for (colIndex in 0 until 2) {
                                    val appIdx = rowIndex * 2 + colIndex
                                    val miniApp = previewApps.getOrNull(appIdx)
                                    Box(
                                        modifier = Modifier
                                            .size((iconSize - 18.dp) / 2f)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        if (miniApp != null) {
                                            val bmp = rememberAppIcon(miniApp, loadIcon, cachedProvider)
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isEditMode) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .clickable(onClick = onRemoveClick)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove ${folder.name}",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = folder.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.65f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FolderOverlaySheet(
    folder: HomeItem.FolderTile,
    installedApps: List<AppInfo>,
    loadIcon: suspend (AppInfo) -> ImageBitmap?,
    cachedProvider: (AppInfo) -> ImageBitmap?,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onRemoveAppFromFolder: (AppInfo) -> Unit,
    onAddAppToFolder: (AppInfo) -> Unit
) {
    var editingName by remember(folder.folderId) { mutableStateOf(false) }
    var nameInput by remember(folder.folderId, folder.name) { mutableStateOf(folder.name) }
    var showAddPicker by remember { mutableStateOf(false) }

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
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .clickable(enabled = false, onClick = {})
                .testTag("folder_expanded_sheet")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (editingName) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = LiquidCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                onRename(nameInput)
                                editingName = false
                            },
                            modifier = Modifier.defaultMinSize(48.dp, 48.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = "Save folder name", tint = LiquidCyan)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { editingName = true }
                        ) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Rename folder",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAddPicker = !showAddPicker },
                        modifier = Modifier.defaultMinSize(48.dp, 48.dp)
                    ) {
                        Icon(
                            imageVector = if (showAddPicker) Icons.Rounded.FolderOpen else Icons.Rounded.Add,
                            contentDescription = if (showAddPicker) "Back to folder apps" else "Add app to folder",
                            tint = LiquidCyan
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.defaultMinSize(48.dp, 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (showAddPicker) {
                    val existingKeys = folder.apps.map { it.componentKey }.toSet()
                    val candidates = installedApps.filter { it.componentKey !in existingKeys }
                    Text(
                        text = "Tap an app to add to ${folder.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(candidates, key = { it.componentKey }) { app ->
                            AppIconTile(
                                app = app,
                                iconSize = 50.dp,
                                showLabel = true,
                                isEditMode = false,
                                loadIcon = loadIcon,
                                cachedProvider = cachedProvider,
                                onClick = {
                                    onAddAppToFolder(app)
                                    showAddPicker = false
                                },
                                onLongClick = {}
                            )
                        }
                    }
                } else if (folder.apps.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp)
                    ) {
                        Text(
                            text = "Folder is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LiquidGlassClickable(
                            onClick = { showAddPicker = true },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Add Apps",
                                color = LiquidCyan,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(folder.apps, key = { it.componentKey }) { app ->
                            AppIconTile(
                                app = app,
                                iconSize = 54.dp,
                                showLabel = true,
                                isEditMode = true,
                                loadIcon = loadIcon,
                                cachedProvider = cachedProvider,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onRemoveAppFromFolder(app) },
                                onRemoveClick = { onRemoveAppFromFolder(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}
