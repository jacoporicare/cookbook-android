package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.ui.TextFieldState
import java.text.NumberFormat

class RecipeEditFormState(recipe: RecipeEdit?) {

    class IngredientFormState(ingredient: RecipeEdit.Ingredient? = null) {

        val name = TextFieldState(defaultValue = ingredient?.name ?: "")
        val isGroup = mutableStateOf(ingredient?.isGroup ?: false)
        val amount = TextFieldState(
            defaultValue = ingredient?.amount?.let { amount ->
                NumberFormat.getInstance().format(amount)
            } ?: "",
            validator = {
                it.isEmpty() || it.replace(",", ".")
                    .replace(" ", "")
                    .toDoubleOrNull() != null
            },
            errorFor = { R.string.ingredient_amount_error },
        )
        val amountUnit = TextFieldState(ingredient?.amountUnit ?: "")

        private val validatedFields = listOf(amount)

        val isValid: Boolean
            get() = validatedFields.all { it.isValid }

        fun enableShowErrors() {
            validatedFields.forEach {
                it.isFocusedDirty = true
                it.enableShowErrors()
            }
        }
    }

    val title = TextFieldState(
        defaultValue = recipe?.title,
        validator = { it.isNotEmpty() },
        errorFor = { R.string.recipe_title_error }
    )
    val preparationTime = TextFieldState(
        defaultValue = recipe?.preparationTime?.toString(),
        validator = { it.isEmpty() || it.toIntOrNull() != null },
        errorFor = { R.string.preparation_time_error }
    )
    val servingCount = TextFieldState(
        defaultValue = recipe?.servingCount?.toString(),
        validator = { it.isEmpty() || it.toIntOrNull() != null },
        errorFor = { R.string.serving_count_error }
    )
    val sideDish = TextFieldState(defaultValue = recipe?.sideDish)
    val directions = TextFieldState(defaultValue = recipe?.directions)

    var ingredients by mutableStateOf(
        recipe?.ingredients?.map { IngredientFormState(it) }
            ?: listOf()
    )

    private val validatedFields = listOf(title, preparationTime, servingCount)

    val isValid: Boolean
        get() = validatedFields.all { it.isValid } && ingredients.all { it.isValid }

    init {
        ingredients = ingredients + IngredientFormState()
    }

    fun enableShowErrors() {
        validatedFields.forEach {
            it.isFocusedDirty = true
            it.enableShowErrors()
        }

        ingredients.forEach { it.enableShowErrors() }
    }
}
