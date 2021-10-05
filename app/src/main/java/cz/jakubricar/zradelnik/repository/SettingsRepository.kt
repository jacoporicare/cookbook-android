package cz.jakubricar.zradelnik.repository

import android.app.Application
import androidx.preference.PreferenceManager
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.getSync
import cz.jakubricar.zradelnik.getSyncFrequency
import cz.jakubricar.zradelnik.getSyncWifiOnly
import cz.jakubricar.zradelnik.getTheme
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.setSync
import cz.jakubricar.zradelnik.setSyncFrequency
import cz.jakubricar.zradelnik.setSyncWifiOnly
import cz.jakubricar.zradelnik.setTheme
import cz.jakubricar.zradelnik.work.setupPeriodicSyncDataWork
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val app: Application
) {

    private val prefManager = PreferenceManager.getDefaultSharedPreferences(app)

    fun getSettings() =
        Settings(
            theme = prefManager.getTheme(),
            sync = prefManager.getSync(),
            syncFrequency = prefManager.getSyncFrequency(),
            syncWifiOnly = prefManager.getSyncWifiOnly(),
            lastSyncDate = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .format(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(app.getAppSharedPreferences().lastSyncDate),
                        ZoneId.systemDefault()
                    )
                )
        )

    fun setTheme(theme: Settings.Theme) {
        prefManager.setTheme(theme)
    }

    fun setSync(sync: Boolean) {
        prefManager.setSync(sync)
        app.setupPeriodicSyncDataWork(newSync = sync)
    }

    fun setSyncFrequency(syncFrequency: Settings.SyncFrequency) {
        prefManager.setSyncFrequency(syncFrequency)
        app.setupPeriodicSyncDataWork(newSyncFrequency = syncFrequency)
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        prefManager.setSyncWifiOnly(syncWifiOnly)
        app.setupPeriodicSyncDataWork(newWifiOnly = syncWifiOnly)
    }
}
