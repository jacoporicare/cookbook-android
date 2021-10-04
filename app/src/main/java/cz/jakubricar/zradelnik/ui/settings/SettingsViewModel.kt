package cz.jakubricar.zradelnik.ui.settings

import android.app.Application
import androidx.compose.runtime.Immutable
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

    init {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(app)

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
}
