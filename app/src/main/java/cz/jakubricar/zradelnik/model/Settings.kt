package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class Settings(
    val theme: Theme,
    val sync: Boolean,
    val syncFrequency: SyncFrequency,
    val syncWifiOnly: Boolean,
    val lastSyncDate: String,
)

enum class Theme {
    LIGHT, DARK, DEFAULT
}

enum class SyncFrequency {
    DAILY, WEEKLY
}
