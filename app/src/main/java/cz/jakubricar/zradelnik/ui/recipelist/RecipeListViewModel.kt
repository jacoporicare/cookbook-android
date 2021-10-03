package cz.jakubricar.zradelnik.ui.recipelist

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import java.util.UUID
import javax.inject.Inject

data class RecipeListUiState(
    val recipes: List<Recipe> = emptyList(),
    val loading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val searchQuery: String? = null,
    val searchVisible: Boolean = false
) {

    val initialLoad: Boolean
        get() = recipes.isEmpty() && loading

    @Composable
    fun rememberFilteredRecipes(): List<Recipe> {
        return remember(recipes, searchQuery) {
            if (searchQuery.isNullOrBlank()) {
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
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(RecipeListUiState(loading = true))
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private val collator = Collator.getInstance()
    private val recipeComparator =
        Comparator<Recipe> { o1, o2 -> collator.compare(o1.title, o2.title) }

    init {
        getRecipes()
    }

    private fun getRecipes() {
        recipeRepository.getRecipes()
            .catch { error ->
                Timber.e(error)
                _uiState.update {
                    val errorMessages = it.errorMessages + ErrorMessage(
                        id = UUID.randomUUID().mostSignificantBits,
                        messageId = R.string.connection_error
                    )
                    it.copy(errorMessages = errorMessages, loading = false)
                }
            }
            .onEach { recipes ->
                if (syncDataRepository.initialSync(app)) {
                    refreshRecipes()
                    return@onEach
                }

                _uiState.update {
                    it.copy(recipes = recipes.sortedWith(recipeComparator), loading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshRecipes() {
        _uiState.update { it.copy(loading = true) }

        viewModelScope.launch {
            val result = syncDataRepository.fetchAllRecipeDetails(app)

            _uiState.update { uiState ->
                result.fold(
                    onSuccess = { uiState.copy(loading = false) },
                    onFailure = {
                        val errorMessages = uiState.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().mostSignificantBits,
                            messageId = R.string.connection_error
                        )
                        uiState.copy(errorMessages = errorMessages, loading = false)
                    }
                )
            }
        }
    }

    fun errorShown(errorId: Long) {
        _uiState.update { currentUiState ->
            val errorMessages = currentUiState.errorMessages.filterNot { it.id == errorId }
            currentUiState.copy(errorMessages = errorMessages)
        }
    }

    fun showSearch() {
        _uiState.update { it.copy(searchVisible = true) }
    }

    fun hideSearch() {
        _uiState.update { it.copy(searchVisible = false, searchQuery = null) }
    }

    fun setSearchQuery(query: String?) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
