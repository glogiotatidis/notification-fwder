<!-- 0d4dea28-29c8-4b6d-837a-6ca138120bd0 1e56ee00-e12f-4340-8e18-d1bfaab21660 -->
# Android Notification Forwarder App

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room (SQLite wrapper with limit of 1000 records)
- **HTTP**: Retrofit + OkHttp
- **Async**: Kotlin Coroutines + Flow
- **DI**: Hilt (Dependency Injection)
- **Architecture**: MVVM with Repository pattern
- **Min SDK**: API 26 (Android 8.0)

## Project Structure

### Core Components

1. **NotificationListenerService** - Captures all incoming notifications
2. **TriggerMatcher** - Evaluates notification against webhook trigger rules
3. **WebhookExecutor** - Sends HTTP requests with retry logic
4. **Database Layer** - Room entities for webhooks, triggers, and history

### Database Schema

- `WebhookConfig` - Stores webhook URLs, headers, auth tokens
- `TriggerRule` - Stores matching criteria (app package, content pattern, priority, etc.)
- `NotificationLog` - History of sent webhooks (last 1000, auto-pruned)

### UI Screens (Compose)

1. **Home/Dashboard** - Overview and service status
2. **Webhook List** - View/edit/delete webhook configurations
3. **Webhook Editor** - Create/edit webhook with trigger builder
4. **Trigger Builder** - Live notification list with filter preview
5. **History Viewer** - Paginated list of sent webhooks

### Permissions Required

- `android.permission.POST_NOTIFICATIONS`
- `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`
- `android.permission.RECEIVE_BOOT_COMPLETED`
- `android.permission.INTERNET`
- `android.permission.FOREGROUND_SERVICE`

## Implementation Phases

### Phase 1: Project Setup

- Initialize Android project with Gradle Kotlin DSL
- Configure build.gradle with dependencies (Compose, Room, Retrofit, Hilt)
- Setup Hilt application class
- Create basic navigation structure
- Add manifest permissions

### Phase 2: Database Layer

- Define Room entities (WebhookConfig, TriggerRule, NotificationLog)
- Create DAOs with insert/update/delete/query operations
- Implement Repository pattern
- Add auto-pruning for NotificationLog (keep last 1000)

### Phase 3: Notification Service

- Implement NotificationListenerService
- Extract notification data (app, title, text, priority, timestamp)
- Implement boot receiver to restart service
- Add foreground service for persistent operation

### Phase 4: Webhook Execution

- Create WebhookExecutor with Retrofit/OkHttp
- Implement trigger matching logic (package name, content regex, priority filters)
- Add retry mechanism for failed requests
- Store webhook results in NotificationLog

### Phase 5: UI - Webhook Management

- Create WebhookListScreen (view all webhooks)
- Create WebhookEditorScreen (add/edit webhook URL, headers, auth)
- Implement ViewModel with Room repository
- Add validation for URLs and headers

### Phase 6: UI - Trigger Builder

- Fetch active notifications from NotificationListenerService
- Display notification list with real-time filtering
- Build trigger rule UI (package selector, content regex, priority picker)
- Live preview: highlight matching notifications as rules change

### Phase 7: UI - History Viewer

- Create NotificationLogScreen with paginated list
- Display: timestamp, notification details, webhook URL, status (success/failed)
- Add filtering/search capabilities
- Show HTTP response codes and error messages

### Phase 8: Build Configuration

- Setup release signing configuration in gradle
- Create keystore for app signing
- Configure ProGuard/R8 rules
- Add version management (versionCode/versionName)

### Phase 9: GitHub Workflows

- **Nightly workflow**: Build APK on every push to main
- Checkout code
- Setup JDK 17
- Build debug APK
- Upload artifact
- **Release workflow**: Build signed APK on release/tag
- Build release APK
- Sign with release keystore (from secrets)
- Create GitHub release with APK attachment

### Phase 10: Testing Framework (AI Agent Compatible)

- Create ADB-based testing scripts for real device verification
- Implement test notification generator (shell script + ADB)
- Build automated verification scripts for:
- App installation and permissions check
- Service lifecycle (boot persistence, foreground service, crash recovery)
- Notification capture and data extraction
- Webhook trigger matching logic
- HTTP webhook execution with mock server
- Database operations (CRUD, pruning, query accuracy)
- UI navigation and data display verification
- Add structured logging with JSON output for machine parsing
- Create test orchestrator script (run_tests.sh) with:
- Setup/teardown procedures
- Sequential test execution
- Pass/fail reporting with detailed logs
- Exit codes for CI integration
- Write comprehensive AI agent documentation:
- Prerequisites and device setup
- How to run tests
- How to interpret results
- Troubleshooting guide

## Key Files to Create

### Gradle Files

- `build.gradle.kts` (project level)
- `app/build.gradle.kts` (app level with signing config)
- `gradle.properties` (version and build settings)

### Application

- `NotificationForwarderApp.kt` - Hilt application
- `MainActivity.kt` - Compose entry point

### Service

- `NotificationForwarder.kt` - NotificationListenerService implementation
- `BootReceiver.kt` - Start service on boot

### Database

- `AppDatabase.kt`
- `entities/` - WebhookConfig, TriggerRule, NotificationLog
- `dao/` - DAOs for each entity
- `repository/` - Repository classes

### UI

- `ui/screens/` - Compose screens
- `ui/viewmodels/` - ViewModels
- `ui/components/` - Reusable UI components

### Networking

- `api/WebhookApi.kt` - Retrofit interface
- `WebhookExecutor.kt` - Execute webhooks with trigger matching

### GitHub Workflows

- `.github/workflows/nightly.yml`
- `.github/workflows/release.yml`

## Git Commit Strategy

- Commit after each phase completion
- Commit message format: `feat: <description>` or `chore: <description>`
- Example commits:

1. "chore: initial project setup with gradle and dependencies"
2. "feat: implement database layer with Room"
3. "feat: add NotificationListenerService"
4. "feat: implement webhook execution with trigger matching"
5. "feat: add webhook management UI"
6. "feat: implement trigger builder with live preview"
7. "feat: add notification history viewer"
8. "chore: configure release builds and signing"
9. "ci: add GitHub workflows for nightly and release builds"

### To-dos

- [ ] Initialize Android project with Gradle, dependencies (Compose, Room, Retrofit, Hilt), and manifest permissions
- [ ] Create Room database with entities, DAOs, and repositories for WebhookConfig, TriggerRule, and NotificationLog
- [ ] Implement NotificationListenerService to capture notifications and BootReceiver for auto-start
- [ ] Build webhook execution engine with trigger matching logic and HTTP client using Retrofit
- [ ] Create Compose UI for webhook list and editor with ViewModels
- [ ] Implement trigger builder UI with live notification preview and filter matching
- [ ] Build notification history viewer with pagination and filtering
- [ ] Configure Gradle for release builds with signing and ProGuard rules
- [ ] Create GitHub Actions workflows for nightly and release builds