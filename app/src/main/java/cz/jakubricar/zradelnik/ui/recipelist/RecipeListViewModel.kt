package cz.jakubricar.zradelnik.ui.recipelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.platform.unaccent
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.utils.ErrorMessage
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
import java.util.UUID
import javax.inject.Inject

@Immutable
data class RecipeListViewState(
    val recipes: List<Recipe> = emptyList(),
    val loading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val initialSync: Boolean = false,
) {

    val initialLoad: Boolean
        get() = recipes.isEmpty() && loading
}

@Composable
fun rememberFilteredRecipes(recipes: List<Recipe>, searchQuery: String): List<Recipe> {
    return remember(recipes, searchQuery) {
        if (searchQuery.isBlank()) {
            recipes
        } else {
            recipes.filter {
                it.title
                    .unaccent()
                    .contains(searchQuery.unaccent(), true)
            }
        }
    }
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListViewState(loading = true))
    val state: StateFlow<RecipeListViewState> = _state.asStateFlow()

    private val collator = Collator.getInstance()
    private val recipeComparator =
        Comparator<Recipe> { o1, o2 -> collator.compare(o1.title, o2.title) }

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

                _state.update {
                    it.copy(recipes = recipes.sortedWith(recipeComparator), loading = false)
                }
            }
            .catch { error ->
                Timber.e(error)
                _state.update {
                    val errorMessages = it.errorMessages + ErrorMessage(
                        id = UUID.randomUUID().mostSignificantBits,
                        messageId = R.string.connection_error,
                        tryAgain = true,
                    )
                    it.copy(errorMessages = errorMessages, loading = false)
                }
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
                    _state.update {
                        val errorMessages = it.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().mostSignificantBits,
                            messageId = R.string.connection_error,
                            tryAgain = true,
                        )
                        it.copy(
                            errorMessages = errorMessages,
                            loading = false,
                            initialSync = false
                        )
                    }
                }
        }
    }

    fun errorShown(errorId: Long) {
        _state.update { viewState ->
            val errorMessages = viewState.errorMessages.filterNot { it.id == errorId }
            viewState.copy(errorMessages = errorMessages)
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
