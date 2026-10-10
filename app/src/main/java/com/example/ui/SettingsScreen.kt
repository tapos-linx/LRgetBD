package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.AreaRepository
import com.example.model.CatalogueSource
import com.example.util.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val archiveUri by settingsManager.archiveUri.collectAsState(initial = null)
    val isDevTestMode by viewModel.isDevTestMode.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()
    val scope = rememberCoroutineScope()

    var csvImportInput by remember { mutableStateOf("") }
    var showImportBox by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Best effort persistable permission
            }
            scope.launch {
                settingsManager.saveArchiveUri(it.toString())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("সেটিংস (Settings)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // Storage Section
        Text("সংগ্রহ ফোল্ডার (Storage Access Framework)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (!archiveUri.isNullOrBlank()) "বর্তমান অবস্থান: $archiveUri" else stringResource(R.string.msg_no_archive_folder),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { launcher.launch(null) },
                    modifier = Modifier.padding(top = 10.dp).testTag("select_folder_button")
                ) {
                    Text(stringResource(R.string.msg_select_archive_folder))
                }
            }
        }

        HorizontalDivider()

        // Batch Size Configuration
        Text("ব্যাচ সাইজ কনফিগারেশন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "প্রতি ব্যাচে মৌজা সংখ্যা: $batchSize টি (ডিফল্ট: ৭ টি)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 7, 14).forEach { size ->
                        OutlinedButton(
                            onClick = { viewModel.setBatchSize(size) },
                            enabled = batchSize != size
                        ) {
                            Text("$size টি")
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Test Mode Toggle
        Text("ডেভেলপার ও টেস্ট মোড", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("অফলাইন টেস্ট ফিক্সচার মোড", fontWeight = FontWeight.Medium)
                        Text(
                            text = "সত্যিকারের সরকারি উৎস ব্যতিরেকে লোকাল পাইপলাইন পরীক্ষা করতে ব্যবহার করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDevTestMode,
                        onCheckedChange = { viewModel.setDevTestMode(it) },
                        modifier = Modifier.testTag("dev_mode_switch")
                    )
                }
            }
        }

        HorizontalDivider()

        // Custom Area Catalogue Import
        Text("মৌজা ক্যাটালগ আমদানি (CSV/JSON)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "প্রয়োজনে ব্যবহারকারী-সরবরাহকৃত সিএসভি বা জেএসন তালিকা আমদানি করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { showImportBox = !showImportBox },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(if (showImportBox) "ইনপুট বক্স বন্ধ করুন" else "ক্যাটালগ ডাটা প্রবেশ করান")
                }

                if (showImportBox) {
                    OutlinedTextField(
                        value = csvImportInput,
                        onValueChange = { csvImportInput = it },
                        label = { Text("CSV/JSON ফরম্যাট (id,upazilaId,name,bnName,jlNumber)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        maxLines = 5
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val imported = if (csvImportInput.trim().startsWith("[")) {
                                    AreaRepository.importFromJson(csvImportInput, CatalogueSource.IMPORTED)
                                } else {
                                    AreaRepository.importFromCsv(csvImportInput, CatalogueSource.IMPORTED)
                                }
                                Toast.makeText(context, "$imported টি মৌজা সফলভাবে আমদানি হয়েছে", Toast.LENGTH_SHORT).show()
                                csvImportInput = ""
                            },
                            enabled = csvImportInput.isNotBlank()
                        ) {
                            Text("আমদানি নিশ্চিত করুন")
                        }
                    }
                }
            }
        }
    }
}
