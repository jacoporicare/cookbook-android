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

data class LoginUiState(
    val loginResult: Result<String>? = null,
    val loading: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        _uiState.update { it.copy(loading = true) }

        viewModelScope.launch {
            val result = userRepository.login(username, password)

            _uiState.update { it.copy(loginResult = result, loading = false) }
        }
    }
}
