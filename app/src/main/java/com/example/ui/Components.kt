package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.DownloadJob
import com.example.model.DownloadStatus

@Composable
fun JobItem(job: DownloadJob) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("মৌজা আইডি: ${job.mouzaId}", style = MaterialTheme.typography.bodyLarge)
                Text(job.status.name, style = MaterialTheme.typography.labelMedium)
            }
            Text("জরিপ: ${job.surveyType.name}", style = MaterialTheme.typography.bodySmall)
            
            if (job.status == DownloadStatus.DOWNLOADING) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
            
            if (job.errorMessage != null) {
                Text(
                    text = job.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
