package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Immutable
data class RecipeEditViewState(
    val recipe: RecipeDetail? = null,
    val loading: Boolean = false
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    companion object {

        const val RECIPE_EDIT_SLUG_KEY = "slug"
    }

    private val _state = MutableStateFlow(RecipeEditViewState(loading = true))
    val state: StateFlow<RecipeEditViewState> = _state.asStateFlow()

    fun getRecipe(slug: String) {
        viewModelScope.launch {
            recipeRepository.getRecipe(slug, true)
                .onSuccess { recipe ->
                    _state.update { it.copy(recipe = recipe, loading = false) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update { it.copy(loading = false) }
                }
        }
    }

    fun setLoading(loading: Boolean) {
        _state.update { it.copy(loading = loading) }
    }
}
