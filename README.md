# LRgetBD — Free-First Native Android Land Record Downloader

## Overview
LRgetBD is a native Android application designed for organizing, downloading, validating, and archiving Bangladesh land records (Khatians and Maps) by administrative area and survey type.

## Features
- **Cascading Area Selection:** Division → District → Upazila → Mouza.
- **Survey Categories:** CS, SA, RS, BS, BRS support.
- **Real-time Download Pipeline:** Persistent queue with background processing using WorkManager.
- **Validation:** PDF signature verification and SHA-256 checksum calculation.
- **Local Storage:** User-controlled archive folder via Android Storage Access Framework (SAF).
- **Bangla-First UI:** Fully translated interface for local usability.
- **Reports:** Comprehensive statistics on collection progress and success rates.

## Technology Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Database:** Room (for local metadata and queue)
- **Background Work:** WorkManager
- **Persistence:** DataStore (for settings)
- **Architecture:** MVVM (Model-View-ViewModel)

## Installation
1. Download the APK from the releases section.
2. Install the APK on your Android device.
3. Open the app and navigate to **Settings (সেটিংস)** to select your archive folder.
4. Navigate to **Area Selection (এলাকা নির্বাচন)** to start collecting records.

## Project Structure
- `com.example.model`: Data models and enums.
- `com.example.data`: Room database, DAO, and repositories.
- `com.example.ui`: Compose screens and ViewModels.
- `com.example.worker`: WorkManager implementation for background tasks.
- `com.example.util`: Utility classes for settings and file management.

## Limitations
- Currently uses a `MockSourceAdapter` for demonstration. Real-source integration requires verified and authorized API endpoints.
- PDF validation is limited to signature checking.

## License
TBD
