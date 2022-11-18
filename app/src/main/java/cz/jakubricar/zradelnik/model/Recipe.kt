package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val directions: String?,
    val ingredients: List<Ingredient>,
    val preparationTime: String?,
    val servingCount: String?,
    val sideDish: String?,
    val cookedHistory: List<Cooked>,
    val tags: List<String>,
) {

    companion object {

        const val instantPotTag = "Instant Pot"
    }

    val isForInstantPot: Boolean
        get() = tags.contains(instantPotTag)

    @Immutable
    data class Ingredient(
        val id: String,
        val name: String,
        val isGroup: Boolean,
        val amount: String?,
        val amountUnit: String?,
    )

    @Immutable
    data class Cooked(
        val date: String,
        val userDisplayName: String,
    )
}
