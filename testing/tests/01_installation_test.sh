#!/bin/bash

# Test 01: Installation Test
# Verifies APK installation and basic app info

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"

TEST_NAME="installation_test"
APK_PATH="$SCRIPT_DIR/../../app/build/outputs/apk/debug/app-debug.apk"

echo "========================================="
echo "Test 01: Installation Test"
echo "========================================="

# Test 1: Check if APK exists
echo -n "Checking if APK exists... "
if [ ! -f "$APK_PATH" ]; then
    echo "FAILED: APK not found at $APK_PATH"
    exit 1
fi
echo "OK"

# Test 2: Install APK
echo -n "Installing APK... "
if uninstall_app && install_apk "$APK_PATH"; then
    echo "OK"
else
    echo "FAILED"
    exit 1
fi

# Wait for installation to complete
sleep 2

# Test 3: Verify package is installed
echo -n "Verifying package installation... "
if is_app_installed; then
    echo "OK"
else
    echo "FAILED"
    exit 2
fi

# Test 4: Get app version
echo -n "Getting app version... "
VERSION=$(get_app_version)
if [ -n "$VERSION" ]; then
    echo "OK (version: $VERSION)"
else
    echo "FAILED"
    exit 1
fi

# Test 5: Launch app
echo -n "Launching app... "
clear_logcat
if launch_app; then
    sleep 3
    echo "OK"
else
    echo "FAILED"
    exit 3
fi

# Test 6: Check for crashes
echo -n "Checking for crashes... "
if get_app_logs 50 | grep -q "FATAL EXCEPTION"; then
    echo "FAILED: App crashed on startup"
    get_app_logs 50
    exit 1
else
    echo "OK"
fi

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "package": "$PACKAGE_NAME",
  "version": "$VERSION",
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "Installation test completed successfully"
echo "========================================="

exit 0

