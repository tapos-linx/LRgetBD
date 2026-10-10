# Source Adapters - LRgetBD

The application uses a modular source-adapter interface to allow for multiple data sources.

## Current Adapters

### MockSourceAdapter
- **Identity:** Internal mock for testing and demonstration.
- **Purpose:** Simulates a successful PDF download with validation and delay.
- **Status:** ACTIVE (for testing).

## Adapter Requirements
Each new adapter must implement the `SourceAdapter` interface:
```kotlin
interface SourceAdapter {
    val name: String
    suspend fun download(job: DownloadJob, targetFile: File): DownloadResult
}
```

## Security & Ethics
- No bypassing of CAPTCHA or authentication controls.
- Respectful rate limiting must be implemented in each adapter.
- Credentials must never be stored in plaintext.
