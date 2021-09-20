package cz.jakubricar.zradelnik.domain

data class RecipeList(
    val recipes: List<Recipe>
) {
    data class Recipe(
        val id: String,
        val slug: String,
        val title: String,
        val imageUrl: String?,
    )
}
