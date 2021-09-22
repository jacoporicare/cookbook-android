package cz.jakubricar.zradelnik.repository

// import com.bumptech.glide.Glide
import android.content.Context
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
    private val apolloClient: ApolloClient
) {
    suspend fun initialFetch(context: Context): Boolean {
        if (context.getAppSharedPreferences().lastSyncDate > 0) {
            return false
        }

        fetchAllRecipeDetails(context)

        return true
    }

    suspend fun fetchAllRecipeDetails(context: Context): Result<Unit> {
        val prefs = context.getAppSharedPreferences()
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
//                    val glide = Glide.with(context)

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

                                // TODO: images
//                                recipe.fragments.recipeFragment.thumbImageUrl?.let {
//                                    launch {
//                                        glide.load(it).preload()
//                                    }
//                                }
//
//                                recipe.fragments.recipeFragment.fullImageUrl?.let {
//                                    launch {
//                                        glide.load(it).preload()
//                                    }
//                                }
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
