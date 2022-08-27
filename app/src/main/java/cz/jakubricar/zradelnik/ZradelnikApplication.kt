package cz.jakubricar.zradelnik

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import cz.jakubricar.zradelnik.di.ZradelnikApiUrl
import cz.jakubricar.zradelnik.work.getFetchRecipesPeriodicWorkRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class ZradelnikApplication : Application(), Configuration.Provider {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private fun delayedInit() {
        applicationScope.launch {
            Firebase.messaging.subscribeToTopic(BuildConfig.NEW_RECIPES_TOPIC)

            val workManager = WorkManager.getInstance(this@ZradelnikApplication)
            workManager.cancelUniqueWork("SyncData") // Cleanup old worker
            workManager.enqueueUniquePeriodicWork(
                "FetchRecipes",
                ExistingPeriodicWorkPolicy.KEEP,
                getFetchRecipesPeriodicWorkRequest()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ZradelnikApiUrl
    fun provideApiUrl(): String = BuildConfig.API_URL
}
