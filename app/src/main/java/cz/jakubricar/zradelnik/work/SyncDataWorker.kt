package cz.jakubricar.zradelnik.work

import android.content.Context
import androidx.core.content.edit
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cz.jakubricar.zradelnik.AppSharedPreferences.Companion.PERIODIC_SYNC_DATA_VERSION
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.PERIODIC_SYNC_DATA_WORK_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncDataRepository: SyncDataRepository
) : CoroutineWorker(appContext, params) {
    companion object {
        const val PERIODIC_SYNC_DATA_WORK_NAME = "SyncData"
    }

    override suspend fun doWork(): Result {
        return try {
            syncDataRepository.fetchAllRecipeDetails(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

fun Context.setupPeriodicSyncDataWork(
    newEnabled: Boolean? = null,
    newIntervalType: String? = null,
    newUnMeteredOnly: Boolean? = null
) {
    val daily = getString(R.string.preference_sync_values_daily)
    val weekly = getString(R.string.preference_sync_values_weekly)

    val appPreferences = getAppSharedPreferences()
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)

    // Migrate old "never"
    val syncFreq = preferences.getString(getString(R.string.preference_sync_key), "")
    val never = getString(R.string.preference_sync_values_never)
    if (syncFreq == never) {
        preferences.edit {
            putBoolean(getString(R.string.preference_sync_on_key), false)
            putString(getString(R.string.preference_sync_key), daily)
        }
    }

    val enabled = newEnabled
        ?: preferences.getBoolean(getString(R.string.preference_sync_on_key), true)

    if (!enabled) {
        WorkManager.getInstance(this).cancelUniqueWork(PERIODIC_SYNC_DATA_WORK_NAME)
        return
    }

    val intervalType = newIntervalType
        ?: preferences.getString(getString(R.string.preference_sync_key), daily)
    val unMeteredOnly = newUnMeteredOnly
        ?: preferences.getBoolean(getString(R.string.preference_sync_wifi_key), true)

    val (interval, intervalUnit) = when (intervalType) {
        daily -> Pair(1L, TimeUnit.DAYS)
        weekly -> Pair(7L, TimeUnit.DAYS)
        else -> {
            throw IllegalArgumentException("Invalid interval type")
        }
    }

    val request = PeriodicWorkRequestBuilder<SyncDataWorker>(interval, intervalUnit)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .apply {
                    if (unMeteredOnly) {
                        setRequiredNetworkType(NetworkType.UNMETERED)
                    }

                    setRequiresDeviceIdle(true)
                }.build()
        )
        .build()

    val replace = newIntervalType != null || newUnMeteredOnly != null
    val versionChanged =
        appPreferences.periodicSyncDataVersion < PERIODIC_SYNC_DATA_VERSION

    val existingPeriodicWorkPolicy =
        if (!replace && !versionChanged) ExistingPeriodicWorkPolicy.KEEP
        else ExistingPeriodicWorkPolicy.REPLACE

    if (versionChanged) {
        appPreferences.periodicSyncDataVersion = PERIODIC_SYNC_DATA_VERSION
    }

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        PERIODIC_SYNC_DATA_WORK_NAME,
        existingPeriodicWorkPolicy,
        request
    )
}
