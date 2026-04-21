package com.filamentvault.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentvault.data.repository.FilamentRepository
import com.filamentvault.ui.theme.ThemeMode
import com.filamentvault.util.BackupUtil
import com.filamentvault.util.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val THEME_KEY = stringPreferencesKey("theme_mode")
private val SHOW_THUMBNAILS_KEY = booleanPreferencesKey("show_thumbnails")

sealed class SettingsEvent {
    data class ExportSuccess(val count: Int) : SettingsEvent()
    data class ImportSuccess(val filaments: Int, val overrides: Int) : SettingsEvent()
    data class Message(val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FilamentRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            when (prefs[THEME_KEY]) {
                "LIGHT" -> ThemeMode.LIGHT
                "SYSTEM" -> ThemeMode.SYSTEM
                else -> ThemeMode.DARK
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.DARK
        )

    val showThumbnails: StateFlow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SHOW_THUMBNAILS_KEY] ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.dataStore.edit { prefs -> prefs[THEME_KEY] = mode.name }
        }
    }

    fun setShowThumbnails(show: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs -> prefs[SHOW_THUMBNAILS_KEY] = show }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            BackupUtil.export(context, uri, repository)
                .onSuccess { count -> _events.emit(SettingsEvent.ExportSuccess(count)) }
                .onFailure { e -> _events.emit(SettingsEvent.Message("Export failed: ${e.message}")) }
        }
    }

    fun importBackup(uri: Uri, replaceExisting: Boolean) {
        viewModelScope.launch {
            when (val result = BackupUtil.import(context, uri, repository, replaceExisting)) {
                is RestoreResult.Success ->
                    _events.emit(SettingsEvent.ImportSuccess(result.filamentCount, result.overrideCount))
                is RestoreResult.Failure ->
                    _events.emit(SettingsEvent.Message("Import failed: ${result.message}"))
            }
        }
    }

    fun resetAllDefaults() {
        viewModelScope.launch {
            repository.clearAllOverrides()
            _events.emit(SettingsEvent.Message("All custom defaults cleared"))
        }
    }
}
