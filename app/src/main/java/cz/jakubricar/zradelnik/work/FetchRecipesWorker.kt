package cz.jakubricar.zradelnik.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import cz.jakubricar.zradelnik.repository.RecipeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FetchRecipesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val recipeRepository: RecipeRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val result = recipeRepository.refetchRecipes()

        return if (result.isSuccess) Result.success() else Result.retry()
    }
}

fun getFetchRecipesPeriodicWorkRequest() =
    PeriodicWorkRequestBuilder<FetchRecipesWorker>(1, TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresDeviceIdle(true)
                .build()
        )
        .build()
