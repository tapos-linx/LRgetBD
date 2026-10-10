package com.example.data

import androidx.room.TypeConverter
import com.example.model.CatalogueSource
import com.example.model.DownloadStatus
import com.example.model.RecordType
import com.example.model.SurveyType

class Converters {
    @TypeConverter
    fun fromSurveyType(value: SurveyType?): String = value?.name ?: SurveyType.RS.name

    @TypeConverter
    fun toSurveyType(value: String?): SurveyType =
        value?.let { runCatching { SurveyType.valueOf(it) }.getOrDefault(SurveyType.RS) } ?: SurveyType.RS

    @TypeConverter
    fun fromRecordType(value: RecordType?): String = value?.name ?: RecordType.KHATIAN.name

    @TypeConverter
    fun toRecordType(value: String?): RecordType =
        value?.let { runCatching { RecordType.valueOf(it) }.getOrDefault(RecordType.KHATIAN) } ?: RecordType.KHATIAN

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus?): String = value?.name ?: DownloadStatus.PENDING.name

    @TypeConverter
    fun toDownloadStatus(value: String?): DownloadStatus =
        value?.let { runCatching { DownloadStatus.valueOf(it) }.getOrDefault(DownloadStatus.PENDING) } ?: DownloadStatus.PENDING

    @TypeConverter
    fun fromCatalogueSource(value: CatalogueSource?): String = value?.name ?: CatalogueSource.OFFICIAL.name

    @TypeConverter
    fun toCatalogueSource(value: String?): CatalogueSource =
        value?.let { runCatching { CatalogueSource.valueOf(it) }.getOrDefault(CatalogueSource.OFFICIAL) } ?: CatalogueSource.OFFICIAL
}
