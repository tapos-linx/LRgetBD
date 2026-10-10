package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.LRgetBDApplication
import com.example.data.OfficialEporchaAdapter
import com.example.data.SourceDownloadResult
import com.example.data.TestMockAdapter
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.util.ArchiveFileManager
import com.example.util.PdfValidator
import com.example.util.SettingsManager
import java.io.File
import kotlinx.coroutines.flow.firstOrNull

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = (applicationContext as LRgetBDApplication).database
        val dao = database.downloadJobDao()
        val archiveManager = ArchiveFileManager(applicationContext)
        val settingsManager = SettingsManager(applicationContext)

        val jobId = inputData.getLong("jobId", -1L)
        val isDevTestMode = inputData.getBoolean("devTestMode", false)

        if (jobId == -1L) return Result.failure()

        val job = dao.getJobById(jobId) ?: return Result.failure()

        // Check if job was paused or cancelled before execution
        if (job.status == DownloadStatus.PAUSED || job.status == DownloadStatus.CANCELLED) {
            return Result.success()
        }

        dao.updateProgress(jobId, DownloadStatus.PROCESSING, 15, "অনুরোধ প্রক্রিয়া শুরু হচ্ছে...")

        // Select adapter strictly: Never allow mock to masquerade as official in production
        val adapter = if (isDevTestMode) {
            TestMockAdapter(returnValidPdf = true)
        } else {
            OfficialEporchaAdapter()
        }

        val cacheDir = applicationContext.cacheDir
        val tempPartFile = File(cacheDir, "job_${jobId}_${System.currentTimeMillis()}.part")

        try {
            dao.updateProgress(jobId, DownloadStatus.DOWNLOADING, 45, "উৎস অনুসন্ধান ও ডাউনলোড চলছে...")
            val result = adapter.downloadRecord(job, tempPartFile)

            return when (result) {
                is SourceDownloadResult.SourceUnverified -> {
                    tempPartFile.delete()
                    dao.updateJob(
                        job.copy(
                            status = DownloadStatus.SOURCE_UNVERIFIED,
                            progressPercent = 0,
                            stageDescription = "অযাচাইকৃত উৎস — ডাউনলোড বন্ধ",
                            sourceName = adapter.sourceName,
                            errorMessage = result.explanationBn,
                            errorCode = "ERR_SOURCE_UNVERIFIED",
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    Result.success()
                }

                is SourceDownloadResult.AccessRestricted -> {
                    tempPartFile.delete()
                    dao.updateJob(
                        job.copy(
                            status = DownloadStatus.ACCESS_RESTRICTED,
                            progressPercent = 0,
                            stageDescription = "প্রবেশাধিকার সীমাবদ্ধ",
                            sourceName = adapter.sourceName,
                            errorMessage = result.reasonBn,
                            errorCode = "ERR_ACCESS_RESTRICTED",
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    Result.success()
                }

                is SourceDownloadResult.AuthRequired -> {
                    tempPartFile.delete()
                    dao.updateJob(
                        job.copy(
                            status = DownloadStatus.AUTH_REQUIRED,
                            progressPercent = 0,
                            stageDescription = "লগইন / ওটিপি প্রয়োজন",
                            sourceName = adapter.sourceName,
                            errorMessage = result.loginPromptBn,
                            errorCode = "ERR_AUTH_REQUIRED",
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    Result.success()
                }

                is SourceDownloadResult.CaptchaOrOtpRequired -> {
                    tempPartFile.delete()
                    dao.updateJob(
                        job.copy(
                            status = DownloadStatus.AUTH_REQUIRED,
                            progressPercent = 0,
                            stageDescription = "ক্যাপচা / ওটিপি প্রয়োজন",
                            sourceName = adapter.sourceName,
                            errorMessage = "ক্যাপচা অথবা ওটিপি ভেরিফিকেশন আবশ্যক (CAPTCHA/OTP Required)",
                            errorCode = "ERR_CAPTCHA_OTP_REQUIRED",
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    Result.success()
                }

                is SourceDownloadResult.Failure -> {
                    tempPartFile.delete()
                    val newRetryCount = job.retryCount + 1
                    dao.updateJob(
                        job.copy(
                            status = DownloadStatus.FAILED,
                            progressPercent = 0,
                            stageDescription = "ডাউনলোড ব্যর্থ",
                            sourceName = adapter.sourceName,
                            errorMessage = result.message,
                            errorCode = result.errorCode,
                            retryCount = newRetryCount,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    if (result.canRetry && runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }

                is SourceDownloadResult.Success -> {
                    dao.updateProgress(jobId, DownloadStatus.READY_TO_DOWNLOAD, 75, "ডাউনলোড প্রস্তুত, ফাইল যাচাইকরণ চলছে...")
                    // Step 5: Rigorous PDF validation
                    val validation = PdfValidator.validate(tempPartFile)
                    when (validation) {
                        is PdfValidator.ValidationResult.Invalid -> {
                            tempPartFile.delete()
                            dao.updateJob(
                                job.copy(
                                    status = DownloadStatus.FAILED,
                                    progressPercent = 0,
                                    stageDescription = "অবৈধ ফাইল ফরম্যাট",
                                    sourceName = adapter.sourceName,
                                    errorMessage = validation.reason,
                                    errorCode = validation.errorCode,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            Result.failure()
                        }

                        is PdfValidator.ValidationResult.Valid -> {
                            dao.updateProgress(jobId, DownloadStatus.VALIDATING, 90, "ডুপ্লিকেট চেক ও ফাইলে সংরক্ষণ...")
                            // Check for duplicates in database by checksum
                            val existingDuplicate = dao.findJobByChecksum(validation.checksum)
                            if (existingDuplicate != null && existingDuplicate.id != job.id) {
                                tempPartFile.delete()
                                dao.updateJob(
                                    job.copy(
                                        status = DownloadStatus.DUPLICATE,
                                        progressPercent = 100,
                                        stageDescription = "বিদ্যমান ফাইলের অনুরূপ (১০০%)",
                                        sourceName = adapter.sourceName,
                                        checksum = validation.checksum,
                                        fileSizeBytes = validation.sizeBytes,
                                        filePath = existingDuplicate.filePath,
                                        errorMessage = "একই রেকর্ড পূর্বে সংরক্ষিত রয়েছে (Duplicate checksum detected)",
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                return Result.success()
                            }

                            // Store validated file to archive
                            val safUri = settingsManager.archiveUri.firstOrNull()
                            val archiveResult = archiveManager.storeValidatedRecord(
                                job = job,
                                tempPartFile = tempPartFile,
                                checksum = validation.checksum,
                                safTreeUriString = safUri
                            )

                            val finalStatus = if (archiveResult.isDuplicate) {
                                DownloadStatus.DUPLICATE
                            } else if (adapter.isOfficial && !adapter.isTestMock) {
                                DownloadStatus.VERIFIED
                            } else {
                                DownloadStatus.VALIDATED
                            }

                            dao.updateJob(
                                job.copy(
                                    status = finalStatus,
                                    progressPercent = 100,
                                    stageDescription = "সংরক্ষণ সম্পন্ন ও প্রস্তুত (১০০%)",
                                    sourceName = adapter.sourceName,
                                    sourceReference = result.sourceReference,
                                    filePath = archiveResult.finalPath,
                                    archiveUri = archiveResult.archiveUri,
                                    checksum = validation.checksum,
                                    fileSizeBytes = validation.sizeBytes,
                                    errorMessage = null,
                                    errorCode = null,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            Result.success()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            tempPartFile.delete()
            dao.updateJob(
                job.copy(
                    status = DownloadStatus.FAILED,
                    errorMessage = e.localizedMessage ?: "অপ্রত্যাশিত নেটওয়ার্ক ত্রুটি",
                    errorCode = "ERR_UNEXPECTED_EXCEPTION",
                    updatedAt = System.currentTimeMillis()
                )
            )
            return Result.failure()
        }
    }
}
