# LRgetBD — Free-First Native Android Land Record Downloader

LRgetBD is a native Android application built with Kotlin, Jetpack Compose, Material 3, Room, and WorkManager for organizing, downloading, validating, and archiving Bangladesh land records (Khatians and Maps) by administrative area and survey type.

## Core Features (Phase 1)
- **Bangla-First Interface:** All primary user workflows, notifications, and status summaries are localized in Bangla.
- **Administrative Cascading Hierarchy:**
  - Division → District → Upazila → Mouza selection.
  - Automatic selection of all Mouzas when an Upazila is selected.
  - Deselect-all and select-all controls.
  - Scales efficiently across 0, 1, 7, 8, and 200+ Mouzas.
- **Survey & Record Types:** CS, SA, RS, BS, BRS support for Khatians and Mouza Maps.
- **Persistent Queue Orchestration:**
  - Room database persistence for all download jobs.
  - Batching with default batch size of 7 Mouzas.
  - Pause, Resume, Cancel, and Retry controls.
- **PDF Validation Engine:**
  - Rejects HTML login/error redirects.
  - Validates magic header `%PDF-` and EOF trailer `%%EOF`.
  - Computes SHA-256 checksums and prevents silent duplicates.
- **Local Archive Management:**
  - Android Storage Access Framework (SAF) folder picker with local app-scoped fallback.
  - Structured storage: `LRgetBD/Bangladesh/Division/District/Upazila/Mouza/SurveyType/RecordType/`
  - Per-directory `manifest.json` generation.
  - CSV and JSON metadata export.
- **Security & Data Ethics:**
  - Never fabricates fake PDFs or mocks success in production.
  - Transparent `SOURCE_UNVERIFIED` indicator when official government API keys are absent.

## Testing & CI
Run automated tests locally:
```bash
gradle :app:testDebugUnitTest
```
Build debug APK:
```bash
gradle :app:assembleDebug
```
GitHub Actions workflow is configured in `.github/workflows/android.yml`.
