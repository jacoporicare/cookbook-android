package cz.jakubricar.zradelnik.ui.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.ui.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class RecipeUiState(
    val loadingState: LoadingState = LoadingState.NONE,
    val recipe: RecipeDetail? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {
    companion object {
        const val RECIPE_SLUG_KEY = "slug"
    }

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    fun getRecipe(slug: String) {
        recipeRepository.getRecipe(slug)
            .onStart {
                _uiState.update { it.copy(loadingState = LoadingState.LOADING) }
            }
            .catch { error ->
                Timber.e(error)
                _uiState.update { it.copy(loadingState = LoadingState.ERROR) }
            }
            .onEach { recipe ->
                if (syncDataRepository.initialFetch(app)) {
                    return@onEach
                }

                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.DATA,
                        recipe = recipe
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
