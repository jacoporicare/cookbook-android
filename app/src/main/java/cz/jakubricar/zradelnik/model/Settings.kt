package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class Settings(
    val theme: Theme,
)

enum class Theme {
    LIGHT, DARK, DEFAULT
}

