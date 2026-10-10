package com.example.data

import androidx.room.TypeConverter
import com.example.model.DownloadStatus
import com.example.model.RecordType
import com.example.model.SurveyType

class Converters {
    @TypeConverter
    fun fromSurveyType(value: SurveyType): String = value.name

    @TypeConverter
    fun toSurveyType(value: String): SurveyType = SurveyType.valueOf(value)

    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}
