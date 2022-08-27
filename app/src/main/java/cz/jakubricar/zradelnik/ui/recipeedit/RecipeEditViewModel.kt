package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.type.IngredientInput
import cz.jakubricar.zradelnik.type.RecipeInput
import cz.jakubricar.zradelnik.ui.ErrorState
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
    val editedRecipe: RecipeEdit? = null,
    val loading: Boolean = false,
    val navigateToRecipeId: String? = null,
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    companion object {

        const val RECIPE_EDIT_ID_KEY = "id"
    }

    private val _state = MutableStateFlow(RecipeEditViewState(loading = true))
    val state: StateFlow<RecipeEditViewState> = _state.asStateFlow()

    val errorState = ErrorState()

    fun getRecipe(id: String) {
        viewModelScope.launch {
            recipeRepository.getRecipeEdit(id)
                .onSuccess { recipe ->
                    _state.update { it.copy(editedRecipe = recipe, loading = false) }
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

    fun save(authToken: String, formState: RecipeEditFormState, newImage: RecipeEdit.NewImage?) {
        if (!formState.isValid) {
            formState.enableShowErrors()
            errorState.addError(R.string.form_is_not_valid_snackbar_message)
            return
        }

        val nonEmptyIngredients = formState.ingredients.filter { it.name.value.isNotBlank() }

        val input = RecipeInput(
            title = formState.title.value.trim(),
            directions = Optional.presentIfNotNull(formState.directions.value.trim()
                .ifEmpty { null }),
            sideDish = Optional.presentIfNotNull(formState.sideDish.value.trim().ifEmpty { null }),
            preparationTime = Optional.presentIfNotNull(
                formState.preparationTime.value.trim().ifEmpty { null }?.toIntOrNull()
            ),
            servingCount = Optional.presentIfNotNull(
                formState.servingCount.value.trim().ifEmpty { null }?.toIntOrNull()
            ),
            ingredients = Optional.presentIfNotNull(
                if (nonEmptyIngredients.isNotEmpty()) {
                    nonEmptyIngredients
                        .map {
                            IngredientInput(
                                amount = Optional.presentIfNotNull(
                                    it.amount.value
                                        .trim()
                                        .ifEmpty { null }
                                        ?.replace(",", ".")
                                        ?.replace(" ", "")
                                        ?.toDoubleOrNull()
                                ),
                                amountUnit = Optional.presentIfNotNull(
                                    it.amountUnit.value.trim().ifEmpty { null }
                                ),
                                name = it.name.value.trim(),
                                isGroup = Optional.presentIfNotNull(it.isGroup.value),
                            )
                        }
                        .filter { it.name.isNotEmpty() }
                } else {
                    null
                }
            ),
            tags = Optional.presentIfNotNull(_state.value.editedRecipe?.tags),
        )

        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            recipeRepository.saveRecipe(authToken, _state.value.editedRecipe?.id, input, newImage)
                .onSuccess { recipe ->
                    _state.update { it.copy(navigateToRecipeId = recipe.id) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    errorState.addError(R.string.recipe_save_failed)
                    _state.update { it.copy(loading = false) }
                }
        }
    }
}
