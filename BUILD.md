# Build Guide — LRgetBD

## Prerequisites
- Android Studio Ladybug (or later) / IntelliJ IDEA
- JDK 17 or higher
- Android SDK 36 (target/compileSdk)
- Gradle 9.x+ (configured via wrapper)

## Building from Command Line
```bash
# Clean and compile debug build
gradle :app:assembleDebug

# Run Unit and Robolectric Tests
gradle :app:testDebugUnitTest

# Run Lint checks
gradle :app:lintDebug
```

## Generated Artifacts
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Test Reports: `app/build/reports/tests/testDebugUnitTest/index.html`
- Lint Reports: `app/build/reports/lint-results-debug.html`

## Application ID and Package
- Application ID: `com.aistudio.lrgetbd.xfzjwy`
- Root Namespace: `com.example`
- Application Label: `LRgetBD`
