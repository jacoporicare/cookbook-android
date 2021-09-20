package cz.jakubricar.zradelnik

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

fun Context.getAppSharedPreferences() = AppSharedPreferences(this)

fun Context.setupNightMode(forceValue: String? = null) {
    val mode = forceValue ?: PreferenceManager.getDefaultSharedPreferences(this)
        .getString(getString(R.string.preference_theme_key), "")

    AppCompatDelegate.setDefaultNightMode(
        when (mode) {
            getString(R.string.preference_theme_values_light) -> AppCompatDelegate.MODE_NIGHT_NO
            getString(R.string.preference_theme_values_dark) -> AppCompatDelegate.MODE_NIGHT_YES
            else ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
        }
    )
}
