package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
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
    val recipe: RecipeDetail? = null,
    val loading: Boolean = false,
    val keepAwake: Boolean = false,
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

    fun toggleKeepAwake() {
        _state.update { it.copy(keepAwake = !it.keepAwake) }
    }
}
