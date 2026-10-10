package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
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

    val filteredJobs = when (selectedFilter) {
        "ACTIVE" -> jobs.filter { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.VALIDATING }
        "PAUSED" -> jobs.filter { it.status == DownloadStatus.PAUSED }
        "FAILED" -> jobs.filter { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE || it.status == DownloadStatus.SOURCE_UNVERIFIED }
        "AUTH" -> jobs.filter { it.status == DownloadStatus.AUTH_REQUIRED || it.status == DownloadStatus.ACCESS_RESTRICTED }
        "COMPLETED" -> jobs.filter { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED || it.status == DownloadStatus.DUPLICATE }
        else -> jobs
    }

    val activeCount = jobs.count { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.VALIDATING }
    val pausedCount = jobs.count { it.status == DownloadStatus.PAUSED }
    val failedCount = jobs.count { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE || it.status == DownloadStatus.SOURCE_UNVERIFIED }
    val authCount = jobs.count { it.status == DownloadStatus.AUTH_REQUIRED || it.status == DownloadStatus.ACCESS_RESTRICTED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "কার্যসারি (মোট ${jobs.size} টি)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Action Control Buttons: Pause, Resume, Cancel, Retry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { viewModel.pauseQueue() },
                enabled = activeCount > 0,
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
                enabled = activeCount > 0 || pausedCount > 0,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancel_queue_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.padding(end = 4.dp))
                Text("বাতিল", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Retry Row
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
                label = { Text("সব (${jobs.size})") }
            )
            FilterChip(
                selected = selectedFilter == "ACTIVE",
                onClick = { selectedFilter = "ACTIVE" },
                label = { Text("চলমান ($activeCount)") }
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

        // Job List
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
                JobItem(job)
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
