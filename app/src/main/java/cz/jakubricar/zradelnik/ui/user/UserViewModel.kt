package cz.jakubricar.zradelnik.ui.user

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.LoggedInUser
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
data class UserViewState(
    val loggedInUser: LoggedInUser? = null,
    val loading: Boolean = false
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val app: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UserViewState(loading = true))
    val state: StateFlow<UserViewState> = _state.asStateFlow()

    init {
        getLoggedInUser()
    }

    fun getLoggedInUser() {
        viewModelScope.launch {
            val authToken = userRepository.getAuthToken(app)

            if (authToken == null) {
                _state.update { it.copy(loggedInUser = null, loading = false) }
                return@launch
            }

            userRepository.getLoggedInUser(authToken)
                .onSuccess { user ->
                    _state.update { it.copy(loggedInUser = user, loading = false) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update { it.copy(loading = false) }
                }
        }
    }

    fun logout() {
        userRepository.logout(app)
        _state.update { it.copy(loggedInUser = null, loading = false) }
    }
}
