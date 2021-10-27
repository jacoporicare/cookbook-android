package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
)
