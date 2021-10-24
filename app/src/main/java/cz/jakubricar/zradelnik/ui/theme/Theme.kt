package cz.jakubricar.zradelnik.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cz.jakubricar.zradelnik.model.Theme

private val LightColorPalette = lightColors(
    primary = Blue700,
    primaryVariant = Blue800,
    secondary = GoogleBlueLight,
    secondaryVariant = GoogleBlueDark,
    background = Gray50,
    onPrimary = Color.White,
    onSecondary = GoogleOnBlueLight
)

private val DarkColorPalette = darkColors(
    primary = Blue600,
    primaryVariant = Blue700,
    secondary = Cyan700,
    secondaryVariant = Cyan700,
    background = BlueGray1000,
    surface = BlueGray1000,
    onPrimary = Color.Black,
    onSecondary = Color.White
)

@Composable
fun ZradelnikTheme(
    theme: Theme = Theme.DEFAULT,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.DEFAULT -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
