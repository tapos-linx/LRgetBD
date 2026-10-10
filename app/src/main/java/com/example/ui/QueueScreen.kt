package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
fun QueueScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    
    val activeJobs = jobs.filter { 
        it.status == DownloadStatus.PENDING || 
        it.status == DownloadStatus.DOWNLOADING || 
        it.status == DownloadStatus.VALIDATING 
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("চলমান কাজের তালিকা", style = MaterialTheme.typography.titleLarge)
        }
        
        if (activeJobs.isEmpty()) {
            item {
                Text("বর্তমানে কোনো কাজ চলছে না।", modifier = Modifier.padding(top = 16.dp))
            }
        }
        
        items(activeJobs) { job ->
            JobItem(job)
        }
    }
}
