package cz.jakubricar.zradelnik

import android.content.SharedPreferences
import androidx.core.content.edit
import cz.jakubricar.zradelnik.model.Settings
import timber.log.Timber

fun SharedPreferences.getTheme() =
    getString(Settings.Keys.THEME, null)
        ?.let {
            try {
                Settings.Theme.valueOf(it.uppercase())
            } catch (e: Error) {
                Timber.e(e)
                null
            }
        }
        ?: Settings.Defaults.theme

fun SharedPreferences.setTheme(theme: Settings.Theme) =
    edit { putString(Settings.Keys.THEME, theme.name.lowercase()) }

fun SharedPreferences.getSync() = getBoolean(Settings.Keys.SYNC, Settings.Defaults.sync)

fun SharedPreferences.setSync(sync: Boolean) =
    edit { putBoolean(Settings.Keys.SYNC, sync) }

fun SharedPreferences.getSyncFrequency() =
    getString(Settings.Keys.SYNC_FREQUENCY, null)
        ?.let {
            try {
                Settings.SyncFrequency.valueOf(it.uppercase())
            } catch (e: Error) {
                Timber.e(e)
                null
            }
        }
        ?: Settings.Defaults.syncFrequency

fun SharedPreferences.setSyncFrequency(syncFrequency: Settings.SyncFrequency) =
    edit { putString(Settings.Keys.SYNC_FREQUENCY, syncFrequency.name.lowercase()) }

fun SharedPreferences.getSyncWifiOnly() =
    getBoolean(Settings.Keys.SYNC_WIFI_ONLY, Settings.Defaults.syncWifiOnly)

fun SharedPreferences.setSyncWifiOnly(syncWifiOnly: Boolean) =
    edit { putBoolean(Settings.Keys.SYNC_WIFI_ONLY, syncWifiOnly) }
