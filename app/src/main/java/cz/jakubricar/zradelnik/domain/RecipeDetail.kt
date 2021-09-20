package cz.jakubricar.zradelnik.domain

data class RecipeDetail(
    val recipe: Recipe?
) {
    data class Recipe(
        val id: String,
        val slug: String,
        val title: String,
        val imageUrl: String?,
        val directions: String?,
        val ingredients: List<Ingredient>,
        val preparationTime: String?,
        val servingCount: String?,
        val sideDish: String?,
    )

    data class Ingredient(
        val name: String,
        val isGroup: Boolean,
        val amount: String?,
        val amountUnit: String?,
    )
}
