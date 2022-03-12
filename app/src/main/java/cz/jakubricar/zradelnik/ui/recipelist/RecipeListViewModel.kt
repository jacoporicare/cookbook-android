package cz.jakubricar.zradelnik.ui.recipelist

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.platform.unaccent
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.ui.ErrorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import javax.inject.Inject

@Immutable
data class RecipeListViewState(
    val recipes: List<Recipe> = emptyList(),
    val loading: Boolean = false,
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val initialSync: Boolean = false,
) {

    val initialLoad: Boolean
        get() = recipes.isEmpty() && loading
}

private val collator = Collator.getInstance()
private val recipeComparator =
    Comparator<Recipe> { o1, o2 -> collator.compare(o1.title, o2.title) }

fun filterAndSortRecipes(recipes: List<Recipe>, searchQuery: String): List<Recipe> {
    val filtered = if (searchQuery.isBlank()) {
        recipes
    } else {
        recipes.filter {
            it.title
                .unaccent()
                .contains(searchQuery.unaccent(), true)
        }
    }

    return filtered.sortedWith(recipeComparator)
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListViewState(loading = true))
    val state: StateFlow<RecipeListViewState> = _state.asStateFlow()

    val errorState = ErrorState()

    init {
        observeRecipes()
    }

    private fun observeRecipes() {
        recipeRepository.observeRecipes()
            .onEach { recipes ->
                if (syncDataRepository.initialSync()) {
                    if (!_state.getAndUpdate { it.copy(initialSync = true) }.initialSync) {
                        refreshRecipes()
                    }
                    return@onEach
                }

                _state.update { it.copy(recipes = recipes, loading = false) }
            }
            .catch { error ->
                Timber.e(error)
                errorState.addError(R.string.connection_error) { refreshRecipes() }
                _state.update { it.copy(loading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun refreshRecipes() {
        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            syncDataRepository.fetchAllRecipeDetails()
                .onSuccess {
                    _state.update { it.copy(loading = false, initialSync = false) }
                }
                .onFailure {
                    errorState.addError(R.string.connection_error) { refreshRecipes() }
                    _state.update { it.copy(loading = false, initialSync = false) }
                }
        }
    }

    fun showSearch() {
        _state.update { it.copy(searchVisible = true) }
    }

    fun hideSearch() {
        _state.update { it.copy(searchVisible = false, searchQuery = "") }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
}
