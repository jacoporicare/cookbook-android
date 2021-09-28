package cz.jakubricar.zradelnik.ui.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class RecipeUiState(
    val recipe: RecipeDetail? = null,
    val loading: Boolean = false
) {

    val failedLoading: Boolean
        get() = recipe == null && !loading
}

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {

    companion object {

        const val RECIPE_SLUG_KEY = "slug"
    }

    private val _uiState = MutableStateFlow(RecipeUiState(loading = true))
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun getRecipe(slug: String) {
        recipeRepository.getRecipe(slug)
            .catch { error ->
                Timber.e(error)
                _uiState.update { it.copy(loading = false) }
            }
            .onEach { recipe ->
                _uiState.update { it.copy(recipe = recipe, loading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun initialSync() = syncDataRepository.initialSync(app)
}
