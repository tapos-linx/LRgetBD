package com.example.data

import com.example.model.DownloadJob
import com.example.model.RecordType
import com.example.model.SurveyType
import java.io.File
import kotlinx.coroutines.delay

sealed class SourceDownloadResult {
    data class Success(
        val fileSizeBytes: Long,
        val sourceReference: String,
        val contentType: String = "application/pdf"
    ) : SourceDownloadResult()

    data class Failure(
        val message: String,
        val errorCode: String,
        val canRetry: Boolean = true
    ) : SourceDownloadResult()

    data class AuthRequired(
        val message: String,
        val loginPromptBn: String
    ) : SourceDownloadResult()

    data class CaptchaOrOtpRequired(
        val message: String,
        val challengeUrl: String? = null
    ) : SourceDownloadResult()

    data class AccessRestricted(
        val message: String,
        val reasonBn: String
    ) : SourceDownloadResult()

    data class SourceUnverified(
        val message: String,
        val explanationBn: String
    ) : SourceDownloadResult()
}

interface SourceAdapter {
    val adapterId: String
    val sourceName: String
    val organization: String
    val officialDocumentationUrl: String
    val termsSummary: String
    val supportedSurveys: List<SurveyType>
    val supportedRecords: List<RecordType>
    val requiresAuthentication: Boolean
    val permittedDownloadMethods: List<String>
    val rateLimitDelayMs: Long
    val isOfficial: Boolean
    val isAuthorizedForBulk: Boolean
    val isTestMock: Boolean
    val statusExplanationBn: String

    suspend fun downloadRecord(job: DownloadJob, targetTempFile: File): SourceDownloadResult
}

/**
 * Adapter representing the official Bangladesh Land Record Portal (ePorcha / DLCRS).
 *
 * Terms & Ethics Audit:
 * - Ownership: Ministry of Land (ভূমি মন্ত্রণালয়), People's Republic of Bangladesh.
 * - Documentation: https://eporcha.gov.bd
 * - Real access constraints: Citizen web access enforces individual CAPTCHA verification
 *   and SMS OTP for authenticated citizen record downloads. Automated scraping or bulk scraping
 *   without an official API MoU is unauthorized by terms of service.
 * - Safety Guarantee: In accordance with free-first and data-ethics rules, this adapter
 *   REFUSES to forge requests, bypass CAPTCHA, or simulate downloads. If an authorized
 *   REST/MOU endpoint is not configured in secrets, it safely flags SOURCE_UNVERIFIED.
 */
class OfficialEporchaAdapter(
    private val authorizedApiKey: String? = null
) : SourceAdapter {

    override val adapterId: String = "bd_eporcha_official"
    override val sourceName: String = "ই-পর্চা (ePorcha Bangladesh)"
    override val organization: String = "ভূমি মন্ত্রণালয়, গণপ্রজাতন্ত্রী বাংলাদেশ সরকার"
    override val officialDocumentationUrl: String = "https://eporcha.gov.bd"
    override val termsSummary: String = "ব্যক্তিগত নাগরিক যাচাই ও ওটিপি আবশ্যক। অননুমোদিত বাল্ক স্ক্র্যাপিং নিষিদ্ধ।"
    override val supportedSurveys: List<SurveyType> = listOf(SurveyType.CS, SurveyType.SA, SurveyType.RS, SurveyType.BS, SurveyType.BRS)
    override val supportedRecords: List<RecordType> = listOf(RecordType.KHATIAN, RecordType.MAP)
    override val requiresAuthentication: Boolean = true
    override val permittedDownloadMethods: List<String> = listOf("REST_API_AUTHORIZED")
    override val rateLimitDelayMs: Long = 3000L // Conservative rate limit
    override val isOfficial: Boolean = true
    override val isAuthorizedForBulk: Boolean = authorizedApiKey?.isNotBlank() == true
    override val isTestMock: Boolean = false
    override val statusExplanationBn: String =
        if (isAuthorizedForBulk) "অনুমোদিত সংযোগ সক্রিয়"
        else "অননুমোদিত বাল্ক এক্সেস বন্ধ — সরকারি এপিআই সংযোগ চুক্তি প্রয়োজন"

    override suspend fun downloadRecord(job: DownloadJob, targetTempFile: File): SourceDownloadResult {
        // Enforce rate limit
        delay(rateLimitDelayMs)

        if (authorizedApiKey.isNullOrBlank()) {
            return SourceDownloadResult.SourceUnverified(
                message = "No verified official bulk API key or authorized ministerial session configured.",
                explanationBn = "যাচাইকৃত সরকারি উৎস (ePorcha) এখনো সরাসরি বাল্ক ডাউনলোডের জন্য উন্মুক্ত নয়। " +
                        "ভুয়া বা কৃত্রিম রেকর্ড তৈরি নিষিদ্ধ। সঠিক সরকারি এপিআই অথবা স্বাক্ষরিত চুক্তি ছাড়া ডাউনলোড বন্ধ।"
            )
        }

        // Real authorized request flow when API key is provided
        return SourceDownloadResult.Failure(
            message = "Official API gateway endpoint awaiting verified certificate.",
            errorCode = "ERR_OFFICIAL_GATEWAY_PENDING",
            canRetry = false
        )
    }
}

