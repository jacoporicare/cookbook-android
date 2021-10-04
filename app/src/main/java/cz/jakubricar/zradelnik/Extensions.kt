package cz.jakubricar.zradelnik

import android.content.Context
import android.content.SharedPreferences
import cz.jakubricar.zradelnik.model.Settings
import timber.log.Timber

fun Context.getAppSharedPreferences() = AppSharedPreferences(this)

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

fun SharedPreferences.getSync() = getBoolean(Settings.Keys.SYNC, Settings.Defaults.sync)

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

fun SharedPreferences.getSyncWifiOnly() =
    getBoolean(Settings.Keys.SYNC_WIFI_ONLY, Settings.Defaults.syncWifiOnly)
