package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class Division(
    val id: Int,
    val name: String,
    val bnName: String
)

@Serializable
data class District(
    val id: Int,
    val divisionId: Int,
    val name: String,
    val bnName: String
)

@Serializable
data class Upazila(
    val id: Int,
    val districtId: Int,
    val name: String,
    val bnName: String
)

@Serializable
data class Mouza(
    val id: String,
    val upazilaId: Int,
    val name: String,
    val bnName: String,
    val jlNumber: String,
    val catalogueSource: CatalogueSource = CatalogueSource.OFFICIAL
)

enum class CatalogueSource {
    OFFICIAL,
    IMPORTED,
    CACHED,
    UNVERIFIED
}

enum class SurveyType(val displayName: String, val bnName: String) {
    CS("CS", "সিএস (Cadastral Survey)"),
    SA("SA", "এসএ (State Acquisition Survey)"),
    RS("RS", "আরএস (Revisional Survey)"),
    BS("BS", "বিএস (Bangladesh Survey)"),
    BRS("BRS", "বিআরএস (City/Modern Survey)"),
    OTHER("OTHER", "অন্যান্য")
}

enum class RecordType(val displayName: String, val bnName: String) {
    KHATIAN("Khatian", "খতিয়ান / পরচা"),
    MAP("Map", "নকশা / মৌজা ম্যাপ")
}

enum class DownloadStatus(val bnTitle: String) {
    PENDING("অপেক্ষমান"),
    DOWNLOADING("ডাউনলোড হচ্ছে"),
    DOWNLOADED_UNVALIDATED("ডাউনলোড সম্পন্ন (অযাচাইকৃত)"),
    VALIDATING("যাচাইকরণ চলছে"),
    VALIDATED("বৈধ ফাইল"),
    VERIFIED("উৎস-যাচাইকৃত"),
    DUPLICATE("পুনরাবৃত্তি (বিদ্যমান)"),
    NOT_FOUND("রেকর্ড পাওয়া যায়নি"),
    FAILED("ব্যর্থ"),
    INCOMPLETE("অসম্পূর্ণ"),
    AUTH_REQUIRED("লগইন / ওটিপি প্রয়োজন"),
    ACCESS_RESTRICTED("প্রবেশাধিকার সীমাবদ্ধ"),
    SOURCE_UNVERIFIED("উৎস অযাচাইকৃত"),
    PAUSED("স্থগিত"),
    CANCELLED("বাতিলকৃত")
}

@Entity(
    tableName = "download_jobs",
    indices = [
        Index(value = ["checksum"]),
        Index(value = ["mouzaId", "surveyType", "recordIdentifier"]),
        Index(value = ["status"])
    ]
)
@Serializable
data class DownloadJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val divisionId: Int = 0,
    val divisionName: String = "",
    val districtId: Int = 0,
    val districtName: String = "",
    val upazilaId: Int = 0,
    val upazilaName: String = "",
    val mouzaId: String = "",
    val mouzaName: String = "",
    val jlNumber: String = "",
    val surveyType: SurveyType = SurveyType.RS,
    val recordType: RecordType = RecordType.KHATIAN,
    val recordIdentifier: String = "ALL", // Specific Khatian number or batch indicator
    val sourceName: String = "UNSET",
    val sourceReference: String? = null,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L,
    val filePath: String? = null,
    val archiveUri: String? = null,
    val checksum: String? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val retryCount: Int = 0,
    val batchId: String? = null
)
