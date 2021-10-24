package cz.jakubricar.zradelnik.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginViewState(
    val loginResult: Result<String>? = null,
    val loading: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginViewState())
    val state: StateFlow<LoginViewState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            val result = userRepository.login(username, password)

            _state.update { it.copy(loginResult = result, loading = false) }
        }
    }

    fun resetLoginResult() {
        _state.update { it.copy(loginResult = null, loading = false) }
    }
}
