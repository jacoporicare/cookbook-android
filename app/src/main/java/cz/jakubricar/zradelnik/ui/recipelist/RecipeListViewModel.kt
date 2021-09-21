package cz.jakubricar.zradelnik.ui.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.lifecycle.LoadingState
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.platform.unaccent
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.ui.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {
    private var allRecipes = emptyList<Recipe>()

    private val _loadingState = MutableLiveData(LoadingState.NONE)
    val loadingState: LiveData<LoadingState> = _loadingState

    private val _recipes = MutableLiveData<List<Recipe>>(emptyList())
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _syncResult = MutableLiveData<SyncResult>()
    val syncResult: LiveData<SyncResult> = _syncResult

    private val _searchQuery = MutableLiveData<String?>()
    val searchQuery: LiveData<String?> = _searchQuery

    private val collator = Collator.getInstance()
    private val recipeComparator =
        Comparator<Recipe> { o1, o2 -> collator.compare(o1.title, o2.title) }

    init {
        getRecipes()
    }

    private fun getRecipes() {
        recipeRepository.getRecipes()
            .onStart {
                _loadingState.value = LoadingState.LOADING
            }
            .catch {
                Timber.e(it)
                _loadingState.value = LoadingState.ERROR
            }
            .onEach {
                try {
                    if (syncDataRepository.initialFetch(app)) {
                        return@onEach
                    }
                } catch (e: Exception) {
                    _loadingState.value = LoadingState.ERROR
                    return@onEach
                }

                _loadingState.value = LoadingState.DATA
                allRecipes = it.sortedWith(recipeComparator)
//                    (if (BuildConfig.LIMITED_RECIPE_COUNT) it.recipes.take(10) else it.recipes)
//                        .sortedWith(recipeComparator)
                filterRecipes(searchQuery.value)
            }
            .launchIn(viewModelScope)
    }

    fun retry() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING

            try {
                syncDataRepository.initialFetch(app)
            } catch (e: Exception) {
                _loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.REFRESHING

            val result = syncDataRepository.fetchAllRecipeDetails(app)

            _loadingState.value = LoadingState.DATA
            _syncResult.value = result.fold(
                { SyncResult(success = true) },
                { SyncResult(error = true) }
            )
        }
    }

    fun filterRecipes(query: String?) {
        _searchQuery.value = query

        _recipes.value = if (query == null) {
            allRecipes
        } else {
            allRecipes.filter { it.title.unaccent().contains(query.unaccent(), true) }
        }
    }
}
