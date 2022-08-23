package cz.jakubricar.zradelnik.network

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.adapter.JavaOffsetDateTimeAdapter
import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.Executable
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.api.CacheKeyGenerator
import com.apollographql.apollo3.cache.normalized.api.CacheKeyGeneratorContext
import com.apollographql.apollo3.cache.normalized.api.CacheKeyResolver
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.TypePolicyCacheKeyGenerator
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.okHttpClient
import cz.jakubricar.zradelnik.di.ZradelnikApiUrl
import cz.jakubricar.zradelnik.type.Date
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val cacheKeyGenerator = object : CacheKeyGenerator {
        override fun cacheKeyForObject(
            obj: Map<String, Any?>,
            context: CacheKeyGeneratorContext,
        ): CacheKey? =
            obj["id"]?.toString()?.let { CacheKey(context.field.type.leafType().name, it) }
                ?: TypePolicyCacheKeyGenerator.cacheKeyForObject(obj, context)
    }

    private val cacheKeyResolver = object : CacheKeyResolver() {
        override fun cacheKeyForField(
            field: CompiledField,
            variables: Executable.Variables,
        ): CacheKey? = (field.resolveArgument("id", variables) as String?)?.let {
            CacheKey(field.type.leafType().name, it)
        }
    }

    @Singleton
    @Provides
    fun provideApolloClient(
        @ZradelnikApiUrl url: String,
        @ApplicationContext appContext: Context,
    ): ApolloClient {
        val sqlCacheFactory = SqlNormalizedCacheFactory(appContext, "apollo.db")
        val memoryCacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)

        return ApolloClient.Builder()
            .serverUrl(url)
            .normalizedCache(
                normalizedCacheFactory = memoryCacheFactory.chain(sqlCacheFactory),
                cacheKeyGenerator = cacheKeyGenerator,
                cacheResolver = cacheKeyResolver
            )
            .okHttpClient(
                OkHttpClient.Builder()
                    .callTimeout(240, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .build()
            )
            .addCustomScalarAdapter(Date.type, JavaOffsetDateTimeAdapter)
            .build()
    }
}
