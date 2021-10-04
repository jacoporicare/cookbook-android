package cz.jakubricar.zradelnik.ui.settings

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import cz.jakubricar.zradelnik.getSync
import cz.jakubricar.zradelnik.getSyncFrequency
import cz.jakubricar.zradelnik.getSyncWifiOnly
import cz.jakubricar.zradelnik.getTheme
import cz.jakubricar.zradelnik.model.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val settings: Settings? = null,
    val loading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    app: Application
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
                    syncWifiOnly = prefManager.getSyncWifiOnly()
                ),
                loading = false
            )
        }
    }

    fun setTheme(theme: Settings.Theme) {
        prefManager.edit { putString(Settings.Keys.THEME, theme.name.lowercase()) }
        _uiState.update { it.copy(settings = it.settings?.copy(theme = theme)) }
    }

    fun setSync(sync: Boolean) {
        prefManager.edit { putBoolean(Settings.Keys.SYNC, sync) }
        _uiState.update { it.copy(settings = it.settings?.copy(sync = sync)) }
    }

    fun setSyncFrequency(syncFrequency: Settings.SyncFrequency) {
        prefManager.edit { putString(Settings.Keys.SYNC_FREQUENCY, syncFrequency.name.lowercase()) }
        _uiState.update { it.copy(settings = it.settings?.copy(syncFrequency = syncFrequency)) }
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        prefManager.edit { putBoolean(Settings.Keys.SYNC_WIFI_ONLY, syncWifiOnly) }
        _uiState.update { it.copy(settings = it.settings?.copy(syncWifiOnly = syncWifiOnly)) }
    }
}
