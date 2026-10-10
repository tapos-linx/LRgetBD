package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
fun ArchiveScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val archivedJobs = jobs.filter { it.status == DownloadStatus.VALIDATED }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("ফাইল ভাণ্ডার (আর্কাইভ)", style = MaterialTheme.typography.titleLarge)
        }
        
        if (archivedJobs.isEmpty()) {
            item {
                Text("আর্কাইভে কোনো ফাইল নেই।", modifier = Modifier.padding(top = 16.dp))
            }
        }
        
        items(archivedJobs) { job ->
            JobItem(job)
        }
    }
}
