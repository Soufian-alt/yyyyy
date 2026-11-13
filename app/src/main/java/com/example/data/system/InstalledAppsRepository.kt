package com.example.data.system

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Memory-bounded LRU icon cache tuned for 3GB RAM devices (~6MB ceiling)
    private val iconCache = LruCache<String, ImageBitmap>(96)

    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val selfPackage = context.packageName
        resolveInfos
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != selfPackage }
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(packageManager)?.toString()?.trim()
                    ?.takeIf { it.isNotEmpty() } ?: pkg
                val appInfo = info.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val installTime = runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(
                            pkg,
                            PackageManager.PackageInfoFlags.of(0L)
                        ).firstInstallTime
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(pkg, 0).firstInstallTime
                    }
                }.getOrDefault(0L)

                AppInfo(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    isSystemApp = isSystem,
                    firstInstallTime = installTime
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    suspend fun getAppIcon(packageName: String, activityName: String, iconPx: Int = 128): ImageBitmap? {
        val key = "$packageName/$activityName"
        iconCache.get(key)?.let { return it }

        return withContext(Dispatchers.IO) {
            runCatching {
                val component = ComponentName(packageName, activityName)
                val drawable = packageManager.getActivityIcon(component)
                val targetSize = iconPx.coerceIn(72, 144)
                val bitmap = drawable.toBitmap(width = targetSize, height = targetSize)
                bitmap.asImageBitmap().also { iconCache.put(key, it) }
            }.getOrNull()
        }
    }

    fun getCachedIcon(packageName: String, activityName: String): ImageBitmap? {
        return iconCache.get("$packageName/$activityName")
    }

    fun trimIconCache() {
        iconCache.evictAll()
    }

    fun observePackageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    fun launchApp(packageName: String, activityName: String): Result<Unit> = runCatching {
        val component = ComponentName(packageName, activityName)
        val launchIntent = Intent.makeMainActivity(component).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        try {
            context.startActivity(launchIntent)
        } catch (_: Exception) {
            val fallback = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } ?: error("No launch intent for $packageName")
            context.startActivity(fallback)
        }
    }

    fun openAppInfo(packageName: String): Result<Unit> = runCatching {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(packageName: String): Result<Unit> = runCatching {
        if (packageName == context.packageName) {
            error("Cannot uninstall active launcher")
        }
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
