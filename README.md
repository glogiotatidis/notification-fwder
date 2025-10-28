# Notification Forwarder for Android

An Android application that captures all incoming notifications and forwards them to configurable webhooks based on trigger rules. Built with Kotlin, Jetpack Compose, and modern Android architecture.

## Features

- 📱 **Notification Capture**: Monitors all incoming notifications via NotificationListenerService
- 🔔 **Auto-start on Boot**: Service automatically starts when device boots
- 🎯 **Trigger Rules**: Configure webhooks with flexible matching rules:
  - Package name filtering
  - Content pattern matching (regex)
  - Priority range filtering
- 🌐 **HTTP Webhooks**: Send notifications to any HTTP endpoint
- 🔑 **Custom Headers**: Add authentication headers and custom headers per webhook
- 📊 **Live Preview**: See which notifications match your trigger rules in real-time
- 📜 **History**: View last 1000 webhook executions with success/failure status
- 🖼️ **Icon Support**: Notification and app icons included as base64 in webhook payload
- 🎨 **Modern UI**: Built with Jetpack Compose and Material 3

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Database**: Room (SQLite)
- **HTTP**: Retrofit + OkHttp
- **DI**: Hilt
- **Architecture**: MVVM with Repository pattern
- **Min SDK**: API 26 (Android 8.0)

## Webhook Payload

Webhooks receive a JSON payload with the following structure:

```json
{
  "packageName": "com.example.app",
  "appName": "Example App",
  "title": "Notification Title",
  "text": "Notification text content",
  "subText": "Optional sub text",
  "bigText": "Optional expanded text",
  "priority": 0,
  "timestamp": 1234567890000,
  "iconBase64": "iVBORw0KGgoAAAANS...",
  "device": {
    "androidVersion": "13",
    "deviceModel": "Pixel 7",
    "deviceManufacturer": "Google"
  }
}
```

## Building the APK

### Prerequisites

- JDK 17
- Android SDK (API 26+)
- Gradle 8.2+

### Build Debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK

1. Create a release keystore (if you don't have one):

```bash
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

2. Set environment variables:

```bash
export KEYSTORE_FILE=release.keystore
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=release
export KEY_PASSWORD=your_key_password
```

3. Build release APK:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions CI/CD

The project includes two GitHub Actions workflows:

### Nightly Build

Triggers on every push to `main`/`master` branch:
- Builds debug APK
- Uploads artifact for 30 days

### Release Build

Triggers on git tags (`v*`) or GitHub releases:
- Builds signed release APK
- Uploads artifact for 90 days
- Attaches APK to GitHub release

#### Setup GitHub Secrets

For release builds, add these secrets to your repository:

- `KEYSTORE_FILE_BASE64`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

To encode your keystore:
```bash
base64 -i release.keystore | tr -d '\n'
```

## Installation

1. Download the APK from releases or build it yourself
2. Install on your Android device
3. Open the app and grant notification access:
   - Settings → Apps → Special app access → Notification access
   - Enable "Notification Forwarder"
4. Configure your webhooks and trigger rules

## Usage

### Adding a Webhook

1. Go to the **Webhooks** tab
2. Tap the **+** button
3. Enter the webhook URL (must be https:// or http://)
4. (Optional) Add custom headers for authentication
5. Configure trigger rules:
   - **Package Filter**: Match specific apps (e.g., "com.whatsapp")
   - **Content Filter**: Regex pattern to match notification content
   - **Priority Range**: Filter by notification priority (-2 to 2)
6. Use the **Live Preview** to test your rules
7. Save the webhook

### Viewing History

1. Go to the **History** tab
2. Filter by success/failed status
3. View detailed information about each webhook execution
4. Clear history when needed

## Testing

See [testing/README.md](testing/README.md) for comprehensive testing documentation and AI agent instructions.

## Permissions

- **INTERNET**: Send HTTP requests
- **POST_NOTIFICATIONS**: Display foreground service notification
- **BIND_NOTIFICATION_LISTENER_SERVICE**: Access notifications
- **RECEIVE_BOOT_COMPLETED**: Auto-start on device boot
- **FOREGROUND_SERVICE**: Run persistent background service

## Privacy

- All data is stored locally on your device
- No data is collected or sent to any third party
- Webhook URLs and trigger rules are stored in local SQLite database
- Only notifications matching your trigger rules are sent to configured webhooks

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

