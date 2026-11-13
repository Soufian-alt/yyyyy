package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {

    @Query("SELECT * FROM desktop_items WHERE isInDock = 0 ORDER BY page ASC, position ASC")
    fun observeHomeItems(): Flow<List<DesktopItemEntity>>

    @Query("SELECT * FROM desktop_items WHERE isInDock = 1 ORDER BY dockOrder ASC")
    fun observeDockItems(): Flow<List<DesktopItemEntity>>

    @Query("SELECT * FROM folder_apps ORDER BY folderId ASC, orderIndex ASC")
    fun observeFolderApps(): Flow<List<FolderAppEntity>>

    @Query("SELECT * FROM app_usage")
    fun observeAppUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM desktop_items WHERE isInDock = 0 ORDER BY page ASC, position ASC")
    suspend fun getHomeItemsOnce(): List<DesktopItemEntity>

    @Query("SELECT * FROM desktop_items WHERE isInDock = 1 ORDER BY dockOrder ASC")
    suspend fun getDockItemsOnce(): List<DesktopItemEntity>

    @Query("SELECT * FROM desktop_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): DesktopItemEntity?

    @Query("SELECT * FROM desktop_items WHERE folderId = :folderId LIMIT 1")
    suspend fun getFolderItem(folderId: String): DesktopItemEntity?

    @Query("SELECT * FROM folder_apps WHERE folderId = :folderId ORDER BY orderIndex ASC")
    suspend fun getAppsInFolder(folderId: String): List<FolderAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesktopItem(item: DesktopItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesktopItems(items: List<DesktopItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderApp(folderApp: FolderAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderApps(folderApps: List<FolderAppEntity>)

    @Query("DELETE FROM desktop_items WHERE id = :id")
    suspend fun deleteDesktopItemById(id: Long)

    @Query("DELETE FROM desktop_items WHERE packageName = :packageName")
    suspend fun deleteDesktopItemsByPackage(packageName: String)

    @Query("DELETE FROM folder_apps WHERE packageName = :packageName")
    suspend fun deleteFolderAppsByPackage(packageName: String)

    @Query("DELETE FROM folder_apps WHERE folderId = :folderId AND packageName = :packageName AND activityName = :activityName")
    suspend fun removeAppFromFolder(folderId: String, packageName: String, activityName: String)

    @Query("DELETE FROM folder_apps WHERE folderId = :folderId")
    suspend fun deleteAllAppsInFolder(folderId: String)

    @Query("UPDATE desktop_items SET folderName = :newName WHERE folderId = :folderId")
    suspend fun renameFolder(folderId: String, newName: String)

    @Query("UPDATE desktop_items SET position = :position, page = :page WHERE id = :id")
    suspend fun updateItemPositionAndPage(id: Long, page: Int, position: Int)

    @Query("UPDATE desktop_items SET dockOrder = :dockOrder WHERE id = :id")
    suspend fun updateDockOrder(id: Long, dockOrder: Int)

    @Query("SELECT * FROM app_usage WHERE componentKey = :componentKey LIMIT 1")
    suspend fun getUsage(componentKey: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsage(usage: AppUsageEntity)

    @Query("DELETE FROM desktop_items")
    suspend fun clearAllDesktopItems()

    @Query("DELETE FROM folder_apps")
    suspend fun clearAllFolderApps()

    @Transaction
    suspend fun swapItems(first: DesktopItemEntity, second: DesktopItemEntity) {
        updateItemPositionAndPage(first.id, second.page, second.position)
        updateItemPositionAndPage(second.id, first.page, first.position)
    }

    @Transaction
    suspend fun recordAppLaunch(componentKey: String, timestamp: Long) {
        val existing = getUsage(componentKey)
        val count = (existing?.launchCount ?: 0) + 1
        upsertUsage(AppUsageEntity(componentKey, count, timestamp))
    }
}
