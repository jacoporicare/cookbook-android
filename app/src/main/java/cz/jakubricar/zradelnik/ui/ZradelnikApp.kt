package cz.jakubricar.zradelnik.ui

import android.content.SharedPreferences
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import cz.jakubricar.zradelnik.SettingsSharedPreferences
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun ZradelnikApp(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSettingsSharedPreferences() }
    var theme by remember { mutableStateOf(prefs.theme) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SettingsSharedPreferences.Keys.THEME) {
                theme = prefs.theme
            }
        }

        prefs.registerOnChangeListener(listener)

        onDispose {
            prefs.unregisterOnChangeListener(listener)
        }
    }

    ZradelnikTheme(theme = theme) {
        ProvideWindowInsets {
            val systemUiController = rememberSystemUiController()
            val darkIcons = MaterialTheme.colors.isLight

            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = darkIcons
                )
            }

            content()
        }
    }
}
