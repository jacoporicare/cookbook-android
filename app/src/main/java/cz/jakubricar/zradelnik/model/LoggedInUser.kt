package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class LoggedInUser(
    val id: String,
    val displayName: String,
)
