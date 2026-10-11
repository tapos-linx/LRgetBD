package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: AppViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val jobs by viewModel.allJobs.collectAsState()
    val isDevTestMode by viewModel.isDevTestMode.collectAsState()
    val isExporting by viewModel.isExportingMasterZip.collectAsState()

    val total = jobs.size
    val validated = jobs.count { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED }
    val duplicates = jobs.count { it.status == DownloadStatus.DUPLICATE }
    val failed = jobs.count { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE }
    val authRequired = jobs.count { it.status == DownloadStatus.AUTH_REQUIRED }
    val pending = jobs.count { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.VALIDATING }
    val unverified = jobs.count { it.status == DownloadStatus.SOURCE_UNVERIFIED || it.status == DownloadStatus.ACCESS_RESTRICTED }

    val lastCompletedJob = jobs.filter { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED }
        .maxByOrNull { it.updatedAt }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LRgetBD — ভূমি রেকর্ড সংগ্রাহক",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Source Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isDevTestMode) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Source Status",
                        tint = if (isDevTestMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isDevTestMode) "মোড: অফলাইন টেস্ট ফিক্সচার" else "উৎস: সরকারি ই-পর্চা (ePorcha.gov.bd)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = if (isDevTestMode)
                        "লোকাল টেস্ট মোড সক্রিয়। কোনো বাস্তব সরকারি ডাউনলোডের দাবি করা হবে না।"
                    else
                        "সরকারি নাগরিক পোর্টাল — উন্মুক্ত ও বিনামূল্যে সরাসরি সকল খতিয়ান ও মৌজা রেকর্ড ডাউনলোড সেবা সক্রিয়।",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Master Folder Download Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "মাস্টার ফোল্ডার ডাউনলোড",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "সকল মৌজা ও খতিয়ান সরাসরি ফোন মেমোরিতে (Downloads) সংরক্ষণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = { viewModel.exportAndDownloadMasterFolder(context) },
                        enabled = !isExporting,
                        modifier = Modifier.testTag("home_download_master_folder_button")
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("ডাউনলোড", fontWeight = FontWeight.Bold)
                    }
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                androidx.compose.material3.FilledTonalButton(
                    onClick = { viewModel.saveMasterFolderStraightToPhoneStorage(context) },
                    enabled = !isExporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_save_straight_to_phone_button")
                ) {
                    Icon(
                        Icons.Default.FolderZip,
                        contentDescription = "Save Straight to Phone Storage",
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("সরাসরি ফোন মেমোরিতে সেভ করুন (Save to Phone Storage)", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "মোট কাজ",
                count = total.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "বৈধ ও সংরক্ষিত",
                count = validated.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "চলমান / অপেক্ষমান",
                count = pending.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            SummaryCard(
                title = "পুনরাবৃত্তি (Duplicate)",
                count = duplicates.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "ব্যর্থ ও অসম্পূর্ণ",
                count = failed.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.errorContainer
            )
            SummaryCard(
                title = "লগইন / সীমাবদ্ধ",
                count = (authRequired + unverified).toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Active Request Progress Status Card (0 to 100% Download Meter)
        val activeJob = jobs.firstOrNull { 
            it.status == DownloadStatus.DOWNLOADING || 
            it.status == DownloadStatus.PROCESSING || 
            it.status == DownloadStatus.READY_TO_DOWNLOAD || 
            it.status == DownloadStatus.VALIDATING ||
            it.status == DownloadStatus.PENDING 
        } ?: lastCompletedJob

        if (activeJob != null) {
            Text(
                text = "রেকর্ড অনুরোধের অগ্রগতি (Request Status & Meter)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            LandRecordStatusCard(
                job = activeJob,
                onRetry = { viewModel.retryFailedJobs() }
            )
        }

        // Last activity
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "সর্বশেষ সফল কার্যকলাপ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (lastCompletedJob != null) {
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastCompletedJob.updatedAt))
                    Text(
                        text = "মৌজা: ${lastCompletedJob.mouzaName} (জে.এল. ${lastCompletedJob.jlNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "সময়: $dateFormatted • আকার: ${lastCompletedJob.fileSizeBytes} বাইট",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "এখনো কোনো ফাইল সংরক্ষণ সম্পন্ন হয়নি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Storage & Ethics Notice
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "ফ্রি-ফার্স্ট নীতিমালা: ডাউনলোডকৃত সকল ফাইল ফোনে সংরক্ষিত থাকে। কোনো পেইড সাবস্ক্রিপশন বা ক্লাউড বাধ্যতামূলক নয়।",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
