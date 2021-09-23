package cz.jakubricar.zradelnik.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.PERIODIC_SYNC_DATA_VERSION
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.SYNC_INTERVAL
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.SYNC_INTERVAL_DAILY
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.SYNC_INTERVAL_WEEKLY
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.SYNC_ON
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.SYNC_WIFI
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.WORK_NAME
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

        const val WORK_NAME = "SyncData"

        // Increase when work definition changes
        const val PERIODIC_SYNC_DATA_VERSION = 1

        const val SYNC_ON = "sync_on"
        const val SYNC_INTERVAL = "sync"
        const val SYNC_INTERVAL_DAILY = "daily"
        const val SYNC_INTERVAL_WEEKLY = "weekly"
        const val SYNC_WIFI = "sync_wifi"
    }

    override suspend fun doWork(): Result {
        val result = syncDataRepository.fetchAllRecipeDetails(applicationContext)

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}

fun Context.setupPeriodicSyncDataWork(
    newEnabled: Boolean? = null,
    newIntervalType: String? = null,
    newUnMeteredOnly: Boolean? = null
) {
    val appPreferences = getAppSharedPreferences()
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)

    val enabled = newEnabled
        ?: preferences.getBoolean(SYNC_ON, true)

    if (!enabled) {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_NAME)
        return
    }

    val intervalType = newIntervalType
        ?: preferences.getString(SYNC_INTERVAL, SYNC_INTERVAL_DAILY)
    val unMeteredOnly = newUnMeteredOnly
        ?: preferences.getBoolean(SYNC_WIFI, true)

    val (interval, intervalUnit) = when (intervalType) {
        SYNC_INTERVAL_DAILY -> Pair(1L, TimeUnit.DAYS)
        SYNC_INTERVAL_WEEKLY -> Pair(7L, TimeUnit.DAYS)
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
        WORK_NAME,
        existingPeriodicWorkPolicy,
        request
    )
}
