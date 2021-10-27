package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.utils.ErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@Immutable
data class RecipeViewState(
    val recipe: RecipeDetail? = null,
    val loading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val keepAwake: Boolean = false,
    val navigateToList: Boolean = false,
) {

    val failedLoading: Boolean
        get() = recipe == null && !loading
}

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository,
) : ViewModel() {

    companion object {

        const val RECIPE_ID_KEY = "id"
    }

    private val _state = MutableStateFlow(RecipeViewState(loading = true))
    val state: StateFlow<RecipeViewState> = _state.asStateFlow()

    val initialSync
        get() = syncDataRepository.initialSync()

    fun getRecipe(id: String) {
        viewModelScope.launch {
            recipeRepository.getRecipeDetail(id)
                .onSuccess { recipe ->
                    _state.update { it.copy(recipe = recipe, loading = false) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update { it.copy(loading = false) }
                }
        }
    }

    fun deleteRecipe(authToken: String, id: String) {
        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            recipeRepository.deleteRecipe(authToken, id)
                .onSuccess {
                    _state.update { it.copy(navigateToList = true) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        val errorMessages = it.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().mostSignificantBits,
                            messageId = R.string.recipe_delete_failed
                        )
                        it.copy(errorMessages = errorMessages, loading = false)
                    }
                }
        }
    }

    fun toggleKeepAwake() {
        _state.update { it.copy(keepAwake = !it.keepAwake) }
    }

    fun errorShown(errorId: Long) {
        _state.update { viewState ->
            val errorMessages = viewState.errorMessages.filterNot { it.id == errorId }
            viewState.copy(errorMessages = errorMessages)
        }
    }
}
