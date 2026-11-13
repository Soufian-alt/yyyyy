# My Liquid OS

**My Liquid OS** is a real, lightweight Android Home Launcher built with **Kotlin**, **Jetpack Compose**, and **Material 3**, featuring an original **Liquid Glass** aesthetic engineered specifically for smooth **60Hz** performance on **Android 11** devices with **3GB RAM** (such as the **Samsung Galaxy A03s**).

---

## Core Features

- **Real Android Home Launcher (`CATEGORY_HOME`)**: Registers as a genuine Home screen replacement in `AndroidManifest.xml` with `singleTask` launch behavior and `onNewIntent` home-button reset.
- **Multi-Page Home Screen**:
  - `HorizontalPager` workspace with animated capsule page indicators and ability to add extra pages.
  - Interactive Liquid Glass widgets: **Liquid Clock** (live time/date + clock/calendar shortcuts), **Device Pulse** (real battery & live RAM usage monitor), and **Glass Search**.
  - Reorder apps, folders, and widgets across positions or pages via Edit Mode or long-press context menus.
  - **Folders**: Create, rename, add/remove apps, and preview up to 4 mini-icons in a 2×2 Liquid Glass folder tile.
  - **Glass Dock**: Floating frosted dock at the bottom (4 or 5 customizable slots) plus App Drawer launcher button.
- **App Drawer**:
  - Real-time search bar, sorting (`A–Z`, `Z–A`, `Most Used`, `Newest`), memory-bounded `LruCache` icon loading (`Dispatchers.IO`), and automatic live updates via `PackageManager` broadcast listeners.
- **Control Center**:
  - **Wi-Fi**: Reads real active Wi-Fi transport state and opens Android's floating `Settings.Panel.ACTION_WIFI`.
  - **Bluetooth**: Reads real adapter state (permission-aware on Android 11 vs Android 12+) and opens Bluetooth Settings.
  - **Airplane Mode**: Reads `Settings.Global.AIRPLANE_MODE_ON` and opens Airplane Mode Settings.
  - **Flashlight (Torch)**: Real `CameraManager.setTorchMode` hardware toggle with live `TorchCallback`.
  - **Do Not Disturb**: Real `NotificationManager` filter toggle (when Notification Policy Access is granted).
  - **Brightness & Media Volume Sliders**: Immediate window + system brightness (`Settings.System.canWrite`) and real `AudioManager.STREAM_MUSIC` control.
  - **System Shortcuts**: Direct access to Android Settings, Display, Battery, and Launcher Settings.
- **Animation Engine & Performance Mode**:
  - Switch between **Full Liquid**, **Reduced**, and **Instant (Off)** motion profiles.
  - Dedicated **Performance Mode** disables `RenderEffect` blur, trims the icon cache, and simplifies glass overlays for 3GB RAM / 60Hz hardware.
- **Wallpaper Studio**:
  - 4 bundled Liquid Glass wallpapers plus zero-permission **Android Photo Picker** (`PickVisualMedia`) for custom gallery photos.
- **Optional Lock Screen Preview**:
  - In-app customizable Lock Screen preview (`Minimal Glass`, `Bold Display`, `Compact Pill`) with live battery, flashlight toggle, camera shortcut, and swipe-up unlock.

---

## Android 11 & Security Notes

1. **Package Visibility (`<queries>`)**: Instead of requesting the broad `QUERY_ALL_PACKAGES` permission, `AndroidManifest.xml` declares targeted `<queries>` for `ACTION_MAIN` + `CATEGORY_LAUNCHER` so Android 11+ lists all launchable apps cleanly while staying Play-policy friendly.
2. **Backdrop Blur on Android 11**: Hardware `RenderEffect` (`Modifier.blur`) was introduced in Android 12 (API 31). On Android 11 (API 30), My Liquid OS automatically uses a single-pass translucent frosted gradient + specular rim shader to preserve 60fps without lag.
3. **System Toggles**: Android restricts third-party apps from silently toggling Airplane Mode or Wi-Fi without user/system interaction; My Liquid OS opens official system panels (`Settings.Panel`) instead of showing non-functional switches.
4. **Lock Screen Scope**: Android does not allow standard third-party apps to replace the secure OS Keyguard; the included Lock Screen is clearly labeled as an optional in-app preview & quick-glance mode.

---

## Building & Running in Android Studio

1. **Open the Project**:
   - Launch **Android Studio** (Ladybug / Meerkat or newer with JDK 11+ / AGP 9 compatible).
   - Choose **File → Open** and select this project root folder.
2. **Sync Gradle**:
   - Allow Android Studio to sync `gradle/libs.versions.toml` and `app/build.gradle.kts`.
   - Note: Do **not** create a `local.properties` file if building inside the cloud container (`ANDROID_SDK_ROOT` is used automatically).
3. **Build & Install Debug APK**:
   - In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**, or click **Run** on a connected Android 11+ device (e.g., Galaxy A03s with USB Debugging enabled).
   - Or via command line:
     ```bash
     gradle :app:assembleDebug
     ```
   - The generated APK will be located at:
     `app/build/outputs/apk/debug/app-debug.apk`
4. **Set as Default Home Launcher**:
   - After installing, press the **Home** button on your phone and select **My Liquid OS → Always**, or open **My Liquid OS Settings → Home Launcher Role → Set Default**.
