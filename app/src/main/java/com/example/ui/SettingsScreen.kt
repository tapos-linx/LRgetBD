package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.util.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsManager = SettingsManager(context)
    val archiveUri by settingsManager.archiveUri.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Take persistable permission
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            scope.launch {
                settingsManager.saveArchiveUri(it.toString())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("সেটিংস", style = MaterialTheme.typography.headlineMedium)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("সংগ্রহ ফোল্ডার", style = MaterialTheme.typography.titleMedium)
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = archiveUri ?: stringResource(R.string.msg_no_archive_folder),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { launcher.launch(null) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.msg_select_archive_folder))
                }
            }
        }
    }
}
