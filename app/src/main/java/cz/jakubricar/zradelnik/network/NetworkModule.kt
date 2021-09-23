package cz.jakubricar.zradelnik.network

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import cz.jakubricar.zradelnik.AppSharedPreferences.Companion.DATA_VERSION
import cz.jakubricar.zradelnik.di.ZradelnikApiUrl
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.type.CustomType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    object RecipeCacheKeyResolver : CacheKeyResolver() {

        fun getCacheKey(typename: Any?, id: Any?) =
            CacheKey.from("$typename.$id")

        override fun fromFieldArguments(
            field: ResponseField,
            variables: Operation.Variables
        ): CacheKey {
            return CacheKey.NO_KEY
        }

        override fun fromFieldRecordSet(
            field: ResponseField,
            recordSet: Map<String, Any>
        ): CacheKey {
            return if (recordSet.containsKey("id") && recordSet["id"] != null) {
                getCacheKey(recordSet["__typename"], recordSet["id"])
            } else {
                CacheKey.NO_KEY
            }
        }
    }

    private val dateCustomTypeAdapter = object : CustomTypeAdapter<OffsetDateTime> {
        override fun decode(value: CustomTypeValue<*>): OffsetDateTime =
            OffsetDateTime.parse(value.value as String, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        override fun encode(value: OffsetDateTime): CustomTypeValue<*> =
            CustomTypeValue.GraphQLString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }

    @Singleton
    @Provides
    fun provideApolloClient(
        @ZradelnikApiUrl url: String,
        @ApplicationContext appContext: Context
    ): ApolloClient {
        val sqlCacheFactory = SqlNormalizedCacheFactory(appContext, "apollo.db")
        val memoryCacheFactory = LruNormalizedCacheFactory(
            EvictionPolicy.builder()
                .maxSizeBytes(10L * 1024 * 1024)
                .build()
        )

        val client = ApolloClient.builder()
            .serverUrl(url)
            .normalizedCache(memoryCacheFactory.chain(sqlCacheFactory), RecipeCacheKeyResolver)
            .okHttpClient(
                OkHttpClient.Builder()
                    .callTimeout(240, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .build()
            )
            .addCustomTypeAdapter(CustomType.DATE, dateCustomTypeAdapter)
            .build()

        val prefs = appContext.getAppSharedPreferences()

        if (prefs.dataVersion != DATA_VERSION) {
            client.clearNormalizedCache()
            prefs.dataVersion = DATA_VERSION
            prefs.lastSyncDate = 0
        }

        return client
    }
}
