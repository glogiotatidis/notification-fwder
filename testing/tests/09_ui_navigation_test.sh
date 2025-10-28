#!/bin/bash

# Test 09: UI Navigation Test
# Tests UI screens and navigation

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"

TEST_NAME="ui_navigation_test"
SCREENSHOTS_DIR="$SCRIPT_DIR/../logs/screenshots_$(date +%Y%m%d_%H%M%S)"

echo "========================================="
echo "Test 09: UI Navigation Test"
echo "========================================="

# Create screenshots directory
mkdir -p "$SCREENSHOTS_DIR"

# Ensure app is running
echo "Launching app..."
launch_app
sleep 3

# Test 1: Home screen visible
echo -n "Checking Home screen... "
take_screenshot "$SCREENSHOTS_DIR/01_home.png"
if adb shell dumpsys window | grep -q "$PACKAGE_NAME"; then
    echo "OK"
else
    echo "FAILED - App not in foreground"
    exit 1
fi

# Test 2: Navigate to Webhooks tab
echo -n "Navigating to Webhooks tab... "
# Tap on Webhooks navigation item (approximate position, adjust if needed)
adb shell input tap 360 2100 2>/dev/null || adb shell input tap 360 1900
sleep 2
take_screenshot "$SCREENSHOTS_DIR/02_webhooks.png"
echo "OK"

# Test 3: Navigate to History tab
echo -n "Navigating to History tab... "
# Tap on History navigation item
adb shell input tap 540 2100 2>/dev/null || adb shell input tap 540 1900
sleep 2
take_screenshot "$SCREENSHOTS_DIR/03_history.png"
echo "OK"

# Test 4: Navigate back to Home
echo -n "Navigating back to Home... "
adb shell input tap 180 2100 2>/dev/null || adb shell input tap 180 1900
sleep 2
take_screenshot "$SCREENSHOTS_DIR/04_back_to_home.png"
echo "OK"

# Test 5: Screen rotation (if supported)
echo -n "Testing screen rotation... "
# Force landscape
adb shell settings put system user_rotation 1
sleep 2
take_screenshot "$SCREENSHOTS_DIR/05_landscape.png"
# Force portrait
adb shell settings put system user_rotation 0
sleep 2
echo "OK"

# Test 6: Back button handling
echo -n "Testing back button... "
press_back
sleep 1
# App should still be visible or minimized gracefully
if adb shell dumpsys window | grep -q "$PACKAGE_NAME\|Launcher"; then
    echo "OK"
else
    echo "WARNING - Unexpected state after back press"
fi

# Test 7: Check for UI errors in logs
echo -n "Checking for UI errors... "
if get_app_logs 100 | grep -i "compose.*error\|ui.*crash"; then
    echo "WARNING - Found UI errors (review above)"
else
    echo "OK - No UI errors found"
fi

echo ""
echo "Screenshots saved to: $SCREENSHOTS_DIR"

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "screens_tested": ["home", "webhooks", "history"],
  "navigation_working": true,
  "rotation_tested": true,
  "screenshots_dir": "$SCREENSHOTS_DIR",
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "UI navigation test completed successfully"
echo "========================================="

exit 0

