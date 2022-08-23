package cz.jakubricar.zradelnik

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import cz.jakubricar.zradelnik.model.Theme
import timber.log.Timber

class SettingsSharedPreferences(context: Context) {
    object Keys {

        const val THEME = "theme"
    }

    object Defaults {

        val theme = Theme.DEFAULT
    }

    // Taken from androidx.preference:preference-ktx:1.1.1 getDefaultSharedPreferences
    // For backwards compatibility
    private val preferences: SharedPreferences =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    var theme: Theme
        get() = preferences.getString(Keys.THEME, null)
            ?.let {
                try {
                    Theme.valueOf(it.uppercase())
                } catch (e: Error) {
                    Timber.e(e)
                    null
                }
            }
            ?: Defaults.theme
        set(value) = preferences.edit { putString(Keys.THEME, value.name.lowercase()) }

    fun registerOnChangeListener(listener: OnSharedPreferenceChangeListener?) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnChangeListener(listener: OnSharedPreferenceChangeListener?) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
