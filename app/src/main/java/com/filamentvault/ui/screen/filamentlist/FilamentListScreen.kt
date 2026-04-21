package com.filamentvault.ui.screen.filamentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentvault.domain.model.Filament
import com.filamentvault.ui.common.ConfirmDeleteDialog
import com.filamentvault.ui.screen.filamentlist.components.EmptyState
import com.filamentvault.ui.screen.filamentlist.components.FilamentCard
import com.filamentvault.ui.screen.filamentlist.components.FilterSheet
import com.filamentvault.ui.screen.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilamentListScreen(
    onAddFilament: () -> Unit,
    onFilamentClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FilamentListViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showThumbnails by settingsViewModel.showThumbnails.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var filamentToDelete by remember { mutableStateOf<Filament?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FilamentVault") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (uiState.filterCriteria.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    // Settings moved to a bottom-nav tab
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddFilament,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Filament")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Active filter chips
            AnimatedVisibility(
                visible = uiState.filterCriteria.isActive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.filterCriteria.materialTypes.forEach { type ->
                        AssistChip(
                            onClick = {
                                viewModel.updateFilter(
                                    uiState.filterCriteria.copy(
                                        materialTypes = uiState.filterCriteria.materialTypes - type
                                    )
                                )
                            },
                            label = { Text(type) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.padding(0.dp)
                                )
                            }
                        )
                    }
                    uiState.filterCriteria.brands.forEach { brand ->
                        AssistChip(
                            onClick = {
                                viewModel.updateFilter(
                                    uiState.filterCriteria.copy(
                                        brands = uiState.filterCriteria.brands - brand
                                    )
                                )
                            },
                            label = { Text(brand) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        )
                    }
                    if (uiState.filterCriteria.activeFilterCount > 1) {
                        AssistChip(
                            onClick = { viewModel.clearFilters() },
                            label = { Text("Clear all") }
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filaments.isEmpty() -> {
                    EmptyState(hasActiveFilters = uiState.filterCriteria.isActive)
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.filaments,
                            key = { it.id }
                        ) { filament ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        filamentToDelete = filament
                                    }
                                    false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {},
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true
                            ) {
                                FilamentCard(
                                    filament = filament,
                                    showThumbnail = showThumbnails,
                                    onClick = { onFilamentClick(filament.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            currentCriteria = uiState.filterCriteria,
            availableBrands = uiState.availableBrands,
            onApply = { criteria ->
                viewModel.updateFilter(criteria)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    filamentToDelete?.let { filament ->
        ConfirmDeleteDialog(
            filamentName = "${filament.materialType} ${filament.colorName ?: filament.colorHex}",
            onConfirm = {
                viewModel.deleteFilament(filament)
                filamentToDelete = null
            },
            onDismiss = { filamentToDelete = null }
        )
    }
}
