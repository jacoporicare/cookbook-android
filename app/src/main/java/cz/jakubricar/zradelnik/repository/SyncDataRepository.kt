package cz.jakubricar.zradelnik.repository

import android.app.Application
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import cz.jakubricar.zradelnik.AllRecipeDetailsQuery
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.getAppSharedPreferences
import cz.jakubricar.zradelnik.network.NetworkModule.RecipeCacheKeyResolver.getCacheKey
import cz.jakubricar.zradelnik.network.toResult
import cz.jakubricar.zradelnik.platform.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

class SyncDataRepository @Inject constructor(
    private val apolloClient: ApolloClient,
    private val app: Application,
) {

    fun initialSync() = app.getAppSharedPreferences().lastSyncDate == 0L

    suspend fun fetchAllRecipeDetails(): Result<Unit> {
        val prefs = app.getAppSharedPreferences()
        val since = Instant.ofEpochMilli(prefs.lastSyncDate).atOffset(ZoneOffset.UTC)
        val initialLoad = prefs.lastSyncDate == 0L

        return try {
            apolloClient.query(AllRecipeDetailsQuery(since, !initialLoad))
                .toBuilder()
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
                .build()
                .await()
                .toResult()
                .map { data ->
                    val imageLoader = app.imageLoader

                    withContext(Dispatchers.IO) {
                        val addedOrUpdatedRecipes = data.recipes
                            .filter { !it.deleted }
                            .map { recipe ->
                                RecipeListQuery.Recipe(
                                    recipe.__typename,
                                    RecipeListQuery.Recipe.Fragments(
                                        recipe.fragments.recipeFragment
                                    )
                                )
                            }

                        val cachedRecipes = try {
                            apolloClient.apolloStore
                                .read(RecipeListQuery())
                                .await()
                                .recipes
                        } catch (e: Exception) {
                            emptyList()
                        }

                        // Has to be saved before writeAndPublish to prevent infinite loop
                        prefs.lastSyncDate = Instant.now().toEpochMilli()

                        try {
                            apolloClient.apolloStore
                                .writeAndPublish(
                                    RecipeListQuery(),
                                    RecipeListQuery.Data(
                                        cachedRecipes
                                            .filter { recipe ->
                                                !data.recipes.any { oldRecipe ->
                                                    recipe.fragments.recipeFragment.id ==
                                                        oldRecipe.fragments.recipeFragment.id
                                                }
                                            }
                                            .plus(addedOrUpdatedRecipes)
                                    )
                                )
                                .await()
                        } catch (e: Exception) {
                            Timber.e(e)
                        }

                        data.recipes.forEach { recipe ->
                            try {
                                if (recipe.deleted) {
                                    apolloClient.apolloStore.remove(
                                        getCacheKey(
                                            recipe.__typename,
                                            recipe.fragments.recipeFragment.id
                                        )
                                    )

                                    return@forEach
                                }

                                apolloClient.apolloStore
                                    .writeAndPublish(
                                        RecipeDetailQuery(recipe.fragments.recipeFragment.slug),
                                        RecipeDetailQuery.Data(
                                            RecipeDetailQuery.Recipe(
                                                recipe.__typename,
                                                RecipeDetailQuery.Recipe.Fragments(
                                                    recipe.fragments.recipeFragment,
                                                    recipe.fragments.recipeDetailFragment
                                                )
                                            )
                                        )
                                    )
                                    .await()

                                recipe.fragments.recipeFragment.thumbImageUrl?.let {
                                    val request = ImageRequest.Builder(app)
                                        .data(it)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .build()
                                    imageLoader.enqueue(request)
                                }

                                recipe.fragments.recipeFragment.fullImageUrl?.let {
                                    val request = ImageRequest.Builder(app)
                                        .data(it)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .build()
                                    imageLoader.enqueue(request)
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
