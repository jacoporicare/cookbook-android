package cz.jakubricar.zradelnik.ui.recipeedit

import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.ui.TextFieldState

class RecipeEditFormState(recipe: RecipeEdit?) {

    var title = TextFieldState(
        defaultValue = recipe?.title,
        validator = { it.isNotEmpty() },
        errorFor = { R.string.recipe_title_error }
    )
    var preparationTime = TextFieldState(
        defaultValue = recipe?.preparationTime?.toString(),
        validator = { it.isEmpty() || it.toIntOrNull() != null },
        errorFor = { R.string.preparation_time_error }
    )
    var servingCount = TextFieldState(
        defaultValue = recipe?.servingCount?.toString(),
        validator = { it.isEmpty() || it.toIntOrNull() != null },
        errorFor = { R.string.serving_count_error }
    )
    var sideDish = TextFieldState(defaultValue = recipe?.sideDish)
    var directions = TextFieldState(defaultValue = recipe?.directions)
}
