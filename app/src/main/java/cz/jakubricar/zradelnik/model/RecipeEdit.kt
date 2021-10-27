package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecipeEdit(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val directions: String?,
    val ingredients: List<Ingredient>,
    val preparationTime: Int?,
    val servingCount: Int?,
    val sideDish: String?,
    val tags: List<String>?,
) {

    @Immutable
    data class Ingredient(
        val id: String,
        val name: String,
        val isGroup: Boolean,
        val amount: Double?,
        val amountUnit: String?,
    )
}
