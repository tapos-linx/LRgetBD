package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LRgetBDApplication
import com.example.data.AreaRepository
import com.example.model.District
import com.example.model.Division
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.model.Mouza
import com.example.model.RecordType
import com.example.model.SurveyType
import com.example.model.Upazila
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as LRgetBDApplication).database
    private val dao = database.downloadJobDao()

    val allJobs = dao.getAllJobs().stateIn(
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

    fun selectDivision(division: Division?) {
        _selectedDivision.value = division
        _selectedDistrict.value = null
        _selectedUpazila.value = null
        _selectedMouzas.value = emptySet()
    }

    fun selectDistrict(district: District?) {
        _selectedDistrict.value = district
        _selectedUpazila.value = null
        _selectedMouzas.value = emptySet()
    }

    fun selectUpazila(upazila: Upazila?) {
        _selectedUpazila.value = upazila
        _selectedMouzas.value = emptySet()
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

    fun startCollection() {
        val upazila = _selectedUpazila.value ?: return
        val mouzas = _selectedMouzas.value
        if (mouzas.isEmpty()) return

        viewModelScope.launch {
            mouzas.forEach { mouzaId ->
                val job = DownloadJob(
                    divisionId = _selectedDivision.value?.id ?: 0,
                    districtId = _selectedDistrict.value?.id ?: 0,
                    upazilaId = upazila.id,
                    mouzaId = mouzaId,
                    surveyType = _selectedSurveyType.value,
                    recordType = RecordType.KHATIAN,
                    recordIdentifier = "ALL",
                    status = DownloadStatus.PENDING
                )
                val jobId = dao.insertJob(job)
                
                // Enqueue WorkManager
                val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.DownloadWorker>()
                    .setInputData(androidx.work.workDataOf("jobId" to jobId))
                    .build()
                androidx.work.WorkManager.getInstance(getApplication()).enqueue(workRequest)
            }
        }
    }
}
