package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus

@Composable
fun HistoryScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val historyJobs = jobs.filter { 
        it.status == DownloadStatus.VALIDATED || 
        it.status == DownloadStatus.COMPLETED ||
        it.status == DownloadStatus.FAILED ||
        it.status == DownloadStatus.DUPLICATE
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("সংগ্রহ ইতিহাস", style = MaterialTheme.typography.titleLarge)
        }
        
        if (historyJobs.isEmpty()) {
            item {
                Text("কোনো ইতিহাস পাওয়া যায়নি।", modifier = Modifier.padding(top = 16.dp))
            }
        }
        
        items(historyJobs) { job ->
            JobItem(job)
        }
    }
}
