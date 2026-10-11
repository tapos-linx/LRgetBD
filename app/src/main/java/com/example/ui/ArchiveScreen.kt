package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus

@Composable
fun ArchiveScreen(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val jobs by viewModel.allJobs.collectAsState()
    val isExporting by viewModel.isExportingMasterZip.collectAsState()

    val archivedJobs = jobs.filter {
        it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED || it.status == DownloadStatus.DUPLICATE
    }

    val upazilaGroups = archivedJobs.groupBy { it.upazilaName.ifBlank { "Upazila_${it.upazilaId}" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("archive_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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

        // Master Folder One-Click Download Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FolderZip,
                        contentDescription = "Master Folder Download",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "মাস্টার ফোল্ডার ডাউনলোড (Download Master Folder)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "সকল মৌজা ও খতিয়ান সহ সম্পূর্ণ মাস্টার ফোল্ডার জিপ (ZIP) আকারে ডিভাইসে ডাউনলোড ও সেভ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.exportAndDownloadMasterFolder(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_master_folder_button"),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("জিপ তৈরি হচ্ছে...")
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download Master Folder",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "মাস্টার ফোল্ডার ডাউনলোড করুন (Download Master Folder ZIP)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                FilledTonalButton(
                    onClick = { viewModel.saveMasterFolderStraightToPhoneStorage(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_straight_to_phone_storage_archive_button"),
                    enabled = !isExporting
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = "Save to Phone Storage",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "সরাসরি ফোন মেমোরিতে সেভ করুন (Save Straight to Phone Storage)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Distinct Upazila Master Folders list
        if (upazilaGroups.size > 1) {
            Text(
                text = "উপজেলা ভিত্তিক পৃথক মাস্টার ফোল্ডার:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            upazilaGroups.forEach { (uName, uJobs) ->
                FilledTonalButton(
                    onClick = { viewModel.exportAndDownloadMasterFolder(context, upazilaName = uName) },
                    modifier = Modifier.fillMaxWidth().testTag("download_upazila_${uName}_button"),
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LR_BD_Records_$uName (${uJobs.size}টি ফাইল) ডাউনলোড",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "সংরক্ষিত প্রতিটি ফাইলের এসএইচএ-২৫৬ (SHA-256) চেকসাম ও সরকারি ই-পর্চা রেকর্ড অক্ষত রাখা হয়েছে।",
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
                        text = "আর্কাইভে এখনো কোনো ফাইল সংরক্ষিত হয়নি। এলাকা নির্বাচন থেকে সংগ্রহ শুরু করুন।",
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
