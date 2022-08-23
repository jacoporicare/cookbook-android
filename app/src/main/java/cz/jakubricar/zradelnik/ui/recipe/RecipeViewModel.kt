package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.ui.ErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Immutable
data class RecipeViewState(
    val recipe: Recipe? = null,
    val loading: Boolean = false,
    val keepAwake: Boolean = false,
    val navigateToList: Boolean = false,
) {

    val failedLoading: Boolean
        get() = recipe == null && !loading
}

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    companion object {

        const val RECIPE_ID_KEY = "id"
    }

    private val _state = MutableStateFlow(RecipeViewState(loading = true))
    val state: StateFlow<RecipeViewState> = _state.asStateFlow()

    val errorState = ErrorState()

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
                    errorState.addError(R.string.recipe_delete_failed)
                    _state.update { it.copy(loading = false) }
                }
        }
    }

    fun toggleKeepAwake() {
        _state.update { it.copy(keepAwake = !it.keepAwake) }
    }
}
