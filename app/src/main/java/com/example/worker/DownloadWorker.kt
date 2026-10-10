package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.LRgetBDApplication
import com.example.data.MockSourceAdapter
import com.example.model.DownloadStatus
import java.io.File
import java.security.MessageDigest

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = (applicationContext as LRgetBDApplication).database
        val dao = database.downloadJobDao()
        val adapter = MockSourceAdapter()

        // Get one pending job
        // In a real app, we might get multiple or be triggered with a specific ID
        // For now, let's just pick one pending
        // Better: receive job ID in inputData
        val jobId = inputData.getLong("jobId", -1)
        if (jobId == -1L) return Result.failure()

        val job = dao.getJobById(jobId) ?: return Result.failure()
        
        dao.updateStatus(jobId, DownloadStatus.DOWNLOADING)

        val cacheDir = applicationContext.cacheDir
        val partFile = File(cacheDir, "job_${jobId}.part")
        
        val result = adapter.download(job, partFile)
        
        return when (result) {
            is com.example.data.DownloadResult.Success -> {
                // Validate PDF
                if (validatePdf(partFile)) {
                    val checksum = calculateChecksum(partFile)
                    // Move to final location (simplified for now, using cache as "final")
                    val finalFile = File(applicationContext.filesDir, "validated_${jobId}.pdf")
                    partFile.renameTo(finalFile)
                    
                    dao.updateJob(job.copy(
                        status = DownloadStatus.VALIDATED,
                        filePath = finalFile.absolutePath,
                        checksum = checksum,
                        updatedAt = System.currentTimeMillis()
                    ))
                    Result.success()
                } else {
                    dao.updateJob(job.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = "Invalid PDF signature",
                        updatedAt = System.currentTimeMillis()
                    ))
                    Result.failure()
                }
            }
            is com.example.data.DownloadResult.Failure -> {
                dao.updateJob(job.copy(
                    status = DownloadStatus.FAILED,
                    errorMessage = result.message,
                    updatedAt = System.currentTimeMillis()
                ))
                Result.failure()
            }
            com.example.data.DownloadResult.AuthRequired -> {
                dao.updateJob(job.copy(
                    status = DownloadStatus.AUTH_REQUIRED,
                    updatedAt = System.currentTimeMillis()
                ))
                Result.retry()
            }
        }
    }

    private fun validatePdf(file: File): Boolean {
        if (!file.exists()) return false
        val bytes = file.readBytes()
        if (bytes.size < 4) return false
        // %PDF
        return bytes[0].toInt() == 0x25 && bytes[1].toInt() == 0x50 && bytes[2].toInt() == 0x44 && bytes[3].toInt() == 0x46
    }

    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
