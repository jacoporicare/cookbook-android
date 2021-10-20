package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.network.mapToData
import cz.jakubricar.zradelnik.network.toResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.floor

class RecipeRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecipes(): Flow<List<Recipe>> =
        apolloClient.query(RecipeListQuery())
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
            .build()
            .watcher()
            .toFlow()
            .mapToData(RecipeListQuery.Data(emptyList()))
            .map { data ->
                data.recipes.map {
                    val recipe = it.fragments.recipeFragment

                    Recipe(
                        id = recipe.id,
                        slug = recipe.slug,
                        title = recipe.title,
                        imageUrl = recipe.thumbImageUrl,
                    )
                }
            }

    suspend fun getRecipeDetail(slug: String): Result<RecipeDetail?> =
        try {
            apolloClient.query(RecipeDetailQuery(slug))
                .toBuilder()
                .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
                .build()
                .await()
                .toResult()
                .map { data ->
                    data.recipe?.let {
                        val recipe = it.fragments.recipeFragment
                        val recipeDetail = it.fragments.recipeDetailFragment

                        RecipeDetail(
                            id = recipe.id,
                            slug = recipe.slug,
                            title = recipe.title,
                            imageUrl = recipe.fullImageUrl,
                            directions = recipeDetail.directions,
                            ingredients = recipeDetail.ingredients?.map { ingredient ->
                                RecipeDetail.Ingredient(
                                    id = ingredient.id,
                                    name = ingredient.name,
                                    isGroup = ingredient.isGroup,
                                    amount = ingredient.amount?.let { amount ->
                                        NumberFormat.getInstance().format(amount)
                                    },
                                    amountUnit = ingredient.amountUnit,
                                )
                            } ?: emptyList(),
                            preparationTime = recipeDetail.preparationTime?.let { time ->
                                formatTime(time)
                            },
                            servingCount = recipeDetail.servingCount?.toString(),
                            sideDish = recipeDetail.sideDish,
                        )
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getRecipeEdit(slug: String): Result<RecipeEdit?> =
        try {
            apolloClient.query(RecipeDetailQuery(slug))
                .toBuilder()
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
                .build()
                .await()
                .toResult()
                .map { data ->
                    data.recipe?.let {
                        val recipe = it.fragments.recipeFragment
                        val recipeDetail = it.fragments.recipeDetailFragment

                        RecipeEdit(
                            id = recipe.id,
                            slug = recipe.slug,
                            title = recipe.title,
                            imageUrl = recipe.fullImageUrl,
                            directions = recipeDetail.directions,
                            ingredients = recipeDetail.ingredients?.map { ingredient ->
                                RecipeEdit.Ingredient(
                                    id = ingredient.id,
                                    name = ingredient.name,
                                    isGroup = ingredient.isGroup,
                                    amount = ingredient.amount,
                                    amountUnit = ingredient.amountUnit,
                                )
                            } ?: emptyList(),
                            preparationTime = recipeDetail.preparationTime,
                            servingCount = recipeDetail.servingCount,
                            sideDish = recipeDetail.sideDish,
                        )
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun formatTime(time: Int): String {
        val hours = floor(time.toDouble() / 60).toInt()
        val minutes = time % 60

        if (hours > 0 && minutes == 0) {
            return "$hours h"
        }

        if (hours > 0 && minutes > 0) {
            return "$hours h $minutes min"
        }

        return "$minutes min"
    }
}
