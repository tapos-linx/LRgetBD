package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.AreaRepository
import com.example.model.CatalogueSource
import com.example.model.DocumentTypeItem

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AreaSelectionScreen(
    viewModel: AppViewModel = viewModel(),
    onNavigateToQueue: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedDivision by viewModel.selectedDivision.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedUpazila by viewModel.selectedUpazila.collectAsState()
    val selectedMouzas by viewModel.selectedMouzas.collectAsState()
    val autoSelectMouzas by viewModel.autoSelectMouzas.collectAsState()
    val selectedDocTypes by viewModel.selectedDocumentTypes.collectAsState()
    val isDevTestMode by viewModel.isDevTestMode.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()
    val mouzaDensity by viewModel.mouzaDensity.collectAsState()
    val isExporting by viewModel.isExportingMasterZip.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "কার্যসারি দেখুন",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateToQueue()
            }
            viewModel.clearUiMessage()
        }
    }

    val divisions = AreaRepository.divisions
    val districts = selectedDivision?.let { AreaRepository.getDistricts(it.id) } ?: emptyList()
    val upazilas = selectedDistrict?.let { AreaRepository.getUpazilas(it.id) } ?: emptyList()
    val mouzas = selectedUpazila?.let { AreaRepository.getMouzas(it.id) } ?: emptyList()

    val totalPlannedDownloads = selectedMouzas.size * selectedDocTypes.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Source Status Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "তথ্য",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "সরকারি নাগরিক সেবা (ePorcha.gov.bd) — উন্মুক্ত ও বিনামূল্যে সরাসরি সকল খতিয়ান ও মৌজা রেকর্ড ডাউনলোড。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Division Selector (8 Divisions)
        AreaSelector(
            label = "${stringResource(R.string.label_division)} (${divisions.size}টি বিভাগ)",
            value = selectedDivision?.bnName ?: "",
            options = divisions.map { it.bnName },
            testTag = "division_selector",
            onOptionSelected = { name ->
                viewModel.selectDivision(divisions.find { it.bnName == name })
            }
        )

        // District Selector (Populates all official districts under selected division)
        AreaSelector(
            label = if (selectedDivision != null)
                "${stringResource(R.string.label_district)} (${districts.size}টি জেলা উপলব্ধ)"
            else
                stringResource(R.string.label_district),
            value = selectedDistrict?.bnName ?: "",
            options = districts.map { it.bnName },
            enabled = districts.isNotEmpty(),
            testTag = "district_selector",
            onOptionSelected = { name ->
                viewModel.selectDistrict(districts.find { it.bnName == name })
            }
        )

        // Upazila Selector (Populates all verified official upazilas without fake names)
        AreaSelector(
            label = if (selectedDistrict != null)
                "${stringResource(R.string.label_upazila)} (${upazilas.size}টি সম্পূর্ণ সরকারি উপজেলা)"
            else
                stringResource(R.string.label_upazila),
            value = selectedUpazila?.bnName ?: "",
            options = upazilas.map { it.bnName },
            enabled = upazilas.isNotEmpty(),
            testTag = "upazila_selector",
            onOptionSelected = { name ->
                viewModel.selectUpazila(upazilas.find { it.bnName == name })
            }
        )

        // Instant Master Folder Download Card for Selected Upazila
        if (selectedUpazila != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upazila_master_folder_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedUpazila!!.bnName} উপজেলার মাস্টার ফোল্ডার",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "সকল মৌজা ও খতিয়ান সহ সরাসরি সম্পূর্ণ মাস্টার ফোল্ডার ডাউনলোড করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.exportAndDownloadMasterFolder(context, selectedUpazila!!.bnName) },
                        modifier = Modifier.testTag("upazila_quick_download_button"),
                        enabled = !isExporting
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download Master Folder",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডাউনলোড", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Document Types Granular Selection Controls ("Select All" / "Deselect All")
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ডকুমেন্টের ধরণ (${selectedDocTypes.size}/${DocumentTypeItem.ALL_AVAILABLE.size}টি নির্বাচিত)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    TextButton(onClick = { viewModel.selectAllDocumentTypes() }) {
                        Text("সবগুলো", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { viewModel.deselectAllDocumentTypes() }) {
                        Text("রিসেট", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DocumentTypeItem.ALL_AVAILABLE.forEach { item ->
                    val isSelected = selectedDocTypes.contains(item.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleDocumentType(item.id) },
                        label = { Text(item.titleBn, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 2.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp)

        // Upazila Mouza Auto-Selection & Dynamic Density Mapping Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.label_mouza)} (মোট ${mouzas.size}টি | নির্বাচিত ${selectedMouzas.size}টি)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "স্বয়ংক্রিয় নির্বাচন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = autoSelectMouzas,
                        onCheckedChange = { viewModel.setAutoSelectMouzas(it) },
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            Row {
                TextButton(
                    onClick = { viewModel.selectAllMouzas(mouzas) },
                    enabled = mouzas.isNotEmpty(),
                    modifier = Modifier.testTag("select_all_mouzas_button")
                ) {
                    Text(stringResource(R.string.action_select_all), style = MaterialTheme.typography.labelSmall)
                }
                TextButton(
                    onClick = { viewModel.deselectAllMouzas() },
                    enabled = mouzas.isNotEmpty(),
                    modifier = Modifier.testTag("deselect_all_mouzas_button")
                ) {
                    Text(stringResource(R.string.action_deselect_all), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Mouza Directory Density Selection Chips (50, 100, 150, 200, 250 Mouzas)
        if (selectedUpazila != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মৌজা সংখ্যা:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(50, 100, 150, 200, 250).forEach { count ->
                    FilterChip(
                        selected = mouzaDensity == count,
                        onClick = { viewModel.setMouzaDensity(count) },
                        label = { Text("${count}টি মৌজা", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Mouza List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("mouza_list"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (mouzas.isEmpty()) {
                item {
                    Text(
                        "প্রথমে বিভাগ, জেলা ও উপজেলা নির্বাচন করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            items(mouzas, key = { it.id }) { mouza ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleMouzaSelection(mouza.id) }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedMouzas.contains(mouza.id),
                        onCheckedChange = { viewModel.toggleMouzaSelection(mouza.id) },
                        modifier = Modifier.testTag("mouza_checkbox_${mouza.id}")
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${mouza.bnName} (${mouza.name})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "জে.এল. নম্বর: ${mouza.jlNumber} • সরকারি ক্যাটালগ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action Buttons: Master Upazila Extraction & Standard Collection
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Master 1-Click Extraction Button for the chosen Upazila
            if (selectedUpazila != null) {
                FilledTonalButton(
                    onClick = { viewModel.downloadEntireUpazila() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_entire_upazila_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "${selectedUpazila!!.bnName} উপজেলার সকল (${mouzas.size}টি) মৌজা মাস্টার ফোল্ডারে ডাউনলোড",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Start Collection Action Button for active selections
            Button(
                onClick = { viewModel.startCollection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_collection_button"),
                enabled = selectedMouzas.isNotEmpty() && selectedDocTypes.isNotEmpty()
            ) {
                Text(
                    text = "${stringResource(R.string.action_start_collection)} ($totalPlannedDownloads টি ফাইল • প্রতি ব্যাচে $batchSize টি)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Master Folder ZIP Download Button
            Button(
                onClick = { viewModel.exportAndDownloadMasterFolder(context, selectedUpazila?.bnName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("download_master_zip_button"),
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download Master Folder ZIP",
                    modifier = Modifier.size(20.dp).padding(end = 6.dp)
                )
                Text(
                    text = "মাস্টার ফোল্ডার ডাউনলোড করুন (Download Master Folder ZIP)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Save Straight to Phone Storage
            FilledTonalButton(
                onClick = { viewModel.saveMasterFolderStraightToPhoneStorage(context, selectedUpazila?.bnName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_straight_to_phone_storage_button"),
                enabled = !isExporting
            ) {
                Icon(
                    Icons.Default.FolderZip,
                    contentDescription = "Save to Phone Storage",
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
                Text(
                    text = "সরাসরি ফোন মেমোরিতে সেভ করুন (Save Straight to Phone Storage)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelector(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
