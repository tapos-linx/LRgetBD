package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DownloadJob
import com.example.model.DownloadStatus

@Composable
fun JobItem(job: DownloadJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (job.mouzaName.isNotBlank()) "${job.mouzaName} (জে.এল. ${job.jlNumber})" else "মৌজা: ${job.mouzaId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(job.status)
            }

            Text(
                text = "এলাকা: ${job.divisionName} > ${job.districtName} > ${job.upazilaName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "জরিপ: ${job.surveyType.displayName} • রেকর্ড: ${job.recordType.bnName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "উৎস: ${job.sourceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (job.status == DownloadStatus.DOWNLOADING || job.status == DownloadStatus.VALIDATING) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            if (job.checksum != null) {
                Text(
                    text = "SHA-256: ${job.checksum.take(16)}... • ${job.fileSizeBytes} B",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (job.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = "${job.errorCode ?: "ত্রুটি"}: ${job.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: DownloadStatus) {
    val (bgColor, textColor) = when (status) {
        DownloadStatus.VALIDATED, DownloadStatus.VERIFIED ->
            Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        DownloadStatus.DOWNLOADING, DownloadStatus.VALIDATING ->
            Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        DownloadStatus.PENDING ->
            Pair(Color(0xFFFFF8E1), Color(0xFFF57F17))
        DownloadStatus.PAUSED ->
            Pair(Color(0xFFECEFF1), Color(0xFF455A64))
        DownloadStatus.FAILED, DownloadStatus.INCOMPLETE ->
            Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        DownloadStatus.DUPLICATE ->
            Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
        DownloadStatus.AUTH_REQUIRED ->
            Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        DownloadStatus.SOURCE_UNVERIFIED, DownloadStatus.ACCESS_RESTRICTED ->
            Pair(Color(0xFFFBE9E7), Color(0xFFD84315))
        DownloadStatus.CANCELLED ->
            Pair(Color(0xFFEEEEEE), Color(0xFF757575))
        else ->
            Pair(Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = status.bnTitle,
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
