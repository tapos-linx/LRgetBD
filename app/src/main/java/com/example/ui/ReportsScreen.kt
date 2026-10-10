package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DownloadStatus
import com.example.util.ArchiveFileManager

@Composable
fun ReportsScreen(viewModel: AppViewModel = viewModel()) {
    val jobs by viewModel.allJobs.collectAsState()
    val context = LocalContext.current
    val archiveManager = ArchiveFileManager(context)

    val total = jobs.size
    val validated = jobs.count { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED }
    val duplicates = jobs.count { it.status == DownloadStatus.DUPLICATE }
    val failed = jobs.count { it.status == DownloadStatus.FAILED || it.status == DownloadStatus.INCOMPLETE }
    val authRequired = jobs.count { it.status == DownloadStatus.AUTH_REQUIRED }
    val unverified = jobs.count { it.status == DownloadStatus.SOURCE_UNVERIFIED || it.status == DownloadStatus.ACCESS_RESTRICTED }
    val pending = jobs.count { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING }

    val totalBytes = jobs.filter { it.status == DownloadStatus.VALIDATED || it.status == DownloadStatus.VERIFIED }
        .sumOf { it.fileSizeBytes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "সংগ্রহ ও আর্কাইভ প্রতিবেদন",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "প্রতিবেদনে সংরক্ষিত প্রতিটি রেকর্ডের হ্যাশ, অবস্থান ও আকার মিলিয়ে যাচাই করা হয়।",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(10.dp)
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                ReportItem("মোট আবিষ্কৃত কাজ", "$total টি")
                ReportItem("সফল ও যাচাইকৃত ফাইল", "$validated টি")
                ReportItem("পুনরাবৃত্তি (Duplicate ফাইল)", "$duplicates টি")
                ReportItem("ব্যর্থ ও অসম্পূর্ণ রেকর্ড", "$failed টি")
                ReportItem("লগইন / ওটিপি আবশ্যক", "$authRequired টি")
                ReportItem("অযাচাইকৃত / সীমাবদ্ধ উৎস", "$unverified টি")
                ReportItem("কার্যসারিতে অপেক্ষমান", "$pending টি")
                ReportItem("মোট সংরক্ষিত ডাটা আকার", "$totalBytes বাইট (${totalBytes / 1024} KB)")

                if (total > 0) {
                    val rate = (validated.toFloat() / total.toFloat() * 100).toInt()
                    ReportItem("সংগ্রহ সমাপ্তির হার", "$rate%")
                }
            }
        }

        Text("মেটাডাটা এক্সপোর্ট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val csv = archiveManager.exportToCsv(jobs)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("LRgetBD_CSV", csv))
                    Toast.makeText(context, "CSV ক্লিপবোর্ডে কপি হয়েছে", Toast.LENGTH_SHORT).show()
                },
                enabled = jobs.isNotEmpty(),
                modifier = Modifier.weight(1f).testTag("export_csv_button")
            ) {
                Text("রপ্তানি (CSV)")
            }

            OutlinedButton(
                onClick = {
                    val json = archiveManager.exportToJson(jobs)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("LRgetBD_JSON", json))
                    Toast.makeText(context, "JSON ক্লিপবোর্ডে কপি হয়েছে", Toast.LENGTH_SHORT).show()
                },
                enabled = jobs.isNotEmpty(),
                modifier = Modifier.weight(1f).testTag("export_json_button")
            ) {
                Text("রপ্তানি (JSON)")
            }
        }
    }
}

@Composable
fun ReportItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
