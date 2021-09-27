package cz.jakubricar.zradelnik.ui.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.platform.unaccent
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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import javax.inject.Inject

data class RecipeListUiState(
    val loadingState: LoadingState = LoadingState.NONE,
    val recipes: List<Recipe> = emptyList()
) {
    /**
     * True if this represents a first load
     */
    val initialLoad: Boolean
        get() = recipes.isEmpty() && loadingState == LoadingState.LOADING
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private val collator = Collator.getInstance()
    private val recipeComparator =
        Comparator<Recipe> { o1, o2 -> collator.compare(o1.title, o2.title) }

    init {
        getRecipes()
    }

    private fun getRecipes() {
        recipeRepository.getRecipes()
            .onStart {
                _uiState.update { it.copy(loadingState = LoadingState.LOADING) }
            }
            .catch { error ->
                Timber.e(error)
                _uiState.update { it.copy(loadingState = LoadingState.ERROR) }
            }
            .onEach { recipes ->
                if (syncDataRepository.initialFetch(app)) {
                    return@onEach
                }

                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.DATA,
                        recipes = recipes.sortedWith(recipeComparator)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshRecipes() {
        _uiState.update { it.copy(loadingState = LoadingState.LOADING) }

        viewModelScope.launch {
            val result = syncDataRepository.fetchAllRecipeDetails(app)

            _uiState.update {
                it.copy(loadingState = result.fold({ LoadingState.DATA }, { LoadingState.ERROR }))
            }
        }
    }
}

fun filterRecipes(recipes: List<Recipe>, query: String?): List<Recipe> {
    return if (query.isNullOrBlank()) {
        recipes
    } else {
        recipes.filter { it.title.unaccent().contains(query.unaccent(), true) }
    }
}
