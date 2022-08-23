package cz.jakubricar.zradelnik.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class SettingsViewState(
    val settings: Settings? = null,
    val loading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsViewState(loading = true))
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    init {
        _state.update { uiState ->
            uiState.copy(
                settings = settingsRepository.getSettings(),
                loading = false
            )
        }
    }

    fun setTheme(theme: Theme) {
        _state.update { it.copy(settings = it.settings?.copy(theme = theme)) }
        settingsRepository.setTheme(theme)
    }
}
