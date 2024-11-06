package cz.jakubricar.zradelnik.ui

import android.content.SharedPreferences
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cz.jakubricar.zradelnik.SettingsSharedPreferences
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun ZradelnikApp(
    enableEdgeToEdge: (SystemBarStyle, SystemBarStyle) -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSettingsSharedPreferences() }
    var theme by remember { mutableStateOf(prefs.theme) }
    val isDarkTheme = shouldUseDarkTheme(theme)

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

    DisposableEffect(isDarkTheme) {
        enableEdgeToEdge(
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ) { isDarkTheme },
            SystemBarStyle.auto(
                lightScrim,
                darkScrim,
            ) { isDarkTheme },
        )
        onDispose {}
    }

    ZradelnikTheme(theme = theme) {
        content()
    }
}


@Composable
private fun shouldUseDarkTheme(theme: Theme) =
    when (theme) {
        Theme.DEFAULT -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)
