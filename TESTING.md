# Testing Matrix — LRgetBD

## Automated Unit & Robolectric Tests

### Executing Tests
```bash
gradle :app:testDebugUnitTest
```

### Test Suites Implemented
1. **`PdfValidationTest`**
   - Valid standard PDF structure with `%PDF-` and `%%EOF` markers.
   - Rejection of HTML login / error pages returned in place of PDFs.
   - Rejection of empty (0-byte) files.
   - Rejection of corrupted files lacking PDF headers.
   - Rejection of truncated streams missing trailer/EOF markers.
   - Accurate 64-character SHA-256 checksum generation.

2. **`AreaSelectionTest`**
   - Cascading filter resets across Division → District → Upazila.
   - Automatic selection of all Mouzas under an Upazila.
   - Deselect-all and select-all restorations.
   - Scalability testing across 0, 1, 7, 8, and 250+ Mouzas.
   - Classification of official vs imported catalogue entries.

3. **`QueueAndArchiveTest`**
   - Source adapter refusal of unauthorized bulk downloads without ministerial API key.
   - Safe isolation of test fixtures (`TestMockAdapter`).
   - Storage Access Framework and local fallback hierarchy:
     `LRgetBD/Bangladesh/<Division>/<District>/<Upazila>/<Mouza>/<SurveyType>/<RecordType>/`
   - Manifest generation and duplicate collision detection.
   - Metadata export fidelity in CSV and JSON formats.

4. **`ExampleRobolectricTest` & `ExampleUnitTest`**
   - Application string and context verification for `LRgetBD`.
