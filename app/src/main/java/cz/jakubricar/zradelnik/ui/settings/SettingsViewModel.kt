package cz.jakubricar.zradelnik.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.repository.SettingsRepository
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
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(loading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { uiState ->
            uiState.copy(
                settings = settingsRepository.getSettings(),
                loading = false
            )
        }
    }

    fun setTheme(theme: Theme) {
        _uiState.update { it.copy(settings = it.settings?.copy(theme = theme)) }
        settingsRepository.setTheme(theme)
    }

    fun setSync(sync: Boolean) {
        _uiState.update { it.copy(settings = it.settings?.copy(sync = sync)) }
        settingsRepository.setSync(sync)
    }

    fun setSyncFrequency(syncFrequency: SyncFrequency) {
        _uiState.update { it.copy(settings = it.settings?.copy(syncFrequency = syncFrequency)) }
        settingsRepository.setSyncFrequency(syncFrequency)
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        _uiState.update { it.copy(settings = it.settings?.copy(syncWifiOnly = syncWifiOnly)) }
        settingsRepository.setSyncWifiOnly(syncWifiOnly)
    }
}
