package cz.jakubricar.zradelnik

import android.content.Context
import androidx.core.content.edit

class AppSharedPreferences(context: Context) {
    companion object {

        // Increase when data handling changes - e.g. adding new field
        const val DATA_VERSION = 7
    }

    object Keys {

        const val APP_SHARED_PREFERENCES = "App"

        const val DATA_VERSION = "DataVersion"
        const val LAST_SYNC_DATE = "LastSyncDate"
        const val PERIODIC_SYNC_DATA_VERSION = "PeriodicSyncDataVersion"
    }

    private val preferences =
        context.getSharedPreferences(Keys.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    var dataVersion: Int
        get() = preferences.getInt(Keys.DATA_VERSION, 0)
        set(value) = preferences.edit { putInt(Keys.DATA_VERSION, value) }

    var lastSyncDate: Long
        get() = preferences.getLong(Keys.LAST_SYNC_DATE, 0)
        set(value) = preferences.edit { putLong(Keys.LAST_SYNC_DATE, value) }

    var periodicSyncDataVersion: Int
        get() = preferences.getInt(Keys.PERIODIC_SYNC_DATA_VERSION, 0)
        set(value) = preferences.edit { putInt(Keys.PERIODIC_SYNC_DATA_VERSION, value) }
}
