package com.filamentvault.ui.screen.defaultsdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentvault.ui.screen.filamentdetail.components.BrandAutocompleteField
import com.filamentvault.ui.screen.filamentdetail.components.FillTypeDropdown
import com.filamentvault.ui.screen.filamentdetail.components.MaterialTypeDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultsEditorScreen(
    baseId: Long?,
    overrideId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: DefaultsEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(baseId, overrideId) {
        viewModel.load(baseId, overrideId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { evt ->
            when (evt) {
                is EditorEvent.SaveSuccess -> onNavigateBack()
                is EditorEvent.ResetSuccess -> onNavigateBack()
                is EditorEvent.DuplicateKey -> snackbarHostState.showSnackbar(evt.message)
                is EditorEvent.Error -> snackbarHostState.showSnackbar(evt.message)
                is EditorEvent.Message -> snackbarHostState.showSnackbar(evt.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            state.isCustom -> "Edit Custom Default"
                            state.isModified -> "Edit Default (modified)"
                            state.isBuiltIn -> "Edit Default"
                            else -> "New Default"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Identity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            MaterialTypeDropdown(
                selectedType = state.materialType,
                onTypeSelected = { viewModel.update("materialType", it) }
            )

            FillTypeDropdown(
                selectedType = state.fillType,
                onTypeSelected = { viewModel.update("fillType", it) }
            )

            BrandAutocompleteField(
                value = state.brand,
                onValueChange = { viewModel.update("brand", it) },
                availableBrands = state.availableBrands
            )

            Text("Filament Diameter", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(1.75f, 2.85f).forEachIndexed { index, d ->
                    SegmentedButton(
                        selected = state.filamentDiameter == d,
                        onClick = { viewModel.updateDiameter(d) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) { Text("${d}mm") }
                }
            }

            HorizontalDivider()
            Text("Temperatures", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            IntRow("Nozzle Min (\u00B0C)", state.nozzleTempMin, "nozzleTempMin", viewModel)
            IntRow("Nozzle Max (\u00B0C)", state.nozzleTempMax, "nozzleTempMax", viewModel)
            IntRow("Bed Min (\u00B0C)", state.bedTempMin, "bedTempMin", viewModel)
            IntRow("Bed Max (\u00B0C)", state.bedTempMax, "bedTempMax", viewModel)
            IntRow("Chamber (\u00B0C)", state.chamberTemp, "chamberTemp", viewModel)

            HorizontalDivider()
            Text("Speeds & Cooling", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            IntRow("Print Speed Min (mm/s)", state.printSpeedMin, "printSpeedMin", viewModel)
            IntRow("Print Speed Max (mm/s)", state.printSpeedMax, "printSpeedMax", viewModel)
            DecimalRow("Max Volumetric Speed (mm\u00B3/s)", state.maxVolumetricSpeed, "maxVolumetricSpeed", viewModel)
            IntRow("Fan Speed Min (%)", state.fanSpeedMin, "fanSpeedMin", viewModel)
            IntRow("Fan Speed Max (%)", state.fanSpeedMax, "fanSpeedMax", viewModel)
            OutlinedTextField(
                value = state.fanSpeedNotes,
                onValueChange = { viewModel.update("fanSpeedNotes", it) },
                label = { Text("Fan Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()
            Text("Retraction, Flow, Layer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            DecimalRow("Retraction Distance (mm)", state.retractionDistance, "retractionDistance", viewModel)
            DecimalRow("Retraction Speed (mm/s)", state.retractionSpeed, "retractionSpeed", viewModel)
            DecimalRow("Flow Rate (%)", state.flowRate, "flowRate", viewModel)
            DecimalRow("Layer Height Min (mm)", state.layerHeightMin, "layerHeightMin", viewModel)
            DecimalRow("Layer Height Max (mm)", state.layerHeightMax, "layerHeightMax", viewModel)

            HorizontalDivider()
            Text("Physical & Drying", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            DecimalRow("Density (g/cm\u00B3)", state.density, "density", viewModel)
            IntRow("Drying Temp (\u00B0C)", state.dryingTemp, "dryingTemp", viewModel)
            IntRow("Drying Time (hrs)", state.dryingTime, "dryingTime", viewModel)
            OutlinedTextField(
                value = state.moistureSensitivity,
                onValueChange = { viewModel.update("moistureSensitivity", it) },
                label = { Text("Moisture Sensitivity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            // Action buttons ---------------------------------------------------
            if (state.isBuiltIn || state.isModified) {
                Button(
                    onClick = { viewModel.saveOverBase() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) { Text("Save (overrides built-in — original preserved)") }

                OutlinedButton(
                    onClick = { viewModel.saveAsNew() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) { Text("Save as new entry") }

                // Reset this one entry back to its built-in factory values
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.isModified) "Reset to original"
                        else "Reset fields to built-in"
                    )
                }
            } else if (state.isCustom) {
                Button(
                    onClick = { viewModel.saveCustom() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) { Text("Save changes") }

                OutlinedButton(
                    onClick = { viewModel.saveAsNew() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) { Text("Duplicate as new entry") }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete") }
            } else {
                // Brand new from scratch
                Button(
                    onClick = { viewModel.saveAsNew() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) { Text("Save new default") }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset to original?") },
            text = {
                Text(
                    if (state.isModified)
                        "Removes your saved edits to this entry and restores the built-in values."
                    else
                        "Discards any unsaved changes to this entry and reloads the built-in values."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetFieldsToBuiltIn()
                    showResetConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete custom default?") },
            text = { Text("This permanently removes this user-created default.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustom()
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun IntRow(label: String, value: String, field: String, vm: DefaultsEditorViewModel) {
    OutlinedTextField(
        value = value,
        onValueChange = { vm.update(field, it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun DecimalRow(label: String, value: String, field: String, vm: DefaultsEditorViewModel) {
    OutlinedTextField(
        value = value,
        onValueChange = { vm.update(field, it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
