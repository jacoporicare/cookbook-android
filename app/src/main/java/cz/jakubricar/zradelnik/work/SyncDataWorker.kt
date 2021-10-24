package cz.jakubricar.zradelnik.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.repository.SyncDataRepository
import cz.jakubricar.zradelnik.work.SyncDataWorker.Companion.WORK_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncDataRepository: SyncDataRepository,
) : CoroutineWorker(appContext, params) {

    companion object {

        const val WORK_NAME = "SyncData"

        // Increase when work definition changes
        const val PERIODIC_SYNC_DATA_VERSION = 2
    }

    override suspend fun doWork(): Result {
        val result = syncDataRepository.fetchAllRecipeDetails()

        return if (result.isSuccess) Result.success() else Result.retry()
    }
}

fun Context.setupPeriodicSyncDataWork(
    newSync: Boolean? = null,
    newSyncFrequency: SyncFrequency? = null,
    newWifiOnly: Boolean? = null,
) {
    val preferences = getSettingsSharedPreferences()
    val sync = newSync ?: preferences.sync

    if (!sync) {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_NAME)
        return
    }

    val frequency = newSyncFrequency ?: preferences.syncFrequency
    val wifiOnly = newWifiOnly ?: preferences.syncWifiOnly

    val (interval, intervalUnit) = when (frequency) {
        SyncFrequency.DAILY -> Pair(1L, TimeUnit.DAYS)
        SyncFrequency.WEEKLY -> Pair(7L, TimeUnit.DAYS)
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

    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
}
