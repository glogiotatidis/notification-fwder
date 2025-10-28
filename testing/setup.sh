#!/bin/bash

# Setup script for test environment

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

echo "Setting up test environment..."

# Create necessary directories
mkdir -p "$LOG_DIR"

# Check ADB availability
if ! command -v adb &> /dev/null; then
    echo "Error: ADB not found. Please install Android SDK Platform Tools"
    exit 1
fi

# Check device connection
if ! adb devices | grep -q "device$"; then
    echo "Error: No device connected or device unauthorized"
    exit 3
fi

# Get device info
DEVICE_MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
ANDROID_VERSION=$(adb shell getprop ro.build.version.release | tr -d '\r')
API_LEVEL=$(adb shell getprop ro.build.version.sdk | tr -d '\r')

echo "Device: $DEVICE_MODEL"
echo "Android: $ANDROID_VERSION (API $API_LEVEL)"

# Check minimum API level
if [ "$API_LEVEL" -lt 26 ]; then
    echo "Error: Device API level is $API_LEVEL, but minimum required is 26 (Android 8.0)"
    exit 2
fi

# Build the app if APK doesn't exist
APK_PATH="$SCRIPT_DIR/../app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "APK not found, building..."
    cd "$SCRIPT_DIR/.."
    if [ -f "gradlew" ]; then
        ./gradlew assembleDebug
    else
        echo "Error: gradlew not found"
        exit 1
    fi
fi

echo "Setup complete"
exit 0

