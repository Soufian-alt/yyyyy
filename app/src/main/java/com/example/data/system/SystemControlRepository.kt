package com.example.data.system

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.role.RoleManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.model.ControlCenterState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SystemControlRepository(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    private var torchCameraId: String? = null
    private var currentTorchState: Boolean = false

    init {
        torchCameraId = findFlashCameraId()
    }

    private fun findFlashCameraId(): String? {
        val cm = cameraManager ?: return null
        return runCatching {
            cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    fun observeTorchState(): Flow<Boolean> = callbackFlow {
        val cm = cameraManager
        if (cm == null) {
            trySend(false)
            awaitClose {}
            return@callbackFlow
        }
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == torchCameraId || torchCameraId == null) {
                    currentTorchState = enabled
                    trySend(enabled)
                }
            }

            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId == torchCameraId) {
                    currentTorchState = false
                    trySend(false)
                }
            }
        }
        cm.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose {
            runCatching { cm.unregisterTorchCallback(callback) }
        }
    }

    fun readControlCenterState(): ControlCenterState {
        val (wifiConnected, wifiLabel) = readWifiState()
        val (btEnabled, btMissingPerm) = readBluetoothState()
        val airplaneOn = runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        }.getOrDefault(false)

        val dndAccess = notificationManager?.isNotificationPolicyAccessGranted == true
        val dndOn = runCatching {
            val filter = notificationManager?.currentInterruptionFilter
                ?: NotificationManager.INTERRUPTION_FILTER_ALL
            filter != NotificationManager.INTERRUPTION_FILTER_ALL
        }.getOrDefault(false)

        val canWrite = Settings.System.canWrite(context)
        val rawBrightness = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 150)
        }.getOrDefault(150)
        val brightnessFraction = (rawBrightness.coerceIn(10, 255) / 255f)

        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val volumeFraction = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)

        val (batteryPercent, isCharging) = readBatteryState()
        val (availRamMb, totalRamMb) = readMemoryStats()

        return ControlCenterState(
            isWifiConnected = wifiConnected,
            wifiLabel = wifiLabel,
            isBluetoothEnabled = btEnabled,
            bluetoothPermissionMissing = btMissingPerm,
            isAirplaneModeOn = airplaneOn,
            isTorchOn = currentTorchState,
            isTorchAvailable = torchCameraId != null,
            isDndOn = dndOn,
            hasDndPolicyAccess = dndAccess,
            brightnessFraction = brightnessFraction,
            canWriteSystemSettings = canWrite,
            volumeFraction = volumeFraction,
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            availableRamMb = availRamMb,
            totalRamMb = totalRamMb,
            isDefaultHomeLauncher = isDefaultHomeLauncher()
        )
    }

    private fun readWifiState(): Pair<Boolean, String> {
        val cm = connectivityManager ?: return false to "Wi-Fi"
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        val connected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val enabled = wifiManager?.isWifiEnabled == true
        return when {
            connected -> true to "Connected"
            enabled -> false to "On"
            else -> false to "Off"
        }
    }

    private fun readBluetoothState(): Pair<Boolean, Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return false to true
            }
        }
        val enabled = runCatching {
            bluetoothManager?.adapter?.isEnabled == true
        }.getOrDefault(false)
        return enabled to false
    }

    private fun readBatteryState(): Pair<Int, Boolean> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return pct to charging
    }

    private fun readMemoryStats(): Pair<Long, Long> {
        val am = activityManager ?: return 0L to 0L
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val availMb = info.availMem / (1024L * 1024L)
        val totalMb = info.totalMem / (1024L * 1024L)
        return availMb to totalMb
    }

    fun isDefaultHomeLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolved?.activityInfo?.packageName == context.packageName
    }

    fun toggleTorch(enable: Boolean): Result<Boolean> = runCatching {
        val id = torchCameraId ?: findFlashCameraId() ?: error("No camera flash unit found")
        torchCameraId = id
        val cm = cameraManager ?: error("CameraManager unavailable")
        cm.setTorchMode(id, enable)
        currentTorchState = enable
        enable
    }

    fun setSystemBrightness(fraction: Float): Boolean {
        if (!Settings.System.canWrite(context)) return false
        val value = (fraction.coerceIn(0.04f, 1f) * 255).toInt().coerceIn(10, 255)
        return runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
        }.isSuccess
    }

    fun setMediaVolume(fraction: Float) {
        val am = audioManager ?: return
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = (fraction.coerceIn(0f, 1f) * max).toInt().coerceIn(0, max)
        runCatching {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    fun toggleDoNotDisturb(): Result<Boolean> = runCatching {
        val nm = notificationManager ?: error("NotificationManager unavailable")
        if (!nm.isNotificationPolicyAccessGranted) {
            openDndPolicySettings()
            error("DND policy permission required")
        }
        val currentlyOn = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        val nextFilter = if (currentlyOn) {
            NotificationManager.INTERRUPTION_FILTER_ALL
        } else {
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        }
        nm.setInterruptionFilter(nextFilter)
        !currentlyOn
    }

    fun openWifiPanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        startActivitySafely(intent)
    }

    fun openBluetoothSettings() {
        startActivitySafely(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun openAirplaneSettings() {
        startActivitySafely(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
    }

    fun openVolumePanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_VOLUME)
        } else {
            Intent(Settings.ACTION_SOUND_SETTINGS)
        }
        startActivitySafely(intent)
    }

    fun openWriteSettingsPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        startActivitySafely(intent)
    }

    fun openDndPolicySettings() {
        startActivitySafely(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    fun openSystemSettings() {
        startActivitySafely(Intent(Settings.ACTION_SETTINGS))
    }

    fun openDisplaySettings() {
        startActivitySafely(Intent(Settings.ACTION_DISPLAY_SETTINGS))
    }

    fun openBatterySettings() {
        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        if (!startActivitySafely(intent)) {
            startActivitySafely(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        }
    }

    fun createRequestDefaultHomeIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    fun openClockApp() {
        if (!startActivitySafely(Intent(AlarmClock.ACTION_SHOW_ALARMS))) {
            startActivitySafely(Intent(Settings.ACTION_DATE_SETTINGS))
        }
    }

    fun openCalendarApp() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
        }
        startActivitySafely(intent)
    }

    fun openCameraApp() {
        startActivitySafely(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
    }

    private fun startActivitySafely(intent: Intent): Boolean {
        return runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.isSuccess
    }
}
