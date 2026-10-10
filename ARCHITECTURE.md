# Architecture - LRgetBD

## Data Layer
- **Room Database:** Stores `DownloadJob` entities representing the collection queue and history.
- **AreaRepository:** Provides static data for administrative divisions in Bangladesh.
- **SettingsManager:** Uses DataStore to persist user preferences like the SAF archive URI.

## Domain Layer
- **SourceAdapter:** Interface for defining how to resolve and download records from various sources.
- **DownloadWorker:** A `CoroutineWorker` that handles the actual download, validation, and file movement logic in the background.

## UI Layer
- **Jetpack Compose:** Modern declarative UI.
- **Navigation Compose:** Handles screen transitions.
- **AppViewModel:** Central state management for the entire application.

## Download Pipeline
1. User selects Mouzas and survey type.
2. `AppViewModel` inserts `PENDING` jobs into Room.
3. `WorkManager` enqueues `DownloadWorker` for each job.
4. `DownloadWorker` transitions status to `DOWNLOADING`.
5. Files are downloaded to cache, validated (PDF signature + checksum), and then moved to the final storage.
6. Status is updated to `VALIDATED` or `FAILED`.
