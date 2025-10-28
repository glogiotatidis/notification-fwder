#!/bin/bash

# Teardown script - cleanup after tests

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_NAME="com.notificationforwarder"

echo "Running teardown..."

# Optional: Uninstall app (commented out by default)
# echo "Uninstalling app..."
# adb uninstall "$PACKAGE_NAME" 2>/dev/null || true

# Clear app data (keep app installed)
echo "Clearing app data..."
adb shell pm clear "$PACKAGE_NAME" 2>/dev/null || true

# Stop any running services
echo "Stopping services..."
adb shell am force-stop "$PACKAGE_NAME" 2>/dev/null || true

echo "Teardown complete"
exit 0

