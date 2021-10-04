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
import cz.jakubricar.zradelnik.getSync
import cz.jakubricar.zradelnik.getSyncFrequency
import cz.jakubricar.zradelnik.getSyncWifiOnly
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.PERIODIC_SYNC_DATA_VERSION
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
    }

    override suspend fun doWork(): Result {
        val result = syncDataRepository.fetchAllRecipeDetails(applicationContext)

        return if (result.isSuccess) Result.success() else Result.retry()
    }
}

fun Context.setupPeriodicSyncDataWork(
    newEnabled: Boolean? = null,
    newSyncFrequency: Settings.SyncFrequency? = null,
    newWifiOnly: Boolean? = null
) {
    val appPreferences = getAppSharedPreferences()
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    val enabled = newEnabled ?: preferences.getSync()

    if (!enabled) {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_NAME)
        return
    }

    val frequency = newSyncFrequency ?: preferences.getSyncFrequency()
    val wifiOnly = newWifiOnly ?: preferences.getSyncWifiOnly()

    val (interval, intervalUnit) = when (frequency) {
        Settings.SyncFrequency.DAILY -> Pair(1L, TimeUnit.DAYS)
        Settings.SyncFrequency.WEEKLY -> Pair(7L, TimeUnit.DAYS)
    }

    val request = PeriodicWorkRequestBuilder<SyncDataWorker>(interval, intervalUnit)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .apply {
                    if (wifiOnly) {
                        setRequiredNetworkType(NetworkType.UNMETERED)
                    }

                    setRequiresDeviceIdle(true)
                }.build()
        )
        .build()

    val replace = newSyncFrequency != null || newWifiOnly != null
    val versionChanged = appPreferences.periodicSyncDataVersion < PERIODIC_SYNC_DATA_VERSION

    val existingPeriodicWorkPolicy =
        if (!replace && !versionChanged) ExistingPeriodicWorkPolicy.KEEP
        else ExistingPeriodicWorkPolicy.REPLACE

    if (versionChanged) {
        appPreferences.periodicSyncDataVersion = PERIODIC_SYNC_DATA_VERSION
    }

    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(WORK_NAME, existingPeriodicWorkPolicy, request)
}
