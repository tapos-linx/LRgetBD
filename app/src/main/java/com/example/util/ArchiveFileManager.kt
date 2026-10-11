package com.example.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
        val upazilaSafe = sanitizeFileName(if (job.upazilaName.isNotBlank()) job.upazilaName else "Upazila_${job.upazilaId}")
        val mouzaSafe = sanitizeFileName(if (job.mouzaName.isNotBlank()) job.mouzaName else "Mouza_${job.mouzaId}")
        val jlSafe = sanitizeFileName(if (job.jlNumber.isNotBlank()) job.jlNumber else "0")
        val surveySafe = sanitizeFileName(job.surveyType.name)
        val recordTypeSafe = sanitizeFileName(job.recordType.name)

        val masterFolderName = "LR_BD_Records_$upazilaSafe"
        val mouzaFolderName = "Mouza_${jlSafe}_$mouzaSafe"
        val relativeSubPath = "$masterFolderName/$mouzaFolderName"
        val baseFileName = "${recordTypeSafe}_${surveySafe}_${job.recordIdentifier}_${checksum.take(8)}"
        val targetFileName = "$baseFileName.pdf"

        // Check if SAF URI is provided and valid
        if (!safTreeUriString.isNullOrBlank()) {
            try {
                val treeUri = Uri.parse(safTreeUriString)
                val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                if (rootDoc != null && rootDoc.canWrite()) {
                    var currentDir: DocumentFile = rootDoc
                    // Navigate/create Master Folder and Mouza subfolder
                    for (segment in listOf(masterFolderName, mouzaFolderName)) {
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

            // Also maintain Upazila Master Index in parent folder (LR_BD_Records_[Upazila])
            val parentDir = dir.parentFile
            if (parentDir != null && parentDir.name.startsWith("LR_BD_Records_")) {
                val masterIndexFile = File(parentDir, "master_upazila_index.json")
                val masterArray = if (masterIndexFile.exists()) {
                    try { JSONArray(masterIndexFile.readText()) } catch (e: Exception) { JSONArray() }
                } else {
                    JSONArray()
                }
                masterArray.put(entry)
                masterIndexFile.writeText(masterArray.toString(2))
            }
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

    fun getBaseStorageDir(): File {
        return context.getExternalFilesDir(null) ?: context.filesDir
    }

    fun listMasterFolderNames(): List<String> {
        val baseDir = getBaseStorageDir()
        return baseDir.listFiles()?.filter { it.isDirectory && it.name.startsWith("LR_BD_Records_") }?.map { it.name } ?: emptyList()
    }

    /**
     * Creates a ZIP archive containing the Master Folder with all its Mouza subfolders and records.
     * Copies the resulting ZIP into public Downloads if available, and returns the file.
     */
    fun createMasterZip(upazilaName: String? = null): File {
        val baseDir = getBaseStorageDir()
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val safeUpazila = if (!upazilaName.isNullOrBlank()) sanitizeFileName(upazilaName) else "All_Upazilas"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val zipFile = File(exportDir, "LR_BD_Records_${safeUpazila}_$timestamp.zip")

        val targetDir = if (!upazilaName.isNullOrBlank()) {
            val candidate = File(baseDir, "LR_BD_Records_$safeUpazila")
            if (candidate.exists()) candidate else baseDir
        } else {
            baseDir
        }

        // Ensure master index file exists in directory
        val indexFile = File(targetDir, "master_upazila_index.json")
        if (!indexFile.exists()) {
            indexFile.writeText("{\n  \"export\": \"LRgetBD\",\n  \"upazila\": \"${upazilaName ?: "All"}\",\n  \"generatedAt\": \"${Date()}\"\n}")
        }

        java.util.zip.ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            zipFolderRecursively(targetDir, targetDir, zipOut)
        }

        // Best effort: Copy to public Downloads folder on device
        try {
            saveZipToPhoneStorage(zipFile)
        } catch (_: Exception) {}

        return zipFile
    }

    /**
     * Saves the ZIP file directly into the phone's public Downloads directory (Phone Storage > Downloads > LRgetBD_Records)
     * using MediaStore.Downloads (Android 10+) or Environment.getExternalStoragePublicDirectory (Android 9 and below).
     */
    fun saveZipToPhoneStorage(zipFile: File): Uri? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, zipFile.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LRgetBD_Records")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(zipFile).use { input -> input.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    return uri
                }
            } else {
                val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(publicDownloads, "LRgetBD_Records")
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, zipFile.name)
                FileInputStream(zipFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("application/zip"), null)
                return Uri.fromFile(targetFile)
            }
        } catch (_: Exception) {}
        return null
    }

    data class PhoneStorageExportResult(
        val exportedFilesCount: Int,
        val storagePathDescription: String,
        val zipUri: Uri?,
        val zipFile: File
    )

    /**
     * Exports the master folder with all its individual PDF files and structure
     * straight into the phone's public Downloads directory under Downloads/LRgetBD_Records/LR_BD_Records_[Upazila]
     */
    fun exportMasterFolderDirectlyToPhoneStorage(upazilaName: String? = null): PhoneStorageExportResult {
        val zipFile = createMasterZip(upazilaName)
        val zipUri = saveZipToPhoneStorage(zipFile)
        val baseDir = getBaseStorageDir()
        val safeUpazila = if (!upazilaName.isNullOrBlank()) sanitizeFileName(upazilaName) else "All_Upazilas"
        val targetDir = if (!upazilaName.isNullOrBlank()) {
            val candidate = File(baseDir, "LR_BD_Records_$safeUpazila")
            if (candidate.exists()) candidate else baseDir
        } else {
            baseDir
        }

        var exportedCount = 0
        val allPdfs = targetDir.walkTopDown().filter { it.isFile && it.extension.lowercase() == "pdf" }.toList()

        for (pdf in allPdfs) {
            try {
                val relativePathFromTarget = targetDir.toURI().relativize(pdf.parentFile.toURI()).path.trim('/')
                val destinationSubFolder = if (relativePathFromTarget.isNotBlank()) {
                    Environment.DIRECTORY_DOWNLOADS + "/LRgetBD_Records/LR_BD_Records_$safeUpazila/$relativePathFromTarget"
                } else {
                    Environment.DIRECTORY_DOWNLOADS + "/LRgetBD_Records/LR_BD_Records_$safeUpazila"
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, pdf.name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, destinationSubFolder)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            FileInputStream(pdf).use { input -> input.copyTo(out) }
                        }
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)
                        exportedCount++
                    }
                } else {
                    val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val outDir = File(publicDownloads, "LRgetBD_Records/LR_BD_Records_$safeUpazila/$relativePathFromTarget")
                    if (!outDir.exists()) outDir.mkdirs()
                    val outFile = File(outDir, pdf.name)
                    FileInputStream(pdf).use { input ->
                        FileOutputStream(outFile).use { output -> input.copyTo(output) }
                    }
                    MediaScannerConnection.scanFile(context, arrayOf(outFile.absolutePath), arrayOf("application/pdf"), null)
                    exportedCount++
                }
            } catch (_: Exception) {}
        }

        val pathDesc = "Phone Storage > Downloads > LRgetBD_Records"
        return PhoneStorageExportResult(
            exportedFilesCount = exportedCount,
            storagePathDescription = pathDesc,
            zipUri = zipUri,
            zipFile = zipFile
        )
    }

    private fun zipFolderRecursively(rootDir: File, currentFile: File, zipOut: java.util.zip.ZipOutputStream) {
        val files = currentFile.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                zipFolderRecursively(rootDir, file, zipOut)
            } else if (!file.name.endsWith(".zip") && !file.name.endsWith(".part")) {
                val relativePath = rootDir.toURI().relativize(file.toURI()).path
                val entry = java.util.zip.ZipEntry(relativePath)
                zipOut.putNextEntry(entry)
                FileInputStream(file).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        }
    }

    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .trim('_')
            .ifEmpty { "Record" }
    }
}
