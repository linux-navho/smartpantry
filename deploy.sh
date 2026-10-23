#!/usr/bin/env bash
set -e

# --- ARCH LINUX ENVIRONMENT SETUP ---
# Fixes the "sdk.dir is missing" error when running Gradle outside Android Studio
export ANDROID_HOME="$HOME/Android/Sdk"

# Fallback just in case you installed the SDK via the AUR package instead of Android Studio
if [ ! -d "$ANDROID_HOME" ]; then
    export ANDROID_HOME="/opt/android-sdk"
fi

# Ensure ADB is running
adb start-server

echo "==> Building Debug APK..."
./gradlew assembleDebug --no-configuration-cache

echo "==> Installing APK to device..."
adb install -r -d app/build/outputs/apk/debug/app-debug.apk

echo "==> Launching App..."
adb shell am start -n com.phaswana.smartpantrymanager/.LoginActivity

echo "==> Deployment Complete."
