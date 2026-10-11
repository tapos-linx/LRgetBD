package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.OfficialEporchaAdapter
import com.example.data.SourceDownloadResult
import com.example.data.TestMockAdapter
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import com.example.model.RecordType
import com.example.model.SurveyType
import com.example.util.ArchiveFileManager
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QueueAndArchiveTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `official adapter provides open citizen public download without requiring mandatory key`() = runBlocking {
        val adapter = OfficialEporchaAdapter(authorizedApiKey = null)
        assertTrue("Official adapter supports public citizen download", adapter.isAuthorizedForBulk)

        val tempFile = tempFolder.newFile("test_target.pdf")
        val job = DownloadJob(
            mouzaId = "dh_101",
            surveyType = SurveyType.RS,
            recordType = RecordType.KHATIAN,
            recordIdentifier = "123"
        )

        val result = adapter.downloadRecord(job, tempFile)
        assertTrue("Expected Success result for citizen public download", result is SourceDownloadResult.Success)
        assertTrue(tempFile.length() > 50)
    }

    @Test
    fun `test mock adapter produces valid PDF only in test harness`() = runBlocking {
        val mockAdapter = TestMockAdapter(returnValidPdf = true)
        assertTrue(mockAdapter.isTestMock)

        val tempFile = tempFolder.newFile("mock_out.pdf")
        val job = DownloadJob(mouzaId = "dh_101", recordIdentifier = "555")
        val result = mockAdapter.downloadRecord(job, tempFile)

        assertTrue(result is SourceDownloadResult.Success)
        assertTrue(tempFile.length() > 100)
    }

    @Test
    fun `archive file manager stores file in structured hierarchy and writes manifest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val archiveManager = ArchiveFileManager(context)

        val tempFile = tempFolder.newFile("temp_part.pdf")
        tempFile.writeText("%PDF-1.4\nTest valid content\n%%EOF")

        val job = DownloadJob(
            id = 101L,
            divisionName = "ঢাকা",
            districtName = "ঢাকা",
            upazilaName = "ধামরাই",
            mouzaName = "বালিয়া",
            jlNumber = "102",
            surveyType = SurveyType.RS,
            recordType = RecordType.KHATIAN,
            recordIdentifier = "789",
            sourceName = "TEST_SOURCE"
        )

        val checksum = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
        val archiveResult = archiveManager.storeValidatedRecord(
            job = job,
            tempPartFile = tempFile,
            checksum = checksum,
            safTreeUriString = null // uses local app-scoped fallback
        )

        assertTrue(archiveResult.finalPath.endsWith(".pdf"))
        val savedFile = File(archiveResult.finalPath)
        assertTrue(savedFile.exists())
        assertEquals(checksum.take(8), savedFile.nameWithoutExtension.takeLast(8))

        // Check that manifest file exists in the directory
        val manifestFile = File(savedFile.parentFile, "manifest.json")
        assertTrue(manifestFile.exists())
        val manifestJson = manifestFile.readText()
        assertTrue(manifestJson.contains("\"checksumSha256\": \"$checksum\""))
        assertTrue(manifestJson.contains("\"recordIdentifier\": \"789\""))
    }

    @Test
    fun `export to CSV and JSON generates accurate records`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val archiveManager = ArchiveFileManager(context)

        val jobs = listOf(
            DownloadJob(
                id = 1,
                status = DownloadStatus.VALIDATED,
                divisionName = "ঢাকা",
                districtName = "ঢাকা",
                upazilaName = "ধামরাই",
                mouzaName = "বালিয়া",
                jlNumber = "102",
                surveyType = SurveyType.RS,
                recordType = RecordType.KHATIAN,
                recordIdentifier = "100",
                sourceName = "TEST_SOURCE",
                checksum = "hash123",
                fileSizeBytes = 2048L
            )
        )

        val csv = archiveManager.exportToCsv(jobs)
        assertTrue(csv.contains("JobId,Status,Division"))
        assertTrue(csv.contains("\"VALIDATED\""))
        assertTrue(csv.contains("\"বালিয়া\""))
        assertTrue(csv.contains("hash123"))

        val json = archiveManager.exportToJson(jobs)
        assertTrue(json.contains("\"id\": 1"))
        assertTrue(json.contains("\"mouzaName\": \"বালিয়া\""))
        assertTrue(json.contains("\"checksum\": \"hash123\""))
    }
}
