package com.example.data

import com.example.model.DownloadJob
import java.io.File

interface SourceAdapter {
    val name: String
    suspend fun download(job: DownloadJob, targetFile: File): DownloadResult
}

sealed class DownloadResult {
    object Success : DownloadResult()
    data class Failure(val message: String) : DownloadResult()
    object AuthRequired : DownloadResult()
}

class MockSourceAdapter : SourceAdapter {
    override val name: String = "Mock Source"

    override suspend fun download(job: DownloadJob, targetFile: File): DownloadResult {
        // Simulate network delay
        kotlinx.coroutines.delay(2000)
        
        // Create a mock PDF
        targetFile.writeBytes("%PDF-1.4\n%mock content".toByteArray())
        
        return DownloadResult.Success
    }
}
