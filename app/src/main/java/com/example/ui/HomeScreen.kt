package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus

@Composable
fun HomeScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    
    val total = jobs.size
    val completed = jobs.count { it.status == DownloadStatus.COMPLETED || it.status == DownloadStatus.VALIDATED }
    val failed = jobs.count { it.status == DownloadStatus.FAILED }
    val pending = jobs.count { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "সংগ্রহের সংক্ষিপ্ত বিবরণ",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "মোট কাজ",
                count = total.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "সম্পন্ন",
                count = completed.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "বাকি আছে",
                count = pending.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            SummaryCard(
                title = "ব্যর্থ",
                count = failed.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        
        if (total == 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, null)
                    Text(
                        "এখনো কোনো তথ্য সংগ্রহ করা হয়নি। শুরু করতে এলাকা নির্বাচন করুন।",
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(count, style = MaterialTheme.typography.displaySmall)
        }
    }
}
