package cz.jakubricar.zradelnik.repository

import android.app.Application
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
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

    private val prefManager = app.getSettingsSharedPreferences()

    fun getSettings() =
        Settings(
            theme = prefManager.theme,
            sync = prefManager.sync,
            syncFrequency = prefManager.syncFrequency,
            syncWifiOnly = prefManager.syncWifiOnly,
            lastSyncDate = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .format(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(app.getAppSharedPreferences().lastSyncDate),
                        ZoneId.systemDefault()
                    )
                )
        )

    fun setTheme(theme: Theme) {
        prefManager.theme = theme
    }

    fun setSync(sync: Boolean) {
        prefManager.sync = sync
        app.setupPeriodicSyncDataWork(newSync = sync)
    }

    fun setSyncFrequency(syncFrequency: SyncFrequency) {
        prefManager.syncFrequency = syncFrequency
        app.setupPeriodicSyncDataWork(newSyncFrequency = syncFrequency)
    }

    fun setSyncWifiOnly(syncWifiOnly: Boolean) {
        prefManager.syncWifiOnly = syncWifiOnly
        app.setupPeriodicSyncDataWork(newWifiOnly = syncWifiOnly)
    }
}
