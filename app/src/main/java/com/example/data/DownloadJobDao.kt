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

    @Query("SELECT * FROM download_jobs WHERE status = :status")
    fun getJobsByStatus(status: DownloadStatus): Flow<List<DownloadJob>>

    @Query("SELECT * FROM download_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): DownloadJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: DownloadJob): Long

    @Update
    suspend fun updateJob(job: DownloadJob)

    @Query("DELETE FROM download_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("UPDATE download_jobs SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus)
}
