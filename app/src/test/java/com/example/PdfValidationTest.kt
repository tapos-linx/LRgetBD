package com.example

import com.example.util.PdfValidator
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PdfValidationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `valid PDF passes validation with sha256 checksum`() {
        val pdfFile = tempFolder.newFile("valid_record.pdf")
        val validPdfContent = buildString {
            append("%PDF-1.4\n")
            append("1 0 obj <</Type /Catalog>> endobj\n")
            append("xref\n0 2\n0000000000 65535 f \n0000000015 00000 n \n")
            append("trailer <</Size 2 /Root 1 0 R>>\nstartxref\n60\n%%EOF\n")
        }
        pdfFile.writeBytes(validPdfContent.toByteArray(Charsets.ISO_8859_1))

        val result = PdfValidator.validate(pdfFile)
        assertTrue("Expected Valid result", result is PdfValidator.ValidationResult.Valid)
        val valid = result as PdfValidator.ValidationResult.Valid
        assertTrue(valid.sizeBytes > 0)
        assertEquals(64, valid.checksum.length) // 64 hex characters for SHA-256
    }

    @Test
    fun `HTML login page returned instead of PDF is rejected`() {
        val htmlFile = tempFolder.newFile("login_redirect.pdf")
        htmlFile.writeText("<!DOCTYPE html><html><head><title>Login Required</title></head><body>Please Sign in with OTP</body></html>")

        val result = PdfValidator.validate(htmlFile)
        assertTrue("Expected Invalid result for HTML error page", result is PdfValidator.ValidationResult.Invalid)
        val invalid = result as PdfValidator.ValidationResult.Invalid
        assertEquals("ERR_HTML_RESPONSE_NOT_PDF", invalid.errorCode)
    }

    @Test
    fun `empty 0-byte file is rejected`() {
        val emptyFile = tempFolder.newFile("empty.pdf")
        emptyFile.writeBytes(ByteArray(0))

        val result = PdfValidator.validate(emptyFile)
        assertTrue(result is PdfValidator.ValidationResult.Invalid)
        val invalid = result as PdfValidator.ValidationResult.Invalid
        assertEquals("ERR_FILE_EMPTY", invalid.errorCode)
    }

    @Test
    fun `random binary without PDF header is rejected`() {
        val corruptedFile = tempFolder.newFile("corrupted.pdf")
        corruptedFile.writeBytes(ByteArray(256) { it.toByte() })

        val result = PdfValidator.validate(corruptedFile)
        assertTrue(result is PdfValidator.ValidationResult.Invalid)
        val invalid = result as PdfValidator.ValidationResult.Invalid
        assertEquals("ERR_INVALID_PDF_HEADER", invalid.errorCode)
    }

    @Test
    fun `truncated PDF missing EOF marker is rejected`() {
        val truncatedFile = tempFolder.newFile("truncated.pdf")
        truncatedFile.writeBytes("%PDF-1.4\n1 0 obj <</Type /Catalog>>\nSome incomplete data here...".toByteArray())

        val result = PdfValidator.validate(truncatedFile)
        assertTrue(result is PdfValidator.ValidationResult.Invalid)
        val invalid = result as PdfValidator.ValidationResult.Invalid
        assertEquals("ERR_INCOMPLETE_PDF_STREAM", invalid.errorCode)
    }
}
