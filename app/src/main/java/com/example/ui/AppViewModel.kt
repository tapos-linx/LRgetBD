package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.LRgetBDApplication
import com.example.data.AreaRepository
import com.example.model.CatalogueSource
import com.example.model.District
import com.example.model.Division
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.model.Mouza
import com.example.model.RecordType
import com.example.model.SurveyType
import com.example.model.Upazila
import com.example.worker.DownloadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _selectedSurveyType = MutableStateFlow(SurveyType.RS)
    val selectedSurveyType = _selectedSurveyType.asStateFlow()

    private val _selectedRecordType = MutableStateFlow(RecordType.KHATIAN)
    val selectedRecordType = _selectedRecordType.asStateFlow()

    // Batch configuration: default 7 Mouzas per batch as mandated
    private val _batchSize = MutableStateFlow(7)
    val batchSize = _batchSize.asStateFlow()

    // Dev test mode switch (Explicitly labeled for testing/inspection)
    private val _isDevTestMode = MutableStateFlow(false)
    val isDevTestMode = _isDevTestMode.asStateFlow()

    // Notification / UI alert message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setDevTestMode(enabled: Boolean) {
        _isDevTestMode.value = enabled
    }

    fun setBatchSize(size: Int) {
        if (size in 1..50) {
            _batchSize.value = size
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
            // Automatically select all verified Mouzas under this Upazila
            _selectedMouzas.value = availableMouzas.map { it.id }.toSet()
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

    fun setSurveyType(type: SurveyType) {
        _selectedSurveyType.value = type
    }

    fun setRecordType(type: RecordType) {
        _selectedRecordType.value = type
    }

    /**
     * Start Collection Pipeline:
     * Splits selected Mouzas into batches (default 7) and schedules background execution.
     */
    fun startCollection() {
        val upazila = _selectedUpazila.value ?: return
        val mouzaIds = _selectedMouzas.value
        if (mouzaIds.isEmpty()) return

        val division = _selectedDivision.value
        val district = _selectedDistrict.value
        val survey = _selectedSurveyType.value
        val record = _selectedRecordType.value
        val testMode = _isDevTestMode.value
        val allAvailableMouzas = AreaRepository.getMouzas(upazila.id).associateBy { it.id }

        viewModelScope.launch {
            val batchId = UUID.randomUUID().toString().take(8)
            val jobsToInsert = mouzaIds.mapNotNull { mouzaId ->
                val mouza = allAvailableMouzas[mouzaId] ?: return@mapNotNull null
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
                    surveyType = survey,
                    recordType = record,
                    recordIdentifier = "ALL",
                    sourceName = if (testMode) "TEST_FIXTURE_DEV" else "EPORCHA_BD",
                    status = DownloadStatus.PENDING,
                    batchId = batchId
                )
            }

            val insertedIds = dao.insertJobs(jobsToInsert)

            // Enqueue WorkManager with conservative concurrency constraints
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Process sequentially in batches of batchSize (default 7)
            val batchChunkSize = _batchSize.value
            insertedIds.chunked(batchChunkSize).forEachIndexed { chunkIndex, chunkIds ->
                chunkIds.forEach { id ->
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

            _uiMessage.value = "${mouzaIds.size} টি মৌজার জন্য সংগ্রহ কার্যসারি শুরু হয়েছে。"
        }
    }

    /**
     * Pause all active jobs in the queue
     */
    fun pauseQueue() {
        viewModelScope.launch {
            dao.pauseActiveJobs()
            workManager.cancelAllWorkByTag("lrgetbd_batch")
            _uiMessage.value = "চলমান সংগ্রহ স্থগিত করা হয়েছে。"
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
}
