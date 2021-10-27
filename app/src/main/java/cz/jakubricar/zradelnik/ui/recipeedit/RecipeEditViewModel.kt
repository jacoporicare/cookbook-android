package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Input
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.repository.RecipeRepository
import cz.jakubricar.zradelnik.type.IngredientInput
import cz.jakubricar.zradelnik.type.RecipeInput
import cz.jakubricar.zradelnik.utils.ErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@Immutable
data class RecipeEditViewState(
    val editedRecipe: RecipeEdit? = null,
    val loading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
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

            _state.update {
                val errorMessages = it.errorMessages + ErrorMessage(
                    id = UUID.randomUUID().mostSignificantBits,
                    messageId = R.string.form_is_not_valid_snackbar_message
                )
                it.copy(errorMessages = errorMessages)
            }

            return
        }

        val nonEmptyIngredients = formState.ingredients.filter { it.name.value.isNotBlank() }

        val input = RecipeInput(
            title = formState.title.value.trim(),
            directions = Input.fromNullable(formState.directions.value.trim().ifEmpty { null }),
            sideDish = Input.fromNullable(formState.sideDish.value.trim().ifEmpty { null }),
            preparationTime = Input.fromNullable(
                formState.preparationTime.value.trim().ifEmpty { null }?.toIntOrNull()
            ),
            servingCount = Input.fromNullable(
                formState.servingCount.value.trim().ifEmpty { null }?.toIntOrNull()
            ),
            ingredients = Input.fromNullable(
                if (nonEmptyIngredients.isNotEmpty()) {
                    nonEmptyIngredients.map {
                        IngredientInput(
                            amount = Input.fromNullable(
                                it.amount.value
                                    .trim()
                                    .ifEmpty { null }
                                    ?.replace(",", ".")
                                    ?.replace(" ", "")
                                    ?.toDoubleOrNull()
                            ),
                            amountUnit = Input.fromNullable(
                                it.amountUnit.value.trim().ifEmpty { null }
                            ),
                            name = it.name.value.trim(),
                            isGroup = Input.fromNullable(it.isGroup.value),
                        )
                    }
                } else {
                    null
                }
            ),
            tags = Input.fromNullable(_state.value.editedRecipe?.tags),
        )

        _state.update { it.copy(loading = true) }

        viewModelScope.launch {
            recipeRepository.saveRecipe(authToken, _state.value.editedRecipe?.id, input, newImage)
                .onSuccess { recipe ->
                    _state.update { it.copy(navigateToRecipeId = recipe.id) }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        val errorMessages = it.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().mostSignificantBits,
                            messageId = R.string.recipe_save_failed
                        )
                        it.copy(errorMessages = errorMessages, loading = false)
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
}
