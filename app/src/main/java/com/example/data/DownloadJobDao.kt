package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadJobDao {
    @Query("SELECT * FROM download_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<DownloadJob>>

    @Query("SELECT * FROM download_jobs ORDER BY createdAt DESC")
    suspend fun getAllJobsSnapshot(): List<DownloadJob>

    @Query("SELECT * FROM download_jobs WHERE status = :status ORDER BY createdAt ASC")
    fun getJobsByStatus(status: DownloadStatus): Flow<List<DownloadJob>>

    @Query("SELECT * FROM download_jobs WHERE status IN ('PENDING', 'DOWNLOADING', 'VALIDATING') ORDER BY createdAt ASC")
    fun getActiveQueueJobs(): Flow<List<DownloadJob>>

    @Query("SELECT * FROM download_jobs WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingJobs(limit: Int = 7): List<DownloadJob>

    @Query("SELECT * FROM download_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): DownloadJob?

    @Query("SELECT * FROM download_jobs WHERE checksum = :checksum AND status IN ('VALIDATED', 'VERIFIED') LIMIT 1")
    suspend fun findJobByChecksum(checksum: String): DownloadJob?

    @Query("SELECT * FROM download_jobs WHERE mouzaId = :mouzaId AND surveyType = :surveyType AND recordIdentifier = :recordIdentifier AND status IN ('VALIDATED', 'VERIFIED') LIMIT 1")
    suspend fun findExistingRecord(mouzaId: String, surveyType: String, recordIdentifier: String): DownloadJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: DownloadJob): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<DownloadJob>): List<Long>

    @Update
    suspend fun updateJob(job: DownloadJob)

    @Query("DELETE FROM download_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("DELETE FROM download_jobs")
    suspend fun deleteAllJobs()

    @Query("UPDATE download_jobs SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE download_jobs SET status = 'PAUSED', updatedAt = :now WHERE status IN ('PENDING', 'DOWNLOADING')")
    suspend fun pauseActiveJobs(now: Long = System.currentTimeMillis()): Int

    @Query("UPDATE download_jobs SET status = 'PENDING', updatedAt = :now WHERE status = 'PAUSED'")
    suspend fun resumePausedJobs(now: Long = System.currentTimeMillis()): Int

    @Query("UPDATE download_jobs SET status = 'CANCELLED', updatedAt = :now WHERE status IN ('PENDING', 'PAUSED', 'DOWNLOADING')")
    suspend fun cancelActiveJobs(now: Long = System.currentTimeMillis()): Int

    @Query("UPDATE download_jobs SET status = 'PENDING', retryCount = retryCount + 1, errorMessage = null, errorCode = null, updatedAt = :now WHERE status IN ('FAILED', 'INCOMPLETE')")
    suspend fun retryFailedJobs(now: Long = System.currentTimeMillis()): Int

    @Query("UPDATE download_jobs SET status = 'PENDING', retryCount = retryCount + 1, errorMessage = null, errorCode = null, updatedAt = :now WHERE status = 'AUTH_REQUIRED'")
    suspend fun retryAuthRequiredJobs(now: Long = System.currentTimeMillis()): Int
}
