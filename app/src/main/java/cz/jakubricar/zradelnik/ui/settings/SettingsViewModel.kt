package cz.jakubricar.zradelnik.ui.settings

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.getSync
import cz.jakubricar.zradelnik.getSyncFrequency
import cz.jakubricar.zradelnik.getSyncWifiOnly
import cz.jakubricar.zradelnik.getTheme
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.setSync
import cz.jakubricar.zradelnik.setSyncFrequency
import cz.jakubricar.zradelnik.setSyncWifiOnly
import cz.jakubricar.zradelnik.setTheme
import cz.jakubricar.zradelnik.work.setupPeriodicSyncDataWork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val settings: Settings? = null,
    val loading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(SettingsUiState(loading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val prefManager = PreferenceManager.getDefaultSharedPreferences(app)

    init {
        _uiState.update { uiState ->
            uiState.copy(
                settings = Settings(
                    theme = prefManager.getTheme(),
                    sync = prefManager.getSync(),
                    syncFrequency = prefManager.getSyncFrequency(),
                    syncWifiOnly = prefManager.getSyncWifiOnly(),
                    lastSyncDate = DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .format(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(app.getAppSharedPreferences().lastSyncDate),
                                ZoneId.systemDefault()
                            )
                        )
                ),
                loading = false
            )
        }
    }

    fun setTheme(theme: Settings.Theme) {
        _uiState.update { it.copy(settings = it.settings?.copy(theme = theme)) }
        prefManager.setTheme(theme)
    }

    fun setSync(sync: Boolean) {
        _uiState.update { it.copy(settings = it.settings?.copy(sync = sync)) }
        prefManager.setSync(sync)
        app.setupPeriodicSyncDataWork(newSync = sync)
    }

    fun setSyncFrequency(syncFrequency: Settings.SyncFrequency) {
        _uiState.update { it.copy(settings = it.settings?.copy(syncFrequency = syncFrequency)) }
        prefManager.setSyncFrequency(syncFrequency)
        app.setupPeriodicSyncDataWork(newSyncFrequency = syncFrequency)
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        _uiState.update { it.copy(settings = it.settings?.copy(syncWifiOnly = syncWifiOnly)) }
        prefManager.setSyncWifiOnly(syncWifiOnly)
        app.setupPeriodicSyncDataWork(newWifiOnly = syncWifiOnly)
    }
}
