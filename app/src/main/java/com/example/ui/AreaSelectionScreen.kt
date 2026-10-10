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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.AreaRepository
import com.example.model.SurveyType

@Composable
fun AreaSelectionScreen(viewModel: AppViewModel = viewModel()) {
    val selectedDivision by viewModel.selectedDivision.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedUpazila by viewModel.selectedUpazila.collectAsState()
    val selectedMouzas by viewModel.selectedMouzas.collectAsState()
    val selectedSurveyType by viewModel.selectedSurveyType.collectAsState()

    val divisions = AreaRepository.divisions
    val districts = selectedDivision?.let { AreaRepository.getDistricts(it.id) } ?: emptyList()
    val upazilas = selectedDistrict?.let { AreaRepository.getUpazilas(it.id) } ?: emptyList()
    val mouzas = selectedUpazila?.let { AreaRepository.getMouzas(it.id) } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AreaSelector(
            label = stringResource(R.string.label_division),
            value = selectedDivision?.bnName ?: "",
            options = divisions.map { it.bnName },
            onOptionSelected = { name ->
                viewModel.selectDivision(divisions.find { it.bnName == name })
            }
        )

        AreaSelector(
            label = stringResource(R.string.label_district),
            value = selectedDistrict?.bnName ?: "",
            options = districts.map { it.bnName },
            enabled = districts.isNotEmpty(),
            onOptionSelected = { name ->
                viewModel.selectDistrict(districts.find { it.bnName == name })
            }
        )

        AreaSelector(
            label = stringResource(R.string.label_upazila),
            value = selectedUpazila?.bnName ?: "",
            options = upazilas.map { it.bnName },
            enabled = upazilas.isNotEmpty(),
            onOptionSelected = { name ->
                viewModel.selectUpazila(upazilas.find { it.bnName == name })
            }
        )

        HorizontalDivider()

        Text(
            text = "জরিপের ধরণ",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SurveyType.values().forEach { type ->
                OutlinedCard(
                    onClick = { viewModel.setSurveyType(type) },
                    modifier = Modifier.weight(1f),
                    border = if (selectedSurveyType == type) {
                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    }
                ) {
                    Text(
                        type.name,
                        modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.label_mouza) + " (${selectedMouzas.size}/${mouzas.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                TextButton(onClick = { viewModel.selectAllMouzas(mouzas) }) {
                    Text(stringResource(R.string.action_select_all))
                }
                TextButton(onClick = { viewModel.deselectAllMouzas() }) {
                    Text(stringResource(R.string.action_deselect_all))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mouzas) { mouza ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleMouzaSelection(mouza.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedMouzas.contains(mouza.id),
                        onCheckedChange = { viewModel.toggleMouzaSelection(mouza.id) }
                    )
                    Column {
                        Text(mouza.bnName, style = MaterialTheme.typography.bodyLarge)
                        Text("জে.এল. নম্বর: ${mouza.jlNumber}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.startCollection() },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedMouzas.isNotEmpty()
        ) {
            Text(stringResource(R.string.action_start_collection))
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
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
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
