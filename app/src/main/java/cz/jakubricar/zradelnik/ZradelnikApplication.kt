package cz.jakubricar.zradelnik

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import cz.jakubricar.zradelnik.di.ZradelnikApiUrl
import cz.jakubricar.zradelnik.work.setupPeriodicSyncDataWork
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
            setupPeriodicSyncDataWork()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        delayedInit()
        setupNightMode()
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
