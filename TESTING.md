# Testing Documentation

## Test Coverage Summary

### ✅ Unit Tests (Passing)

Located in `app/src/test/java/`, these tests run quickly without an emulator:

1. **TriggerMatcherTest.kt** - 18 tests
   - Simple text matching vs regex matching
   - Package name filtering
   - Content pattern matching
   - Priority range filtering
   - Combined rules
   - Null/empty pattern handling
   - Case insensitivity
   - BigText search
   - Invalid regex handling

2. **WebhookRepositoryTest.kt** - 10 tests
   - CRUD operations
   - Flow emissions
   - Enabled webhook filtering
   - Trigger rule associations

3. **NotificationLogRepositoryTest.kt** - 8 tests
   - Insert and auto-pruning
   - Status filtering
   - Count tracking
   - Batch operations

4. **WebhookPayloadTest.kt** - 3 tests
   - Serialization
   - Deserialization
   - Null value handling

**Total: 39 unit tests**

### 🧪 Instrumented Tests (Device/Emulator Required)

Located in `app/src/androidTest/java/`:

1. **DatabaseIntegrationTest.kt** - 8 tests
   - Room database operations
   - Foreign key constraints
   - Cascade deletes
   - Auto-pruning
   - Data persistence

2. **TriggerMatcherInstrumentedTest.kt** - 3 tests
   - Complex regex patterns
   - Multiple rules
   - Combined conditions

**Total: 11 instrumented tests**

### 🤖 ADB-Based System Tests

Located in `testing/tests/`, these test the complete app on a real device:

1. Installation Test
2. Permissions Test
3. Service Test
4. Notification Capture Test
5. Webhook CRUD Test
6. Trigger Matching Test
7. Webhook Execution Test
8. Database Test
9. UI Navigation Test

**Total: 9 system integration tests**

## Running Tests

### Run Unit Tests Locally

```bash
./gradlew testDebugUnitTest
```

View HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`

### Run Instrumented Tests Locally

```bash
# Start an emulator first
./gradlew connectedDebugAndroidTest
```

View HTML report: `app/build/reports/androidTests/connected/index.html`

### Run ADB System Tests

```bash
cd testing
./run_tests.sh
```

View results: `testing/logs/test_results_*.json`

## CI/CD Test Integration

### Nightly Build Workflow

**Triggers:** Every push to `main` branch

**Tests Executed:**
- ✅ Unit tests (`testDebugUnitTest`)
- ⏭️ Instrumented tests (skipped for speed)
- ✅ APK build verification

**Duration:** ~7-8 minutes

### Release Build Workflow

**Triggers:** Git tags (`v*`), releases, or manual dispatch

**Tests Executed:**
- ✅ Unit tests (`testReleaseUnitTest`)
- ✅ Instrumented tests (on release builds)
- ✅ Signed APK build

**Duration:** ~15-20 minutes (with instrumented tests)

## Test Artifacts

After each workflow run, the following artifacts are available:

1. **test-results-{sha}** - JUnit XML and HTML reports
2. **app-debug-{sha}** - Debug APK (30-day retention)
3. **app-release-{version}** - Release APK (90-day retention)

Download artifacts:
```bash
gh run download <run-id>
```

## Current Test Status

**Unit Tests:** ✅ All 39 tests passing  
**Build:** ✅ APK builds successfully (8.7 MB)  
**CI/CD:** ✅ Nightly workflow passing  
**System Tests:** ⏳ Ready to run on device

## Next Steps

1. ✅ Unit tests passing in CI/CD
2. ✅ APK building successfully
3. ⏭️ Run ADB tests on real device
4. ⏭️ Fix instrumented tests (optional)
5. ⏭️ Test on real Android device