/**
 * Test fixture adapter strictly isolated for automated testing and local verification.
 * CANNOT be called in production unless explicit test mode is active.
 */
class TestMockAdapter(
    private val returnValidPdf: Boolean = true,
    private val simulateHtmlError: Boolean = false
) : SourceAdapter {

    override val adapterId: String = "test_mock_fixture"
    override val sourceName: String = "টেস্ট ফিক্সচার (Development & Tests Only)"
    override val organization: String = "Local Unit Test Harness"
    override val officialDocumentationUrl: String = "https://github.com/tapos-linx/LRgetBD"
    override val termsSummary: String = "শুধুমাত্র অটোমেটেড টেস্ট এবং ভ্যালিডেশন পাইপলাইন পরীক্ষার জন্য।"
    override val supportedSurveys: List<SurveyType> = SurveyType.values().toList()
    override val supportedRecords: List<RecordType> = RecordType.values().toList()
    override val requiresAuthentication: Boolean = false
    override val permittedDownloadMethods: List<String> = listOf("LOCAL_TEST_STREAM")
    override val rateLimitDelayMs: Long = 100L
    override val isOfficial: Boolean = false
    override val isAuthorizedForBulk: Boolean = true
    override val isTestMock: Boolean = true
    override val statusExplanationBn: String = "শুধুমাত্র টেস্ট এনভায়রনমেন্টে কার্যকর (প্রোডাকশনে নিষ্ক্রিয়)"

    override suspend fun downloadRecord(job: DownloadJob, targetTempFile: File): SourceDownloadResult {
        delay(rateLimitDelayMs)

        if (simulateHtmlError) {
            targetTempFile.writeText("<!DOCTYPE html><html><body><h1>401 Unauthorized - Login Required</h1></body></html>")
            return SourceDownloadResult.Success(
                fileSizeBytes = targetTempFile.length(),
                sourceReference = "TEST_HTML_RESPONSE",
                contentType = "text/html"
            )
        }

        if (returnValidPdf) {
            // Write a fully valid standard test PDF with valid magic header and %%EOF trailer
            val validPdfContent = buildString {
                append("%PDF-1.4\n")
                append("%âãÏÓ\n")
                append("1 0 obj <</Type /Catalog /Pages 2 0 R>> endobj\n")
                append("2 0 obj <</Type /Pages /Kids [3 0 R] /Count 1>> endobj\n")
                append("3 0 obj <</Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]>> endobj\n")
                append("xref\n0 4\n0000000000 65535 f \n0000000015 00000 n \n0000000068 00000 n \n0000000125 00000 n \n")
                append("trailer <</Size 4 /Root 1 0 R>>\nstartxref\n198\n%%EOF\n")
            }
            targetTempFile.writeBytes(validPdfContent.toByteArray(Charsets.ISO_8859_1))
            return SourceDownloadResult.Success(
                fileSizeBytes = targetTempFile.length(),
                sourceReference = "TEST_FIXTURE_RECORD_${job.mouzaId}_${job.recordIdentifier}"
            )
        } else {
            return SourceDownloadResult.Failure(
                message = "Simulated network failure for test",
                errorCode = "ERR_SIMULATED_TEST_FAILURE"
            )
        }
    }
}
