package com.filamentvault.ui.screen.filamentdetail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentvault.ui.common.ColorSwatchCircle
import com.filamentvault.ui.screen.filamentdetail.components.BrandAutocompleteField
import com.filamentvault.ui.screen.filamentdetail.components.ColorPickerDialog
import com.filamentvault.ui.screen.filamentdetail.components.FillTypeDropdown
import com.filamentvault.ui.screen.filamentdetail.components.ImageColorPickerDialog
import com.filamentvault.ui.screen.filamentdetail.components.ImageSection
import com.filamentvault.ui.screen.filamentdetail.components.MaterialTypeDropdown
import com.filamentvault.ui.screen.filamentdetail.components.NotesField
import com.filamentvault.ui.screen.filamentdetail.components.PrintSettingsSection
import com.filamentvault.ui.screen.filamentdetail.components.SpoolCropperDialog
import com.filamentvault.ui.screen.filamentdetail.components.TemperatureFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentDetailScreen(
    filamentId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: FilamentDetailViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showColorPicker by remember { mutableStateOf(false) }
    var showImageColorPicker by remember { mutableStateOf(false) }
    var imagePathForColorPick by remember { mutableStateOf<String?>(null) }
    var duplicateInfo by remember { mutableStateOf<DuplicateInfo?>(null) }
    var imagePathForCrop by remember { mutableStateOf<String?>(null) }
    // When the user imports a new photo, auto-open the cropper on the original.
    // We track the last-known original so we only trigger once per import.
    var lastKnownOriginal by remember { mutableStateOf(formState.originalImageUri) }
    LaunchedEffect(formState.originalImageUri) {
        val current = formState.originalImageUri
        val alreadyCropped = formState.imageUri != null &&
            formState.imageUri != formState.originalImageUri
        if (current != null && current != lastKnownOriginal && !alreadyCropped) {
            imagePathForCrop = current
        }
        lastKnownOriginal = current
    }

    LaunchedEffect(filamentId) {
        viewModel.loadFilament(filamentId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.SaveSuccess -> onNavigateBack()
                is DetailEvent.DefaultsApplied -> {
                    snackbarHostState.showSnackbar(
                        "Default settings applied for ${event.materialType}"
                    )
                }
                is DetailEvent.DuplicateFound -> {
                    duplicateInfo = event.info
                }
                is DetailEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (formState.isEditing) "Edit Filament" else "Add Filament")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = formState.isValid && !formState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Identity
            Text(
                text = "Identity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            MaterialTypeDropdown(
                selectedType = formState.materialType,
                onTypeSelected = { type ->
                    viewModel.updateField("materialType", type)
                }
            )

            FillTypeDropdown(
                selectedType = formState.fillType,
                onTypeSelected = { type ->
                    viewModel.updateField("fillType", type)
                }
            )

            // Brand — type-ahead with 500ms debounce defaults lookup
            BrandAutocompleteField(
                value = formState.brand,
                onValueChange = { value ->
                    viewModel.updateField("brand", value)
                },
                availableBrands = formState.availableBrands,
                modifier = Modifier.fillMaxWidth()
            )

            // Color
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorSwatchCircle(
                    colorHex = formState.colorHex,
                    size = 48.dp
                )

                OutlinedTextField(
                    value = formState.colorHex,
                    onValueChange = { viewModel.updateColor(it) },
                    label = { Text("Color Hex *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = { showColorPicker = true }
                ) {
                    Text("Pick")
                }
            }

            OutlinedTextField(
                value = formState.colorName,
                onValueChange = { viewModel.updateField("colorName", it) },
                label = { Text("Color Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )

                FilledIconButton(
                    onClick = { viewModel.updateQuantity(-1) },
                    enabled = formState.quantity > 1,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease quantity"
                    )
                }

                Text(
                    text = "${formState.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(48.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp)
                )

                FilledIconButton(
                    onClick = { viewModel.updateQuantity(1) },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase quantity"
                    )
                }
            }

            // Filament Diameter
            Text("Filament Diameter", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(1.75f, 2.85f).forEachIndexed { index, diameter ->
                    SegmentedButton(
                        selected = formState.filamentDiameter == diameter,
                        onClick = { viewModel.updateDiameter(diameter) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                    ) {
                        Text("${diameter}mm")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 2: Temperatures
            TemperatureFields(
                nozzleTempMin = formState.nozzleTempMin,
                nozzleTempMax = formState.nozzleTempMax,
                bedTempMin = formState.bedTempMin,
                bedTempMax = formState.bedTempMax,
                chamberTemp = formState.chamberTemp,
                onFieldChange = { field, value -> viewModel.updateField(field, value) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 3: Print Settings
            PrintSettingsSection(
                printSpeedMin = formState.printSpeedMin,
                printSpeedMax = formState.printSpeedMax,
                maxVolumetricSpeed = formState.maxVolumetricSpeed,
                fanSpeedMin = formState.fanSpeedMin,
                fanSpeedMax = formState.fanSpeedMax,
                fanSpeedNotes = formState.fanSpeedNotes,
                retractionDistance = formState.retractionDistance,
                retractionSpeed = formState.retractionSpeed,
                flowRate = formState.flowRate,
                layerHeightMin = formState.layerHeightMin,
                layerHeightMax = formState.layerHeightMax,
                onFieldChange = { field, value -> viewModel.updateField(field, value) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 4: Physical Properties & Drying
            Text(
                text = "Physical Properties & Drying",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = formState.density,
                onValueChange = { viewModel.updateField("density", it) },
                label = { Text("Density (g/cm\u00B3)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formState.dryingTemp,
                    onValueChange = { viewModel.updateField("dryingTemp", it) },
                    label = { Text("Drying Temp (\u00B0C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.dryingTime,
                    onValueChange = { viewModel.updateField("dryingTime", it) },
                    label = { Text("Drying Time (hrs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = formState.moistureSensitivity,
                onValueChange = { viewModel.updateField("moistureSensitivity", it) },
                label = { Text("Moisture Sensitivity") },
                placeholder = { Text("Low / Moderate / High / Very High") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 5: Notes
            NotesField(
                notes = formState.notes,
                onNotesChange = { viewModel.updateField("notes", it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Section 6: Image
            ImageSection(
                imageUri = formState.imageUri,
                onImageSelected = { uri -> viewModel.setImageUri(uri) },
                onImageRemoved = { viewModel.removeImage() },
                onExtractColor = { path ->
                    imagePathForColorPick = path
                    showImageColorPicker = true
                },
                onRecrop = { _ ->
                    // Recrop always uses the preserved original, not the cropped thumbnail
                    val original = formState.originalImageUri ?: formState.imageUri
                    if (original != null) imagePathForCrop = original
                }
            )

            // Save button at bottom
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.isValid && !formState.isSaving
            ) {
                Text(
                    if (formState.isEditing) "Update Filament" else "Save Filament",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = formState.colorHex,
            onColorSelected = { hex ->
                viewModel.updateColor(hex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    imagePathForCrop?.let { path ->
        SpoolCropperDialog(
            sourcePath = path,
            onCropped = { croppedPath ->
                viewModel.setImagePath(croppedPath)
                imagePathForCrop = null
            },
            onDismiss = { imagePathForCrop = null }
        )
    }

    if (showImageColorPicker && imagePathForColorPick != null) {
        ImageColorPickerDialog(
            imagePath = imagePathForColorPick!!,
            onColorSelected = { hex ->
                viewModel.updateColor(hex)
                showImageColorPicker = false
                imagePathForColorPick = null
            },
            onDismiss = {
                showImageColorPicker = false
                imagePathForColorPick = null
            }
        )
    }

    duplicateInfo?.let { info ->
        val description = buildString {
            append(info.materialType)
            info.brand?.let { append(" by $it") }
            info.colorName?.let { append(" ($it)") }
        }
        val newQty = info.currentQuantity + 1

        AlertDialog(
            onDismissRequest = { duplicateInfo = null },
            title = { Text("Duplicate Filament") },
            text = {
                Text(
                    "You already have ${info.currentQuantity} spool(s) of $description. " +
                        "Would you like to increase the count to $newQty, or cancel this entry?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.incrementExisting(info.existingId)
                    duplicateInfo = null
                }) {
                    Text("Add Spool ($newQty)")
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicateInfo = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
