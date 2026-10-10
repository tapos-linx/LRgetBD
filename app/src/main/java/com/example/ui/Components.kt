package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DownloadJob
import com.example.model.DownloadStatus
import java.io.File

/**
 * Comprehensive Status Card displaying the full lifecycle of a Land Record Request.
 * Features:
 * - States: 'Pending', 'Processing', 'Ready to Download', 'Validating', 'Ready / Saved'
 * - Download Meter: 0 to 100% with animated progress and percentage badge
 * - Step Stage Flow Indicator (অনুরোধ -> প্রক্রিয়াকরণ -> ডাউনলোড -> যাচাইকৃত)
 * - Cryptographic SHA-256 fingerprint & size meter
 * - Action affordances (Open File, Share, Retry)
 */
@Composable
fun LandRecordStatusCard(
    job: DownloadJob,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val animatedProgress by animateFloatAsState(
        targetValue = (job.progressPercent.coerceIn(0, 100)) / 100f,
        label = "DownloadProgressAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("land_record_status_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, JL Number, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (job.mouzaName.isNotBlank())
                            "${job.mouzaName} (জে.এল. ${job.jlNumber})"
                        else
                            "মৌজা আইডি: ${job.mouzaId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${job.divisionName} > ${job.districtName} > ${job.upazilaName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(job.status)
            }

            // Record & Survey Specifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "জরিপ: ${job.surveyType.displayName} • রেকর্ড: ${job.recordType.bnName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "উৎস: ${job.sourceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 0% to 100% Download Meter Section
            Spacer(modifier = Modifier.height(12.dp))
            DownloadMeterSection(
                job = job,
                animatedProgress = animatedProgress
            )

            // Multi-Stage Lifecycle Stepper
            Spacer(modifier = Modifier.height(10.dp))
            StageFlowStepper(status = job.status)

            // Stage Description Subtitle
            if (job.stageDescription.isNotBlank()) {
                Text(
                    text = job.stageDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Checksum & File Size (when validated or duplicate)
            if (job.checksum != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "SHA-256: ${job.checksum.take(24)}...",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "আকার: ${job.fileSizeBytes} বাইট (${formatFileSize(job.fileSizeBytes)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error notice (if failed or unverified)
            if (job.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${job.errorCode ?: "ত্রুটি"}: ${job.errorMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Action Buttons Row
            ActionButtonsRow(
                job = job,
                context = context,
                onRetry = onRetry
            )
        }
    }
}

/**
 * Download Meter Section with 0 to 100% Progress Bar and Percentage Indicator
 */
@Composable
fun DownloadMeterSection(
    job: DownloadJob,
    animatedProgress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (job.status) {
                        DownloadStatus.DOWNLOADING, DownloadStatus.PROCESSING -> Icons.Default.Sync
                        DownloadStatus.READY_TO_DOWNLOAD -> Icons.Default.Download
                        DownloadStatus.VALIDATED, DownloadStatus.VERIFIED -> Icons.Default.CheckCircle
                        DownloadStatus.PENDING -> Icons.Default.HourglassEmpty
                        else -> Icons.Default.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ডাউনলোড মিটার (Download Meter)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Circular / Box Percentage Meter: 0% - 100%
            Surface(
                color = if (job.progressPercent == 100)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${job.progressPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (job.progressPercent == 100)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Linear Progress Gauge: 0 to 100%
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("download_progress_bar"),
            color = if (job.progressPercent == 100)
                Color(0xFF2E7D32)
            else
                MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * 4-Step Lifecycle Stepper
 * States: 1. অনুরোধ (Pending) -> 2. প্রক্রিয়াকরণ (Processing) -> 3. ডাউনলোড (Ready/Downloading) -> 4. যাচাইকৃত (Ready/Saved)
 */
@Composable
fun StageFlowStepper(status: DownloadStatus) {
    val activeStep = when (status) {
        DownloadStatus.PENDING -> 1
        DownloadStatus.PROCESSING -> 2
        DownloadStatus.DOWNLOADING, DownloadStatus.READY_TO_DOWNLOAD -> 3
        DownloadStatus.VALIDATING, DownloadStatus.VALIDATED, DownloadStatus.VERIFIED, DownloadStatus.DUPLICATE -> 4
        else -> 1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepNode(stepNumber = 1, title = "অনুরোধ", isActive = activeStep >= 1, isCurrent = activeStep == 1)
        StepLine(isActive = activeStep >= 2)
        StepNode(stepNumber = 2, title = "প্রক্রিয়া", isActive = activeStep >= 2, isCurrent = activeStep == 2)
        StepLine(isActive = activeStep >= 3)
        StepNode(stepNumber = 3, title = "ডাউনলোড", isActive = activeStep >= 3, isCurrent = activeStep == 3)
        StepLine(isActive = activeStep >= 4)
        StepNode(stepNumber = 4, title = "প্রস্তুত", isActive = activeStep >= 4, isCurrent = activeStep == 4)
    }
}

@Composable
fun StepNode(stepNumber: Int, title: String, isActive: Boolean, isCurrent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StepLine(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            )
    )
}

/**
 * Action Buttons for Open, Share, and Retry
 */
@Composable
fun ActionButtonsRow(
    job: DownloadJob,
    context: Context,
    onRetry: (() -> Unit)? = null
) {
    val isReady = job.status == DownloadStatus.VALIDATED || job.status == DownloadStatus.VERIFIED || job.status == DownloadStatus.DUPLICATE
    val isFailed = job.status == DownloadStatus.FAILED || job.status == DownloadStatus.INCOMPLETE

    if (isReady || isFailed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isReady) {
                FilledTonalButton(
                    onClick = {
                        val path = job.filePath
                        if (!path.isNullOrBlank()) {
                            val file = File(path)
                            if (file.exists()) {
                                Toast.makeText(context, "সংরক্ষিত ফাইল: ${file.name}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "ফাইল আর্カイভে সংরক্ষিত: $path", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "রেকর্ড ভ্যালিডেশন সম্পন্ন।", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("view_record_button")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ফাইল দেখুন (View)", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        val shareText = "LRgetBD ভূমি রেকর্ড: ${job.mouzaName} (জে.এল. ${job.jlNumber}), জরিপ: ${job.surveyType.displayName}, SHA-256: ${job.checksum ?: "N/A"}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Land Record - ${job.mouzaName}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "রেকর্ড শেয়ার করুন"))
                    },
                    modifier = Modifier.weight(1f).testTag("share_record_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("শেয়ার (Share)", style = MaterialTheme.typography.labelMedium)
                }
            } else if (isFailed && onRetry != null) {
                FilledTonalButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().testTag("retry_record_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পুনরায় চেষ্টা করুন (Retry)", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Backwards-compatible JobItem delegating to the rich LandRecordStatusCard
 */
@Composable
fun JobItem(job: DownloadJob) {
    LandRecordStatusCard(job = job)
}

@Composable
fun StatusBadge(status: DownloadStatus) {
    val (bgColor, textColor, label) = when (status) {
        DownloadStatus.PENDING ->
            Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Pending (অপেক্ষমান)")
        DownloadStatus.PROCESSING ->
            Triple(Color(0xFFE0F7FA), Color(0xFF00838F), "Processing (প্রক্রিয়াধীন)")
        DownloadStatus.DOWNLOADING ->
            Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Downloading (ডাউনলোড হচ্ছে)")
        DownloadStatus.READY_TO_DOWNLOAD ->
            Triple(Color(0xFFE8EAF6), Color(0xFF283593), "Ready to Download (প্রস্তুত)")
        DownloadStatus.VALIDATING ->
            Triple(Color(0xFFF3E5F5), Color(0xFF6A1B9A), "Validating (যাচাইকরণ)")
        DownloadStatus.VALIDATED, DownloadStatus.VERIFIED ->
            Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Ready & Saved (সংরক্ষিত)")
        DownloadStatus.PAUSED ->
            Triple(Color(0xFFECEFF1), Color(0xFF455A64), "Paused (স্থগিত)")
        DownloadStatus.FAILED, DownloadStatus.INCOMPLETE ->
            Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Failed (ব্যর্থ)")
        DownloadStatus.DUPLICATE ->
            Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), "Duplicate (বিদ্যমান)")
        DownloadStatus.AUTH_REQUIRED ->
            Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Auth Required (লগইন আবশ্যক)")
        DownloadStatus.SOURCE_UNVERIFIED, DownloadStatus.ACCESS_RESTRICTED ->
            Triple(Color(0xFFFBE9E7), Color(0xFFD84315), "Source Unverified (অযাচাইকৃত)")
        DownloadStatus.CANCELLED ->
            Triple(Color(0xFFEEEEEE), Color(0xFF757575), "Cancelled (বাতিলকৃত)")
        else ->
            Triple(Color(0xFFF5F5F5), Color(0xFF616161), status.bnTitle)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
    }
}
