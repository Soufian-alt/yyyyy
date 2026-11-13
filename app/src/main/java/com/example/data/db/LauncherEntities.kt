package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desktop_items")
data class DesktopItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val itemType: String, // "APP", "FOLDER", "WIDGET"
    val page: Int = 0,
    val position: Int = 0,
    val packageName: String? = null,
    val activityName: String? = null,
    val folderId: String? = null,
    val folderName: String? = null,
    val widgetType: String? = null,
    val isInDock: Boolean = false,
    val dockOrder: Int = 0
)

@Entity(
    tableName = "folder_apps",
    primaryKeys = ["folderId", "packageName", "activityName"]
)
data class FolderAppEntity(
    val folderId: String,
    val packageName: String,
    val activityName: String,
    val orderIndex: Int
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val componentKey: String,
    val launchCount: Int,
    val lastLaunchedAt: Long
)
