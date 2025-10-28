# Testing Framework for Notification Forwarder

## AI Agent Testing Instructions

This testing framework is designed to be executed by AI agents to verify the correctness and completeness of the Notification Forwarder Android app on a real Android device via ADB.

## Prerequisites

### Hardware & Software Requirements

1. **Android Device**:
   - Android 8.0 (API 26) or higher
   - USB debugging enabled
   - Connected via USB or wireless ADB

2. **Development Machine**:
   - ADB (Android Debug Bridge) installed
   - Bash shell (Linux/macOS) or Git Bash (Windows)
   - curl or wget for webhook testing
   - jq for JSON parsing (optional but recommended)
   - Python 3.7+ (for mock webhook server)

3. **Network**:
   - Device can access network for webhook testing
   - Mock webhook server accessible from device

### Device Setup

1. Enable Developer Options:
   ```bash
   # Settings → About Phone → Tap "Build Number" 7 times
   ```

2. Enable USB Debugging:
   ```bash
   # Settings → System → Developer Options → USB Debugging
   ```

3. Connect device and verify:
   ```bash
   adb devices
   # Should show your device in "device" state
   ```

## Test Structure

```
testing/
├── README.md                    # This file
├── run_tests.sh                 # Main test orchestrator
├── setup.sh                     # Test environment setup
├── teardown.sh                  # Cleanup script
├── mock_webhook_server.py       # Mock HTTP server for testing
├── tests/
│   ├── 01_installation_test.sh
│   ├── 02_permissions_test.sh
│   ├── 03_service_test.sh
│   ├── 04_notification_capture_test.sh
│   ├── 05_webhook_crud_test.sh
│   ├── 06_trigger_matching_test.sh
│   ├── 07_webhook_execution_test.sh
│   ├── 08_database_test.sh
│   └── 09_ui_navigation_test.sh
├── helpers/
│   ├── adb_helpers.sh
│   ├── notification_generator.sh
│   └── json_parser.sh
└── logs/
    └── .gitkeep
```

## Running Tests

### Quick Start

```bash
cd testing
./run_tests.sh
```

### Run Specific Test

```bash
cd testing
./tests/04_notification_capture_test.sh
```

### With Mock Server

```bash
# Terminal 1: Start mock server
cd testing
python3 mock_webhook_server.py

# Terminal 2: Run tests
./run_tests.sh
```

## Test Descriptions

### 1. Installation Test (`01_installation_test.sh`)

Verifies that the APK can be installed and basic app info is correct.

**Tests:**
- APK installation succeeds
- Package name is correct
- App version is retrievable
- Main activity can be launched
- App doesn't crash on startup

**Exit Codes:**
- 0: All tests passed
- 1: Installation failed
- 2: Package not found
- 3: Launch failed

### 2. Permissions Test (`02_permissions_test.sh`)

Checks that all required permissions are declared and granted.

**Tests:**
- Internet permission granted
- Notification listener permission declared
- Boot receiver permission declared
- Foreground service permission declared
- Notification listener can be enabled
- Permissions persist after reboot

**Exit Codes:**
- 0: All permissions OK
- 1: Missing permission declaration
- 2: Permission grant failed

### 3. Service Test (`03_service_test.sh`)

Validates service lifecycle and foreground operation.

**Tests:**
- Service starts successfully
- Service runs in foreground
- Service survives app force-stop
- Service auto-starts on boot
- Service notification is visible
- Service doesn't consume excessive resources

**Exit Codes:**
- 0: Service working correctly
- 1: Service failed to start
- 2: Service not running in foreground
- 3: Boot persistence failed

### 4. Notification Capture Test (`04_notification_capture_test.sh`)

Tests notification capture and data extraction.

**Tests:**
- App captures test notifications
- Notification data correctly extracted:
  - Package name
  - Title
  - Text
  - Priority
  - Timestamp
  - Icon (base64)
- Multiple notifications handled
- Notification removal detected
- Active notification list updates

**Exit Codes:**
- 0: Notification capture working
- 1: Failed to capture notifications
- 2: Data extraction errors

### 5. Webhook CRUD Test (`05_webhook_crud_test.sh`)

Tests database operations for webhooks.

**Tests:**
- Create webhook via UI
- Read/list webhooks
- Update webhook configuration
- Delete webhook
- Add/remove custom headers
- Enable/disable webhooks
- Multiple webhooks support
- Data persistence after app restart

**Exit Codes:**
- 0: CRUD operations successful
- 1: Create failed
- 2: Read failed
- 3: Update failed
- 4: Delete failed

