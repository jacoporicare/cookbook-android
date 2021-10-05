package cz.jakubricar.zradelnik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Indigo500,
    primaryVariant = Indigo900,
    secondary = Cyan500,
    secondaryVariant = Cyan900,
    background = Gray50
)

private val DarkColorPalette = darkColors(
    primary = Indigo300,
    primaryVariant = Indigo500,
    secondary = Cyan500,
    background = BlueGray1000,
    surface = BlueGray1000
)

@Composable
fun ZradelnikTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
