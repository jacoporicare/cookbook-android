package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecipeDetail(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val directions: String?,
    val ingredients: List<Ingredient>,
    val preparationTime: String?,
    val servingCount: String?,
    val sideDish: String?,
) {

    @Immutable
    data class Ingredient(
        val id: String,
        val name: String,
        val isGroup: Boolean,
        val amount: String?,
        val amountUnit: String?,
    )
}
