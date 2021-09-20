package cz.jakubricar.zradelnik.network.api

import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.domain.RecipeDetail
import java.text.NumberFormat
import kotlin.math.floor

fun RecipeDetailQuery.Data.asDomain() =
    RecipeDetail(
        recipe?.asDomain(),
    )

fun RecipeDetailQuery.Recipe.asDomain() =
    RecipeDetail.Recipe(
        id = fragments.recipeFragment.id,
        slug = fragments.recipeFragment.slug,
        title = fragments.recipeFragment.title,
        imageUrl = fragments.recipeFragment.fullImageUrl,
        directions = fragments.recipeDetailFragment.directions,
        ingredients = fragments.recipeDetailFragment.ingredients?.map { ingredient ->
            RecipeDetail.Ingredient(
                name = ingredient.name,
                isGroup = ingredient.isGroup,
                amount = ingredient.amount?.let { NumberFormat.getInstance().format(it) },
                amountUnit = ingredient.amountUnit,
            )
        } ?: emptyList(),
        preparationTime = fragments.recipeDetailFragment.preparationTime?.let { formatTime(it) },
        servingCount = fragments.recipeDetailFragment.servingCount?.toString(),
        sideDish = fragments.recipeDetailFragment.sideDish,
    )

private fun formatTime(time: Int): String {
    val hours = floor(time.toDouble() / 60).toInt()
    val minutes = time % 60

    if (hours > 0 && minutes == 0) {
        return "$hours h"
    }

    if (hours > 0 && minutes > 0) {
        return "$hours h $minutes min"
    }

    return "$minutes min"
}
