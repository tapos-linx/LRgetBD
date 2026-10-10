package com.example

import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.model.RecordType
import com.example.model.SurveyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusCardAndProgressMeterTest {

    @Test
    fun `download job properly tracks 0 to 100 percent progress meter`() {
        val pendingJob = DownloadJob(
            id = 1,
            mouzaId = "dh_101",
            mouzaName = "ধামরাই",
            status = DownloadStatus.PENDING,
            progressPercent = 0,
            stageDescription = "অপেক্ষমান"
        )
        assertEquals(0, pendingJob.progressPercent)
        assertEquals(DownloadStatus.PENDING, pendingJob.status)

        val processingJob = pendingJob.copy(
            status = DownloadStatus.PROCESSING,
            progressPercent = 25,
            stageDescription = "অনুরোধ প্রস্তুত হচ্ছে..."
        )
        assertEquals(25, processingJob.progressPercent)
        assertEquals(DownloadStatus.PROCESSING, processingJob.status)

        val downloadingJob = processingJob.copy(
            status = DownloadStatus.DOWNLOADING,
            progressPercent = 50,
            stageDescription = "সার্ভার অনুসন্ধান ও ডাটা ডাউনলোড হচ্ছে..."
        )
        assertEquals(50, downloadingJob.progressPercent)
        assertEquals(DownloadStatus.DOWNLOADING, downloadingJob.status)

        val readyToDownloadJob = downloadingJob.copy(
            status = DownloadStatus.READY_TO_DOWNLOAD,
            progressPercent = 75,
            stageDescription = "ডাউনলোড সম্পন্ন, ফাইল যাচাইকরণ চলছে..."
        )
        assertEquals(75, readyToDownloadJob.progressPercent)
        assertEquals(DownloadStatus.READY_TO_DOWNLOAD, readyToDownloadJob.status)

        val completedJob = readyToDownloadJob.copy(
            status = DownloadStatus.VALIDATED,
            progressPercent = 100,
            stageDescription = "সংরক্ষণ সম্পন্ন ও প্রস্তুত (১০০%)",
            checksum = "a1b2c3d4e5f6",
            fileSizeBytes = 1024L
        )
        assertEquals(100, completedJob.progressPercent)
        assertEquals(DownloadStatus.VALIDATED, completedJob.status)
        assertTrue(completedJob.checksum != null)
    }

    @Test
    fun `states have clear distinct Bangla titles`() {
        assertEquals("Pending (অপেক্ষমান)", "Pending (অপেক্ষমান)")
        assertEquals("Processing (প্রক্রিয়াধীন)", "Processing (প্রক্রিয়াধীন)")
        assertEquals("Ready to Download (প্রস্তুত)", "Ready to Download (প্রস্তুত)")
    }
}
