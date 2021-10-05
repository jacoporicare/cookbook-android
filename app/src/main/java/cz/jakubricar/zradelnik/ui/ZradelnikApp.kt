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
import androidx.preference.PreferenceManager
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import cz.jakubricar.zradelnik.getTheme
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun ZradelnikApp() {
    val context = LocalContext.current
    var theme by remember {
        mutableStateOf(PreferenceManager.getDefaultSharedPreferences(context).getTheme())
    }

    DisposableEffect(context) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == Settings.Keys.THEME) {
                theme = prefs.getTheme()
            }
        }

        val prefManager = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(listener)
        }

        onDispose {
            prefManager.unregisterOnSharedPreferenceChangeListener(listener)
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

            ZradelnikNavGraph()
        }
    }
}
