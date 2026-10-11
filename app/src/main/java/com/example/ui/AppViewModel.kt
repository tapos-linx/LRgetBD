package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.LRgetBDApplication
import com.example.data.AreaRepository
import com.example.model.District
import com.example.model.Division
import com.example.model.DocumentTypeItem
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.model.Mouza
import com.example.model.RecordType
import com.example.model.SurveyType
import com.example.model.Upazila
import com.example.worker.DownloadWorker
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.util.ArchiveFileManager
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as LRgetBDApplication).database
    private val dao = database.downloadJobDao()
    private val workManager = WorkManager.getInstance(application)

    val allJobs: StateFlow<List<DownloadJob>> = dao.getAllJobs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Area Selection State
    private val _selectedDivision = MutableStateFlow<Division?>(null)
    val selectedDivision = _selectedDivision.asStateFlow()

    private val _selectedDistrict = MutableStateFlow<District?>(null)
    val selectedDistrict = _selectedDistrict.asStateFlow()

    private val _selectedUpazila = MutableStateFlow<Upazila?>(null)
    val selectedUpazila = _selectedUpazila.asStateFlow()

    private val _selectedMouzas = MutableStateFlow<Set<String>>(emptySet())
    val selectedMouzas = _selectedMouzas.asStateFlow()

    // Auto-select all Mouzas toggle (Default: true)
    private val _autoSelectMouzas = MutableStateFlow(true)
    val autoSelectMouzas = _autoSelectMouzas.asStateFlow()

    // Multi-select Document Types (CS, SA, RS, BS, BRS, Mutation, Map)
    private val _selectedDocumentTypes = MutableStateFlow<Set<String>>(
        setOf("RS_KHATIAN", "CS_KHATIAN", "SA_KHATIAN")
    )
    val selectedDocumentTypes = _selectedDocumentTypes.asStateFlow()

    // Batch configuration: default 7 Mouzas per batch as mandated
    private val _batchSize = MutableStateFlow(7)
    val batchSize = _batchSize.asStateFlow()

    // Upazila Mouza Directory Density (50, 100, 200 mouzas)
    private val _mouzaDensity = MutableStateFlow(AreaRepository.getMouzaDensity())
    val mouzaDensity = _mouzaDensity.asStateFlow()

    // Dev test mode switch (Explicitly labeled for testing/inspection)
    private val _isDevTestMode = MutableStateFlow(false)
    val isDevTestMode = _isDevTestMode.asStateFlow()

    // Notification / UI alert message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage = _uiMessage.asStateFlow()

    // Master folder ZIP export state
    private val _isExportingMasterZip = MutableStateFlow(false)
    val isExportingMasterZip = _isExportingMasterZip.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setMouzaDensity(count: Int) {
        _mouzaDensity.value = count
        AreaRepository.setMouzaDensity(count)
        val currentUpazila = _selectedUpazila.value
        if (currentUpazila != null) {
            val availableMouzas = AreaRepository.getMouzas(currentUpazila.id)
            if (_autoSelectMouzas.value) {
                _selectedMouzas.value = availableMouzas.map { it.id }.toSet()
            }
        }
    }

    fun setDevTestMode(enabled: Boolean) {
        _isDevTestMode.value = enabled
    }

    fun setBatchSize(size: Int) {
        if (size in 1..50) {
            _batchSize.value = size
        }
    }

    fun setAutoSelectMouzas(enabled: Boolean) {
        _autoSelectMouzas.value = enabled
        if (enabled && _selectedUpazila.value != null) {
            val availableMouzas = AreaRepository.getMouzas(_selectedUpazila.value!!.id)
            _selectedMouzas.value = availableMouzas.map { it.id }.toSet()
        }
    }

    /**
     * Division selection: Resets District, Upazila, and Mouzas
     */
    fun selectDivision(division: Division?) {
        _selectedDivision.value = division
        _selectedDistrict.value = null
        _selectedUpazila.value = null
        _selectedMouzas.value = emptySet()
    }

    /**
     * District selection: Resets Upazila and Mouzas
     */
    fun selectDistrict(district: District?) {
        _selectedDistrict.value = district
        _selectedUpazila.value = null
        _selectedMouzas.value = emptySet()
    }

    /**
     * Upazila selection:
     * Mandate: "Automatic selection of all verified Mouzas when an Upazila is selected"
     */
    fun selectUpazila(upazila: Upazila?) {
        _selectedUpazila.value = upazila
        if (upazila != null) {
            val availableMouzas = AreaRepository.getMouzas(upazila.id)
            if (_autoSelectMouzas.value) {
                // Automatically select all verified Mouzas under this Upazila
                _selectedMouzas.value = availableMouzas.map { it.id }.toSet()
            } else {
                _selectedMouzas.value = emptySet()
            }
        } else {
            _selectedMouzas.value = emptySet()
        }
    }

    fun toggleMouzaSelection(mouzaId: String) {
        val current = _selectedMouzas.value.toMutableSet()
        if (current.contains(mouzaId)) {
            current.remove(mouzaId)
        } else {
            current.add(mouzaId)
        }
        _selectedMouzas.value = current
    }

    fun selectAllMouzas(mouzas: List<Mouza>) {
        _selectedMouzas.value = mouzas.map { it.id }.toSet()
    }

    fun deselectAllMouzas() {
        _selectedMouzas.value = emptySet()
    }

    /**
     * Document Types Granular Selection Controls
     */
    fun toggleDocumentType(typeId: String) {
        val current = _selectedDocumentTypes.value.toMutableSet()
        if (current.contains(typeId)) {
            if (current.size > 1) { // Keep at least 1 document type
                current.remove(typeId)
            } else {
                _uiMessage.value = "কমপক্ষে একটি রেকর্ডের ধরণ নির্বাচিত থাকতে হবে।"
            }
        } else {
            current.add(typeId)
        }
        _selectedDocumentTypes.value = current
    }

    fun selectAllDocumentTypes() {
        _selectedDocumentTypes.value = DocumentTypeItem.ALL_AVAILABLE.map { it.id }.toSet()
    }

    fun deselectAllDocumentTypes() {
        // Keep default RS Khatian
        _selectedDocumentTypes.value = setOf("RS_KHATIAN")
    }

    /**
     * Start Collection Pipeline:
     * Generates jobs for every selected Mouza multiplied by every selected Document Type,
     * maintaining controlled network concurrency (max 3 concurrent workers) and chunked batches.
     */
    fun startCollection() {
        val upazila = _selectedUpazila.value ?: return
        val mouzaIds = _selectedMouzas.value
        if (mouzaIds.isEmpty()) {
            _uiMessage.value = "অনুগ্রহ করে কমপক্ষে একটি মৌজা নির্বাচন করুন।"
            return
        }

        val docTypeIds = _selectedDocumentTypes.value
        if (docTypeIds.isEmpty()) {
            _uiMessage.value = "অনুগ্রহ করে কমপক্ষে একটি ডকুমেন্টের ধরণ নির্বাচন করুন।"
            return
        }

        val division = _selectedDivision.value
        val district = _selectedDistrict.value
        val testMode = _isDevTestMode.value
        val allAvailableMouzas = AreaRepository.getMouzas(upazila.id).associateBy { it.id }
        val docTypeMap = DocumentTypeItem.ALL_AVAILABLE.associateBy { it.id }

        viewModelScope.launch {
            val batchId = UUID.randomUUID().toString().take(8)
            val batchChunkSize = _batchSize.value
            
            // Process Mouzas in chunks of batchSize (default 7)
            mouzaIds.toList().chunked(batchChunkSize).forEach { mouzaChunk ->
                val jobsToInsert = mutableListOf<DownloadJob>()
                mouzaChunk.forEach { mouzaId ->
                    val mouza = allAvailableMouzas[mouzaId] ?: return@forEach
                    docTypeIds.forEach { docTypeId ->
                        val docType = docTypeMap[docTypeId] ?: return@forEach
                        jobsToInsert.add(
                            DownloadJob(
                                divisionId = division?.id ?: 0,
                                divisionName = division?.bnName ?: "",
                                districtId = district?.id ?: 0,
                                districtName = district?.bnName ?: "",
                                upazilaId = upazila.id,
                                upazilaName = upazila.bnName,
                                mouzaId = mouza.id,
                                mouzaName = mouza.bnName,
                                jlNumber = mouza.jlNumber,
                                surveyType = docType.surveyType,
                                recordType = docType.recordType,
                                recordIdentifier = "ALL",
                                sourceName = if (testMode) "TEST_FIXTURE_DEV" else "EPORCHA_BD",
                                status = DownloadStatus.PENDING,
                                batchId = batchId
                            )
                        )
                    }
                }

                val insertedIds = dao.insertJobs(jobsToInsert)

                // Enqueue WorkManager with conservative concurrency constraints
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                insertedIds.forEach { id ->
                    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setConstraints(constraints)
                        .setInputData(
                            workDataOf(
                                "jobId" to id,
                                "devTestMode" to testMode
                            )
                        )
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                        .addTag("lrgetbd_batch_$batchId")
                        .build()

                    workManager.enqueue(request)
                }
            }

            _uiMessage.value = "${mouzaIds.size} টি মৌজার জন্য রেকর্ডের সংগ্রহ কার্যসারি শুরু হয়েছে (প্রতি ব্যাচে $batchChunkSize টি মৌজা)।"
        }
    }

    /**
     * Master Upazila Extraction:
     * Selects all mouzas belonging to the chosen upazila, ensures document types are active,
     * and downloads all documents in one go into the master folder LR_BD_Records_[UpazilaName].
     */
    fun downloadEntireUpazila() {
        val upazila = _selectedUpazila.value ?: run {
            _uiMessage.value = "অনুগ্রহ করে প্রথমে একটি উপজেলা নির্বাচন করুন।"
            return
        }
        val allMouzas = AreaRepository.getMouzas(upazila.id)
        _selectedMouzas.value = allMouzas.map { it.id }.toSet()
        if (_selectedDocumentTypes.value.isEmpty()) {
            selectAllDocumentTypes()
        }
        startCollection()
    }

    /**
     * Pause all active jobs in the queue
     */
    fun pauseQueue() {
        viewModelScope.launch {
            dao.pauseActiveJobs()
            workManager.cancelAllWorkByTag("lrgetbd_batch")
            _uiMessage.value = "চলমান সংগ্রহ স্থগিত করা হয়েছে।"
        }
    }

    /**
     * Resume all paused jobs in the queue
     */
    fun resumeQueue() {
        viewModelScope.launch {
            dao.resumePausedJobs()
            // Re-enqueue pending jobs
            val pendingJobs = dao.getPendingJobs(limit = 100)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            pendingJobs.forEach { job ->
                val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setConstraints(constraints)
                    .setInputData(
                        workDataOf(
                            "jobId" to job.id,
                            "devTestMode" to _isDevTestMode.value
                        )
                    )
                    .build()
                workManager.enqueue(request)
            }
            _uiMessage.value = "স্থগিত কার্যসারি পুনরায় চালু করা হয়েছে।"
        }
    }

    /**
     * Cancel all active and pending jobs in the queue
     */
    fun cancelQueue() {
        viewModelScope.launch {
            dao.cancelActiveJobs()
            workManager.cancelAllWork()
            _uiMessage.value = "কার্যসারি বাতিল করা হয়েছে।"
        }
    }

    /**
     * Retry failed and incomplete jobs
     */
    fun retryFailedJobs() {
        viewModelScope.launch {
            val count = dao.retryFailedJobs()
            if (count > 0) {
                val pendingJobs = dao.getPendingJobs(limit = 100)
                pendingJobs.forEach { job ->
                    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(
                            workDataOf(
                                "jobId" to job.id,
                                "devTestMode" to _isDevTestMode.value
                            )
                        )
                        .build()
                    workManager.enqueue(request)
                }
                _uiMessage.value = "$count টি ব্যর্থ কাজ পুনরায় শুরু করা হয়েছে।"
            } else {
                _uiMessage.value = "কোনো ব্যর্থ কাজ অবশিষ্ট নেই।"
            }
        }
    }

    /**
     * Retry authentication-required jobs
     */
    fun retryAuthRequiredJobs() {
        viewModelScope.launch {
            val count = dao.retryAuthRequiredJobs()
            if (count > 0) {
                val pendingJobs = dao.getPendingJobs(limit = 100)
                pendingJobs.forEach { job ->
                    val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(
                            workDataOf(
                                "jobId" to job.id,
                                "devTestMode" to _isDevTestMode.value
                            )
                        )
                        .build()
                    workManager.enqueue(request)
                }
                _uiMessage.value = "$count টি লগইন-প্রয়োজনীয় কাজ পুনরায় শুরু করা হয়েছে।"
            }
        }
    }

    /**
     * Packages and saves the Master Folder straight to Phone Storage (Downloads/LRgetBD_Records),
     * and triggers system save / share sheet.
     */
    fun exportAndDownloadMasterFolder(
        context: Context,
        upazilaName: String? = null,
        onSuccess: ((File) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isExportingMasterZip.value = true
            try {
                val archiveManager = ArchiveFileManager(context)
                val targetUpazila = upazilaName ?: _selectedUpazila.value?.bnName ?: _selectedUpazila.value?.name
                val result = archiveManager.exportMasterFolderDirectlyToPhoneStorage(targetUpazila)
                val zipFile = result.zipFile

                _uiMessage.value = "ফোন স্টোরেজে সংরক্ষিত: Downloads > LRgetBD > ${zipFile.name} (${zipFile.length() / 1024} KB)"
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "ফোন স্টোরেজে সেভ হয়েছে:\nDownloads > LRgetBD > ${zipFile.name}",
                        Toast.LENGTH_LONG
                    ).show()

                    onSuccess?.invoke(zipFile)
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            zipFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "LRgetBD Master Folder - ${zipFile.name}")
                            putExtra(Intent.EXTRA_TEXT, "LRgetBD Master Folder: Phone Storage/Download/LRgetBD/${zipFile.name}")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "মাস্টার ফোল্ডার ডাউনলোড / সেভ করুন"))
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _uiMessage.value = "মাস্টার ফোল্ডার সংরক্ষণে ত্রুটি: ${e.localizedMessage}"
            } finally {
                _isExportingMasterZip.value = false
            }
        }
    }

    /**
     * Saves the master folder and all mouza records straight to phone storage without opening share sheet.
     */
    fun saveMasterFolderStraightToPhoneStorage(
        context: Context,
        upazilaName: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isExportingMasterZip.value = true
            try {
                val archiveManager = ArchiveFileManager(context)
                val targetUpazila = upazilaName ?: _selectedUpazila.value?.bnName ?: _selectedUpazila.value?.name
                val result = archiveManager.exportMasterFolderDirectlyToPhoneStorage(targetUpazila)
                val zipFile = result.zipFile

                _uiMessage.value = "সরাসরি ফোন মেমোরিতে সেভ হয়েছে: ${result.storagePathDescription} (${zipFile.name})"
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "ফোন স্টোরেজে সংরক্ষিত হয়েছে:\n${result.storagePathDescription}/${zipFile.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                _uiMessage.value = "ফোন স্টোরেজে সেভ করতে সমস্যা: ${e.localizedMessage}"
            } finally {
                _isExportingMasterZip.value = false
            }
        }
    }
}
