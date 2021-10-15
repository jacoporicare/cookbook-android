package cz.jakubricar.zradelnik.ui.settings

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.LoggedInUser
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.repository.SettingsRepository
import cz.jakubricar.zradelnik.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Immutable
data class SettingsViewState(
    val settings: Settings? = null,
    val loggedInUser: LoggedInUser? = null,
    val loadingSettings: Boolean = false,
    val loadingLoggedInUser: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(
        SettingsViewState(
            loadingSettings = true,
            loadingLoggedInUser = true
        )
    )
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    init {
        _state.update { uiState ->
            uiState.copy(
                settings = settingsRepository.getSettings(),
                loadingSettings = false
            )
        }

        getLoggedInUser()
    }

    fun setTheme(theme: Theme) {
        _state.update { it.copy(settings = it.settings?.copy(theme = theme)) }
        settingsRepository.setTheme(theme)
    }

    fun setSync(sync: Boolean) {
        _state.update { it.copy(settings = it.settings?.copy(sync = sync)) }
        settingsRepository.setSync(sync)
    }

    fun setSyncFrequency(syncFrequency: SyncFrequency) {
        _state.update { it.copy(settings = it.settings?.copy(syncFrequency = syncFrequency)) }
        settingsRepository.setSyncFrequency(syncFrequency)
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        _state.update { it.copy(settings = it.settings?.copy(syncWifiOnly = syncWifiOnly)) }
        settingsRepository.setSyncWifiOnly(syncWifiOnly)
    }

    fun getLoggedInUser() {
        viewModelScope.launch {
            val authToken = userRepository.getAuthToken(app)

            if (authToken == null) {
                _state.update { it.copy(loggedInUser = null) }
                return@launch
            }

            userRepository.getLoggedInUser(authToken)
                .onSuccess { user ->
                    _state.update { it.copy(loggedInUser = user, loadingLoggedInUser = false) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update { it.copy(loadingLoggedInUser = false) }
                }
        }
    }

    fun logout() {
        userRepository.logout(app)
        _state.update { it.copy(loggedInUser = null, loadingLoggedInUser = false) }
    }
}
