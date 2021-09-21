package cz.jakubricar.zradelnik.ui.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.jakubricar.zradelnik.lifecycle.LoadingState
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.ui.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val app: Application,
    private val recipeRepository: RecipeRepository,
    private val syncDataRepository: SyncDataRepository
) : AndroidViewModel(app) {
    companion object {
        const val RECIPE_SLUG_KEY = "slug"
    }

    private val _loadingState = MutableLiveData(LoadingState.NONE)
    val loadingState: LiveData<LoadingState> = _loadingState

    private val _recipe = MutableLiveData<RecipeDetail>()
    val recipe: LiveData<RecipeDetail> = _recipe

    private val _syncResult = MutableLiveData<SyncResult>()
    val syncResult: LiveData<SyncResult> = _syncResult

    fun getRecipe(slug: String) {
        recipeRepository.getRecipe(slug)
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
                _recipe.value = it
            }
            .launchIn(viewModelScope)
    }
}
