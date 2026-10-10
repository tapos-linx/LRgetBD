# Architecture — LRgetBD

## Layered Design Overview

```
+-------------------------------------------------------------+
|                 Jetpack Compose UI (M3)                    |
|  HomeScreen | AreaSelectionScreen | QueueScreen | Settings  |
+-------------------------------------------------------------+
                              |
+-------------------------------------------------------------+
|                  ViewModel State Layer                      |
|                        AppViewModel                         |
+-------------------------------------------------------------+
            |                                    |
+------------------------+          +-------------------------+
| Data Persistence Layer |          | Background Worker Layer |
| AppDatabase (Room)     |          | WorkManager             |
| DownloadJobDao         |          | DownloadWorker          |
| AreaRepository         |          +-------------------------+
| SettingsManager        |                       |
+------------------------+          +-------------------------+
            |                       | Modular Source Adapters |
+------------------------+          | OfficialEporchaAdapter  |
| Storage & Validation   |          | TestMockAdapter (test)  |
| ArchiveFileManager     |          +-------------------------+
| PdfValidator           |
+------------------------+
```

## Archive Storage Layout
```
LRgetBD/
└── Bangladesh/
    └── <Division>/
        └── <District>/
            └── <Upazila>/
                └── <Mouza_JL>/
                    └── <SurveyType>/
                        └── <RecordType>/
                            ├── Record_ALL_a1b2c3d4.pdf
                            └── manifest.json
```