### 6. Trigger Matching Test (`06_trigger_matching_test.sh`)

Validates trigger rule matching logic.

**Tests:**
- Package name filtering
- Content regex matching
- Priority range filtering
- Combined rule matching
- Invalid regex handling
- Empty/null rules
- Live preview accuracy

**Exit Codes:**
- 0: Trigger matching correct
- 1: Matching logic error
- 2: Regex handling failed

### 7. Webhook Execution Test (`07_webhook_execution_test.sh`)

Tests HTTP webhook delivery.

**Tests:**
- Webhook sends on matching notification
- Correct JSON payload structure
- Custom headers included
- Icon base64 included
- Device info included
- Retry on failure
- Success/failure logging
- HTTP status code capture

**Exit Codes:**
- 0: Webhook execution working
- 1: Webhook not sent
- 2: Payload incorrect
- 3: Headers missing

### 8. Database Test (`08_database_test.sh`)

Validates database operations and constraints.

**Tests:**
- Database created correctly
- Auto-pruning (keeps 1000 logs)
- Foreign key constraints
- Transaction integrity
- Concurrent access handling
- Backup/restore capability

**Exit Codes:**
- 0: Database operations correct
- 1: Database errors
- 2: Pruning failed
- 3: Constraint violation

### 9. UI Navigation Test (`09_ui_navigation_test.sh`)

Tests UI screens and navigation.

**Tests:**
- All screens accessible
- Navigation between screens
- Back button handling
- Screen rotation
- Data display correctness
- Form validation
- Error handling

**Exit Codes:**
- 0: UI navigation working
- 1: Screen not found
- 2: Navigation failed
- 3: Data display error

## Test Output Format

All tests output JSON-formatted results for machine parsing:

```json
{
  "test_name": "installation_test",
  "timestamp": "2025-10-28T12:34:56Z",
  "status": "passed",
  "duration_ms": 1234,
  "tests": [
    {
      "name": "install_apk",
      "status": "passed",
      "message": "APK installed successfully"
    },
    {
      "name": "verify_package",
      "status": "passed",
      "message": "Package name verified"
    }
  ],
  "summary": {
    "total": 5,
    "passed": 5,
    "failed": 0,
    "skipped": 0
  }
}
```

## Troubleshooting

### Device Not Found

```bash
# Check connection
adb devices

# Restart ADB server
adb kill-server
adb start-server

# Verify device authorized
adb devices
```

### Notification Listener Not Enabled

```bash
# Manually enable via UI
adb shell am start -a android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS

# Or use accessibility service
adb shell settings put secure enabled_notification_listeners \
  com.notificationforwarder/.service.NotificationForwarderService
```

### Service Not Starting

```bash
# Check logcat
adb logcat | grep NotificationForwarder

# Force stop and restart
adb shell am force-stop com.notificationforwarder
adb shell am start-foreground-service \
  -n com.notificationforwarder/.service.NotificationForwarderService
```

### Webhook Not Received

```bash
# Check network connectivity
adb shell ping -c 4 8.8.8.8

# Check mock server logs
# Verify device can reach mock server IP

# Check app logs
adb logcat | grep WebhookExecutor
```

## Exit Codes Summary

- **0**: All tests passed
- **1**: Generic failure
- **2**: Setup/environment issue
- **3**: Device not found
- **4**: App not installed
- **5**: Service failure
- **10+**: Specific test failures

## Interpreting Results

### Success Criteria

A complete test run should:
1. All tests return exit code 0
2. JSON output shows 100% pass rate
3. No errors in logcat during tests
4. Mock server receives all expected webhooks
5. Database contains expected data
6. UI responds correctly to all interactions

### Common Issues

1. **Intermittent Failures**: Increase timeouts in test scripts
2. **Race Conditions**: Add sleep delays between operations
3. **Permission Denials**: Re-grant permissions manually
4. **Network Issues**: Use local mock server on same network

## Continuous Integration

These tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions integration
- name: Run Android Tests
  run: |
    cd testing
    ./setup.sh
    ./run_tests.sh
    exit_code=$?
    ./teardown.sh
    exit $exit_code
```

## Extending Tests

To add a new test:

1. Create `tests/XX_testname_test.sh`
2. Follow the template structure
3. Output JSON results
4. Add to `run_tests.sh`
5. Document in this README

## Contact & Support

For issues with the testing framework, check:
1. Logs in `testing/logs/`
2. ADB logcat output
3. Mock server logs
4. Device system logs

