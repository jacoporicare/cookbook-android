package cz.jakubricar.zradelnik.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun ZradelnikApp() {
    ZradelnikTheme {
        ProvideWindowInsets {
            val systemUiController = rememberSystemUiController()
            val darkIcons = MaterialTheme.colors.isLight

            SideEffect {
                systemUiController.setSystemBarsColor(
                    Color.Transparent,
                    darkIcons = darkIcons
                )
            }

            ZradelnikNavGraph()
        }
    }
}
