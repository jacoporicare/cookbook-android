package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class Settings(
    val theme: Theme,
    val sync: Boolean,
    val syncFrequency: SyncFrequency,
    val syncWifiOnly: Boolean,
    val lastSyncDate: String
) {

    enum class Theme {
        LIGHT, DARK, DEFAULT
    }

    enum class SyncFrequency {
        DAILY, WEEKLY
    }

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

}
