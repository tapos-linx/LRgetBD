package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun ReportsScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    
    val total = jobs.size
    val validated = jobs.count { it.status == DownloadStatus.VALIDATED }
    val failed = jobs.count { it.status == DownloadStatus.FAILED }
    val authRequired = jobs.count { it.status == DownloadStatus.AUTH_REQUIRED }
    val duplicates = jobs.count { it.status == DownloadStatus.DUPLICATE }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("সংগ্রহ প্রতিবেদন", style = MaterialTheme.typography.headlineMedium)
        
        ReportItem("মোট রেকর্ড", total.toString())
        ReportItem("বৈধ রেকর্ড", validated.toString())
        ReportItem("ব্যর্থ", failed.toString())
        ReportItem("পুনরাবৃত্তি (Duplicate)", duplicates.toString())
        ReportItem("অনুমতি প্রয়োজন (Auth Required)", authRequired.toString())
        
        if (total > 0) {
            val successRate = (validated.toFloat() / total.toFloat() * 100).toInt()
            ReportItem("সাফল্যের হার", "$successRate%")
        }
    }
}

@Composable
fun ReportItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleLarge)
        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
