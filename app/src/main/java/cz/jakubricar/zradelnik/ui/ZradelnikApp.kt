package cz.jakubricar.zradelnik.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cz.jakubricar.zradelnik.SettingsSharedPreferences
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun ZradelnikApp(
    content: @Composable () -> Unit,
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
        content()
    }
}
