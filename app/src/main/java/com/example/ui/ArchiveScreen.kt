package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus

@Composable
fun ArchiveScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val archivedJobs = jobs.filter {
        it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED || it.status == DownloadStatus.DUPLICATE
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("archive_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ফাইল ভাণ্ডার (আর্কাইভ)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${archivedJobs.size} টি সংরক্ষিত",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "সংরক্ষিত প্রতিটি ফাইলের এসএইচএ-২৫৬ (SHA-256) চেকসাম ও অরিজিনাল বাইট অক্ষত রাখা হয়েছে।",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("archive_job_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (archivedJobs.isEmpty()) {
                item {
                    Text(
                        text = "আর্কাইভে এখনো কোনো ফাইল সংরক্ষিত হয়নি।",
                        modifier = Modifier.padding(top = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(archivedJobs, key = { it.id }) { job ->
                JobItem(job)
            }
        }
    }
}
