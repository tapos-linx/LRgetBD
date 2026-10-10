package com.example.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object PdfValidator {

    sealed class ValidationResult {
        data class Valid(val checksum: String, val sizeBytes: Long) : ValidationResult()
        data class Invalid(val reason: String, val errorCode: String) : ValidationResult()
    }

    /**
     * Validates that the file is a genuine PDF and not an HTML error or login page,
     * empty file, or truncated stream.
     */
    fun validate(file: File): ValidationResult {
        if (!file.exists()) {
            return ValidationResult.Invalid("ফাইলটি পাওয়া যায়নি (File not found)", "ERR_FILE_NOT_FOUND")
        }

        val size = file.length()
        if (size <= 0) {
            return ValidationResult.Invalid("ফাইলটি শূন্য বাইট বা ফাঁকা (Empty file)", "ERR_FILE_EMPTY")
        }

        if (size < 128) {
            return ValidationResult.Invalid("ফাইলের আকার অপ্রতুল (File too small to be valid PDF)", "ERR_FILE_TOO_SMALL")
        }

        // Read header bytes
        val headerBytes = ByteArray(1024.coerceAtMost(size.toInt()))
        FileInputStream(file).use { input ->
            input.read(headerBytes)
        }

        val headerText = String(headerBytes, Charsets.UTF_8).lowercase()

        // Detect HTML login / error pages
        if (headerText.contains("<!doctype html") ||
            headerText.contains("<html") ||
            headerText.contains("<head") ||
            headerText.contains("<body") ||
            headerText.contains("login") ||
            headerText.contains("sign in") ||
            headerText.contains("error 40") ||
            headerText.contains("error 50") ||
            headerText.contains("unauthorized")
        ) {
            return ValidationResult.Invalid(
                "এইচটিএমএল লগইন অথবা ত্রুটি পৃষ্ঠা পাওয়া গেছে (HTML login/error page returned instead of PDF)",
                "ERR_HTML_RESPONSE_NOT_PDF"
            )
        }

        // Check PDF Magic Number (%PDF-)
        if (headerBytes.size < 5 ||
            headerBytes[0] != 0x25.toByte() || // %
            headerBytes[1] != 0x50.toByte() || // P
            headerBytes[2] != 0x44.toByte() || // D
            headerBytes[3] != 0x46.toByte() || // F
            headerBytes[4] != 0x2D.toByte()    // -
        ) {
            return ValidationResult.Invalid(
                "অবৈধ পিডিএফ হেডার বা সিগনেচার (Invalid PDF signature header)",
                "ERR_INVALID_PDF_HEADER"
            )
        }

        // Check for PDF EOF trailer in the last 1024 bytes
        val tailSize = 1024.coerceAtMost(size.toInt())
        val tailBytes = ByteArray(tailSize)
        FileInputStream(file).use { input ->
            input.skip(size - tailSize)
            input.read(tailBytes)
        }
        val tailText = String(tailBytes, Charsets.ISO_8859_1)
        if (!tailText.contains("%%EOF") && !tailText.contains("startxref")) {
            return ValidationResult.Invalid(
                "অসম্পূর্ণ পিডিএফ স্ট্রিম (PDF EOF marker missing / incomplete download)",
                "ERR_INCOMPLETE_PDF_STREAM"
            )
        }

        // Compute SHA-256 Checksum
        val checksum = calculateSha256(file)
        return ValidationResult.Valid(checksum = checksum, sizeBytes = size)
    }

    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }
}
