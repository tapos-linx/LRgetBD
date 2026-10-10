package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Division(val id: Int, val name: String, val bnName: String)

@Serializable
data class District(val id: Int, val divisionId: Int, val name: String, val bnName: String)

@Serializable
data class Upazila(val id: Int, val districtId: Int, val name: String, val bnName: String)

@Serializable
data class Mouza(
    val id: String,
    val upazilaId: Int,
    val name: String,
    val bnName: String,
    val jlNumber: String
)

enum class SurveyType {
    CS, SA, RS, BS, BRS, OTHER
}

enum class RecordType {
    KHATIAN, MAP
}

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    VALIDATING,
    VALIDATED,
    FAILED,
    COMPLETED,
    CANCELLED,
    PAUSED,
    DUPLICATE,
    NOT_FOUND,
    AUTH_REQUIRED
}

@Entity(tableName = "download_jobs")
@Serializable
data class DownloadJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val divisionId: Int,
    val districtId: Int,
    val upazilaId: Int,
    val mouzaId: String,
    val surveyType: SurveyType,
    val recordType: RecordType,
    val recordIdentifier: String, // e.g. Khatian Number
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val checksum: String? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0
)
