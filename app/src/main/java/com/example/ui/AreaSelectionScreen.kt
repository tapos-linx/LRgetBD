package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.model.RecordType
import com.example.model.SurveyType

@Composable
fun AreaSelectionScreen(viewModel: AppViewModel = viewModel()) {
    val selectedDivision by viewModel.selectedDivision.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedUpazila by viewModel.selectedUpazila.collectAsState()
    val selectedMouzas by viewModel.selectedMouzas.collectAsState()
    val selectedSurveyType by viewModel.selectedSurveyType.collectAsState()
    val selectedRecordType by viewModel.selectedRecordType.collectAsState()
    val isDevTestMode by viewModel.isDevTestMode.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val divisions = AreaRepository.divisions
    val districts = selectedDivision?.let { AreaRepository.getDistricts(it.id) } ?: emptyList()
    val upazilas = selectedDistrict?.let { AreaRepository.getUpazilas(it.id) } ?: emptyList()
    val mouzas = selectedUpazila?.let { AreaRepository.getMouzas(it.id) } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Source Status Banner
        if (!isDevTestMode) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "তথ্য",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "উৎস স্থিতি: সরকারি ই-পর্চা পোর্টাল। অননুমোদিত বাল্ক স্ক্র্যাপিং বা ভুয়া রেকর্ড তৈরি বন্ধ রয়েছে।",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "সতর্কতা",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "অফলাইন টেস্ট ফিক্সচার মোড সক্রিয় (শুধুমাত্র পাইপলাইন ও আর্কিটেকচার পরীক্ষার জন্য)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Division Selector
        AreaSelector(
            label = stringResource(R.string.label_division),
            value = selectedDivision?.bnName ?: "",
            options = divisions.map { it.bnName },
            testTag = "division_selector",
            onOptionSelected = { name ->
                viewModel.selectDivision(divisions.find { it.bnName == name })
            }
        )

        // District Selector
        AreaSelector(
            label = stringResource(R.string.label_district),
            value = selectedDistrict?.bnName ?: "",
            options = districts.map { it.bnName },
            enabled = districts.isNotEmpty(),
            testTag = "district_selector",
            onOptionSelected = { name ->
                viewModel.selectDistrict(districts.find { it.bnName == name })
            }
        )

        // Upazila Selector
        AreaSelector(
            label = stringResource(R.string.label_upazila),
            value = selectedUpazila?.bnName ?: "",
            options = upazilas.map { it.bnName },
            enabled = upazilas.isNotEmpty(),
            testTag = "upazila_selector",
            onOptionSelected = { name ->
                viewModel.selectUpazila(upazilas.find { it.bnName == name })
            }
        )

        // Survey & Record Types
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text("জরিপ ধরণ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(SurveyType.CS, SurveyType.SA, SurveyType.RS, SurveyType.BS).forEach { type ->
                        OutlinedCard(
                            onClick = { viewModel.setSurveyType(type) },
                            modifier = Modifier.weight(1f),
                            border = if (selectedSurveyType == type) {
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            }
                        ) {
                            Text(
                                type.name,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedSurveyType == type) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(0.8f)) {
                Text("রেকর্ড ধরণ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(RecordType.KHATIAN, RecordType.MAP).forEach { type ->
                        OutlinedCard(
                            onClick = { viewModel.setRecordType(type) },
                            modifier = Modifier.weight(1f),
                            border = if (selectedRecordType == type) {
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            }
                        ) {
                            Text(
                                if (type == RecordType.KHATIAN) "খতিয়ান" else "নকশা",
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedRecordType == type) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // Mouza List Header & Batch Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.label_mouza) + " (${selectedMouzas.size}/${mouzas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row {
                TextButton(
                    onClick = { viewModel.selectAllMouzas(mouzas) },
                    enabled = mouzas.isNotEmpty(),
                    modifier = Modifier.testTag("select_all_mouzas_button")
                ) {
                    Text(stringResource(R.string.action_select_all), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = { viewModel.deselectAllMouzas() },
                    enabled = mouzas.isNotEmpty(),
                    modifier = Modifier.testTag("deselect_all_mouzas_button")
                ) {
                    Text(stringResource(R.string.action_deselect_all), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Mouza Selection List
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
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(mouzas, key = { it.id }) { mouza ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleMouzaSelection(mouza.id) }
                        .padding(vertical = 4.dp),
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
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "জে.এল. নম্বর: ${mouza.jlNumber} • ${if (mouza.catalogueSource == CatalogueSource.OFFICIAL) "সরকারি ক্যাটালগ" else "আমদানিকৃত"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Start Collection Action Button
        Button(
            onClick = { viewModel.startCollection() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_collection_button"),
            enabled = selectedMouzas.isNotEmpty()
        ) {
            Text(
                text = "${stringResource(R.string.action_start_collection)} (${selectedMouzas.size} টি মৌজা, প্রতি ব্যাচে ৭টি)",
                style = MaterialTheme.typography.labelLarge
            )
        }
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
