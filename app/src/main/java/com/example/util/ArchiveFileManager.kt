package com.example.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.model.DownloadJob
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class ArchiveFileManager(private val context: Context) {

    data class ArchiveResult(
        val finalPath: String,
        val archiveUri: String?,
        val isDuplicate: Boolean,
        val relativeSubPath: String
    )

    /**
     * Store validated file atomically into the user-selected SAF folder or local app storage.
     * Hierarchy:
     * LRgetBD/Bangladesh/<Division>/<District>/<Upazila>/<Mouza>/<SurveyType>/<RecordType>/<safeFilename>.pdf
     */
    fun storeValidatedRecord(
        job: DownloadJob,
        tempPartFile: File,
        checksum: String,
        safTreeUriString: String?
    ): ArchiveResult {
        val divisionSafe = sanitizeFileName(if (job.divisionName.isNotBlank()) job.divisionName else "Division_${job.divisionId}")
        val districtSafe = sanitizeFileName(if (job.districtName.isNotBlank()) job.districtName else "District_${job.districtId}")
        val upazilaSafe = sanitizeFileName(if (job.upazilaName.isNotBlank()) job.upazilaName else "Upazila_${job.upazilaId}")
        val mouzaSafe = sanitizeFileName(if (job.mouzaName.isNotBlank()) "${job.mouzaName}_JL${job.jlNumber}" else "Mouza_${job.mouzaId}")
        val surveySafe = sanitizeFileName(job.surveyType.name)
        val recordTypeSafe = sanitizeFileName(job.recordType.name)

        val relativeSubPath = "LRgetBD/Bangladesh/$divisionSafe/$districtSafe/$upazilaSafe/$mouzaSafe/$surveySafe/$recordTypeSafe"
        val baseFileName = "${recordTypeSafe}_${job.recordIdentifier}_${checksum.take(8)}"
        val targetFileName = "$baseFileName.pdf"

        // Check if SAF URI is provided and valid
        if (!safTreeUriString.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(safTreeUriString)
                val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                if (rootDoc != null && rootDoc.canWrite()) {
                    var currentDir: DocumentFile = rootDoc
                    // Navigate/create path segments
                    for (segment in listOf("LRgetBD", "Bangladesh", divisionSafe, districtSafe, upazilaSafe, mouzaSafe, surveySafe, recordTypeSafe)) {
                        val subDir = currentDir.findFile(segment)
                        val nextDir = if (subDir == null || !subDir.isDirectory) {
                            currentDir.createDirectory(segment) ?: currentDir
                        } else {
                            subDir
                        }
                        currentDir = nextDir
                    }

                    // Check for existing file with same name
                    val existingDoc = currentDir.findFile(targetFileName)
                    if (existingDoc != null && existingDoc.exists()) {
                        // Check if file is duplicate
                        return ArchiveResult(
                            finalPath = existingDoc.uri.toString(),
                            archiveUri = existingDoc.uri.toString(),
                            isDuplicate = true,
                            relativeSubPath = relativeSubPath
                        )
                    }

                    val newFileDoc = currentDir.createFile("application/pdf", targetFileName)
                    if (newFileDoc != null) {
                        context.contentResolver.openOutputStream(newFileDoc.uri)?.use { out ->
                            FileInputStream(tempPartFile).use { input ->
                                input.copyTo(out)
                            }
                        }

                        // Write / update manifest in that directory
                        writeSafManifest(currentDir, job, targetFileName, checksum, tempPartFile.length())

                        return ArchiveResult(
                            finalPath = newFileDoc.uri.toString(),
                            archiveUri = newFileDoc.uri.toString(),
                            isDuplicate = false,
                            relativeSubPath = relativeSubPath
                        )
                    }
                }
            } catch (e: Exception) {
                // Fall back to local file storage if SAF fails
            }
        }

        // Standard Local Storage Fallback:
        // Use externalFilesDir if available, or internal filesDir
        val baseFolder = context.getExternalFilesDir(null) ?: context.filesDir
        val targetDir = File(baseFolder, relativeSubPath)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val destinationFile = File(targetDir, targetFileName)
        val isDuplicate = destinationFile.exists() && destinationFile.length() == tempPartFile.length()

        if (!isDuplicate) {
            // Atomic move or copy
            if (!tempPartFile.renameTo(destinationFile)) {
                destinationFile.outputStream().use { out ->
                    tempPartFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                tempPartFile.delete()
            }
        }

        writeLocalManifest(targetDir, job, destinationFile.name, checksum, destinationFile.length())

        return ArchiveResult(
            finalPath = destinationFile.absolutePath,
            archiveUri = null,
            isDuplicate = isDuplicate,
            relativeSubPath = relativeSubPath
        )
    }

    private fun writeLocalManifest(dir: File, job: DownloadJob, fileName: String, checksum: String, sizeBytes: Long) {
        try {
            val manifestFile = File(dir, "manifest.json")
            val manifestArray = if (manifestFile.exists()) {
                try { JSONArray(manifestFile.readText()) } catch (e: Exception) { JSONArray() }
            } else {
                JSONArray()
            }

            val entry = JSONObject().apply {
                put("jobId", job.id)
                put("fileName", fileName)
                put("recordIdentifier", job.recordIdentifier)
                put("surveyType", job.surveyType.name)
                put("recordType", job.recordType.name)
                put("sourceName", job.sourceName)
                put("sourceReference", job.sourceReference ?: "")
                put("checksumSha256", checksum)
                put("sizeBytes", sizeBytes)
                put("downloadTimestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
                put("division", job.divisionName)
                put("district", job.districtName)
                put("upazila", job.upazilaName)
                put("mouza", job.mouzaName)
                put("jlNumber", job.jlNumber)
                put("status", "VALIDATED")
            }

            manifestArray.put(entry)
            manifestFile.writeText(manifestArray.toString(2))
        } catch (e: Exception) {
            // Manifest write best-effort
        }
    }

    private fun writeSafManifest(dir: DocumentFile, job: DownloadJob, fileName: String, checksum: String, sizeBytes: Long) {
        try {
            var manifestDoc = dir.findFile("manifest.json")
            val existingContent = if (manifestDoc != null) {
                context.contentResolver.openInputStream(manifestDoc.uri)?.use { it.bufferedReader().readText() } ?: "[]"
            } else "[]"

            val manifestArray = try { JSONArray(existingContent) } catch (e: Exception) { JSONArray() }
            val entry = JSONObject().apply {
                put("jobId", job.id)
                put("fileName", fileName)
                put("checksumSha256", checksum)
                put("sizeBytes", sizeBytes)
                put("status", "VALIDATED")
                put("timestamp", System.currentTimeMillis())
            }
            manifestArray.put(entry)

            if (manifestDoc == null) {
                manifestDoc = dir.createFile("application/json", "manifest.json")
            }
            if (manifestDoc != null) {
                context.contentResolver.openOutputStream(manifestDoc.uri)?.use { out ->
                    out.write(manifestArray.toString(2).toByteArray(Charsets.UTF_8))
                }
            }
        } catch (e: Exception) {
            // SAF manifest write best-effort
        }
    }

    /**
     * Export all download job records as CSV
     */
    fun exportToCsv(jobs: List<DownloadJob>): String {
        val sb = StringBuilder()
        sb.append("JobId,Status,Division,District,Upazila,Mouza,JL,SurveyType,RecordType,RecordId,Source,Checksum,SizeBytes,FilePath,CreatedAt\n")
        jobs.forEach { job ->
            sb.append("${job.id},")
            sb.append("\"${job.status.name}\",")
            sb.append("\"${job.divisionName}\",")
            sb.append("\"${job.districtName}\",")
            sb.append("\"${job.upazilaName}\",")
            sb.append("\"${job.mouzaName}\",")
            sb.append("\"${job.jlNumber}\",")
            sb.append("\"${job.surveyType.name}\",")
            sb.append("\"${job.recordType.name}\",")
            sb.append("\"${job.recordIdentifier}\",")
            sb.append("\"${job.sourceName}\",")
            sb.append("\"${job.checksum ?: ""}\",")
            sb.append("${job.fileSizeBytes},")
            sb.append("\"${job.filePath ?: ""}\",")
            sb.append("${job.createdAt}\n")
        }
        return sb.toString()
    }

    /**
     * Export all download job records as JSON
     */
    fun exportToJson(jobs: List<DownloadJob>): String {
        val array = JSONArray()
        jobs.forEach { job ->
            val obj = JSONObject().apply {
                put("id", job.id)
                put("status", job.status.name)
                put("divisionName", job.divisionName)
                put("districtName", job.districtName)
                put("upazilaName", job.upazilaName)
                put("mouzaName", job.mouzaName)
                put("jlNumber", job.jlNumber)
                put("surveyType", job.surveyType.name)
                put("recordType", job.recordType.name)
                put("recordIdentifier", job.recordIdentifier)
                put("sourceName", job.sourceName)
                put("checksum", job.checksum ?: "")
                put("fileSizeBytes", job.fileSizeBytes)
                put("filePath", job.filePath ?: "")
                put("errorMessage", job.errorMessage ?: "")
                put("createdAt", job.createdAt)
                put("updatedAt", job.updatedAt)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .trim('_')
            .ifEmpty { "Record" }
    }
}
