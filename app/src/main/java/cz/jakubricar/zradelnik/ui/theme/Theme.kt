package cz.jakubricar.zradelnik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue400,
    primaryVariant = Blue600,
    onPrimary = Color.White,
    secondary = BlueGray500,
    secondaryVariant = BlueGray600,
    onSecondary = Color.Black,
    background = BlueGray900,
    onBackground = Color.White,
    error = Red200,
    surface = BlueGray800,
)

private val LightColorPalette = lightColors(
    primary = Blue400,
    primaryVariant = Blue600,
    onPrimary = Color.White,
    secondary = BlueGray500,
    secondaryVariant = BlueGray600,
    onSecondary = Color.Black,
    background = Gray50,
    onBackground = Color.Black,
    error = Red800,
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
