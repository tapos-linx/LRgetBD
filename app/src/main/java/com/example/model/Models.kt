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
    MUTATION("MUTATION", "নামজারি খতিয়ান (Mutation)"),
    OTHER("OTHER", "অন্যান্য")
}

enum class RecordType(val displayName: String, val bnName: String) {
    KHATIAN("Khatian", "খতিয়ান / পরচা"),
    MAP("Map", "নকশা / মৌজা ম্যাপ")
}

@Serializable
data class DocumentTypeItem(
    val id: String,
    val surveyType: SurveyType,
    val recordType: RecordType,
    val titleBn: String,
    val subtitleBn: String = ""
) {
    companion object {
        val ALL_AVAILABLE = listOf(
            DocumentTypeItem("RS_KHATIAN", SurveyType.RS, RecordType.KHATIAN, "আরএস খতিয়ান", "Revisional Survey"),
            DocumentTypeItem("CS_KHATIAN", SurveyType.CS, RecordType.KHATIAN, "সিএস খতিয়ান", "Cadastral Survey"),
            DocumentTypeItem("SA_KHATIAN", SurveyType.SA, RecordType.KHATIAN, "এসএ খতিয়ান", "State Acquisition"),
            DocumentTypeItem("BS_KHATIAN", SurveyType.BS, RecordType.KHATIAN, "বিএস খতিয়ান", "Bangladesh Survey"),
            DocumentTypeItem("BRS_KHATIAN", SurveyType.BRS, RecordType.KHATIAN, "বিআরএস খতিয়ান", "City Survey"),
            DocumentTypeItem("MUTATION_KHATIAN", SurveyType.MUTATION, RecordType.KHATIAN, "নামজারি খতিয়ান", "Mutation Khatian"),
            DocumentTypeItem("RS_MAP", SurveyType.RS, RecordType.MAP, "আরএস নকশা (ম্যাপ)", "Cadastral Sheet"),
            DocumentTypeItem("CS_MAP", SurveyType.CS, RecordType.MAP, "সিএস নকশা (ম্যাপ)", "Original Sheet"),
            DocumentTypeItem("BS_MAP", SurveyType.BS, RecordType.MAP, "বিএস নকশা (ম্যাপ)", "Modern Sheet")
        )
    }
}

enum class DownloadStatus(val bnTitle: String) {
    PENDING("অপেক্ষমান"),
    PROCESSING("প্রক্রিয়াধীন"),
    DOWNLOADING("ডাউনলোড হচ্ছে"),
    READY_TO_DOWNLOAD("ডাউনলোডের জন্য প্রস্তুত"),
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
    val progressPercent: Int = 0, // 0 to 100% download meter
    val stageDescription: String = "", // e.g. "সার্ভার অনুসন্ধান", "ডাউনলোড চলছে (৪৫%)"
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
