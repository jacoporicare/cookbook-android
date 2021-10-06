package cz.jakubricar.zradelnik

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
import timber.log.Timber

class SettingsSharedPreferences(context: Context) {
    object Keys {

        const val THEME = "theme"
        const val SYNC = "sync_on"
        const val SYNC_FREQUENCY = "sync"
        const val SYNC_WIFI_ONLY = "sync_wifi"
    }

    object Defaults {

        val theme = Theme.DEFAULT
        const val sync = true
        val syncFrequency = SyncFrequency.DAILY
        const val syncWifiOnly = true
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

    var sync: Boolean
        get() = preferences.getBoolean(Keys.SYNC, Defaults.sync)
        set(value) = preferences.edit { putBoolean(Keys.SYNC, value) }

    var syncFrequency: SyncFrequency
        get() = preferences.getString(Keys.SYNC_FREQUENCY, null)
            ?.let {
                try {
                    SyncFrequency.valueOf(it.uppercase())
                } catch (e: Error) {
                    Timber.e(e)
                    null
                }
            }
            ?: Defaults.syncFrequency
        set(value) = preferences.edit {
            putString(Keys.SYNC_FREQUENCY, value.name.lowercase())
        }

    var syncWifiOnly: Boolean
        get() = preferences.getBoolean(Keys.SYNC_WIFI_ONLY, Defaults.syncWifiOnly)
        set(value) = preferences.edit { putBoolean(Keys.SYNC_WIFI_ONLY, value) }

    fun registerOnChangeListener(listener: OnSharedPreferenceChangeListener?) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnChangeListener(listener: OnSharedPreferenceChangeListener?) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
