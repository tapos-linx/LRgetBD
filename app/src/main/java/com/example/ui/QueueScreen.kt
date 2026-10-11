package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus

@Composable
fun QueueScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val total = jobs.size
    val completedCount = jobs.count { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED || it.status == DownloadStatus.DUPLICATE }
    val activeJobs = jobs.filter { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PROCESSING || it.status == DownloadStatus.READY_TO_DOWNLOAD || it.status == DownloadStatus.VALIDATING }
    val pausedCount = jobs.count { it.status == DownloadStatus.PAUSED }
    val failedCount = jobs.count { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE || it.status == DownloadStatus.SOURCE_UNVERIFIED }
    val authCount = jobs.count { it.status == DownloadStatus.AUTH_REQUIRED || it.status == DownloadStatus.ACCESS_RESTRICTED }

    val overallPercentage = if (total > 0) ((completedCount.toFloat() / total.toFloat()) * 100).toInt() else 0
    val activeJob = activeJobs.firstOrNull { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PROCESSING || it.status == DownloadStatus.READY_TO_DOWNLOAD || it.status == DownloadStatus.VALIDATING }

    val filteredJobs = when (selectedFilter) {
        "ACTIVE" -> activeJobs
        "PAUSED" -> jobs.filter { it.status == DownloadStatus.PAUSED }
        "FAILED" -> jobs.filter { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE || it.status == DownloadStatus.SOURCE_UNVERIFIED }
        "AUTH" -> jobs.filter { it.status == DownloadStatus.AUTH_REQUIRED || it.status == DownloadStatus.ACCESS_RESTRICTED }
        "COMPLETED" -> jobs.filter { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED || it.status == DownloadStatus.DUPLICATE }
        else -> jobs
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Overall Queue & Batch Progress Tracker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সার্বিক সংগ্রহ অগ্রগতি (Overall Progress)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$overallPercentage%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { overallPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "মোট ফাইল: $total টি • সম্পন্ন: $completedCount টি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (activeJob != null) "বর্তমান: ${activeJob.mouzaName}" else "অপেক্ষমান",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Action Control Buttons: Pause, Resume, Cancel, Retry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { viewModel.pauseQueue() },
                enabled = activeJobs.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pause_queue_button")
            ) {
                Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.padding(end = 4.dp))
                Text("স্থগিত", style = MaterialTheme.typography.labelMedium)
            }

            FilledTonalButton(
                onClick = { viewModel.resumeQueue() },
                enabled = pausedCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .testTag("resume_queue_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.padding(end = 4.dp))
                Text("চালু", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = { viewModel.cancelQueue() },
                enabled = activeJobs.isNotEmpty() || pausedCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancel_queue_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.padding(end = 4.dp))
                Text("বাতিল", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Retry Controls Row
        if (failedCount > 0 || authCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (failedCount > 0) {
                    FilledTonalButton(
                        onClick = { viewModel.retryFailedJobs() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("retry_failed_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.padding(end = 4.dp))
                        Text("ব্যর্থ পুনরায় ($failedCount)", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (authCount > 0) {
                    FilledTonalButton(
                        onClick = { viewModel.retryAuthRequiredJobs() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("retry_auth_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry Auth", modifier = Modifier.padding(end = 4.dp))
                        Text("লগইন কাজ ($authCount)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("সব ($total)") }
            )
            FilterChip(
                selected = selectedFilter == "ACTIVE",
                onClick = { selectedFilter = "ACTIVE" },
                label = { Text("চলমান (${activeJobs.size})") }
            )
            FilterChip(
                selected = selectedFilter == "PAUSED",
                onClick = { selectedFilter = "PAUSED" },
                label = { Text("স্থগিত ($pausedCount)") }
            )
            FilterChip(
                selected = selectedFilter == "FAILED",
                onClick = { selectedFilter = "FAILED" },
                label = { Text("ব্যর্থ ($failedCount)") }
            )
        }

        // Job List with LandRecordStatusCard and 0-100% Download Meters
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("queue_job_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredJobs.isEmpty()) {
                item {
                    Text(
                        text = "কার্যসারিতে কোনো রেকর্ড নেই। এলাকা নির্বাচন থেকে নতুন সংগ্রহ শুরু করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }

            items(filteredJobs, key = { it.id }) { job ->
                LandRecordStatusCard(
                    job = job,
                    onRetry = { viewModel.retryFailedJobs() }
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
