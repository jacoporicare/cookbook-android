package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.request.RequestHeaders
import cz.jakubricar.zradelnik.CreateRecipeMutation
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.UpdateRecipeMutation
import cz.jakubricar.zradelnik.fragment.RecipeDetailFragment
import cz.jakubricar.zradelnik.fragment.RecipeFragment
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.network.mapToData
import cz.jakubricar.zradelnik.network.toResult
import cz.jakubricar.zradelnik.platform.await
import cz.jakubricar.zradelnik.type.RecipeInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.floor

class RecipeRepository @Inject constructor(
    private val apolloClient: ApolloClient,
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
                        title = recipe.title,
                        imageUrl = recipe.thumbImageUrl,
                    )
                }
            }

    suspend fun getRecipeDetail(id: String): Result<RecipeDetail?> =
        try {
            apolloClient.query(RecipeDetailQuery(id))
                .toBuilder()
                .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
                .build()
                .await()
                .toResult()
                .map { data ->
                    data.recipe?.let {
                        val recipe = it.fragments.recipeFragment
                        val recipeDetail = it.fragments.recipeDetailFragment

                        mapFragmentsToRecipeDetail(recipe, recipeDetail)
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getRecipeEdit(id: String): Result<RecipeEdit?> =
        try {
            apolloClient.query(RecipeDetailQuery(id))
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
                            tags = recipe.tags,
                        )
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun saveRecipe(
        authToken: String,
        id: String?,
        input: RecipeInput,
    ): Result<RecipeDetail> =
        try {
            if (id != null) {
                apolloClient.mutate(UpdateRecipeMutation(id, input, Input.absent()))
                    .toBuilder()
                    .requestHeaders(
                        RequestHeaders.builder()
                            .addHeader("Authorization", "Bearer $authToken")
                            .build()
                    )
                    .build()
                    .await()
                    .toResult()
                    .map { data ->
                        data.updateRecipe.let {
                            val recipe = it.fragments.recipeFragment
                            val recipeDetail = it.fragments.recipeDetailFragment

                            mapFragmentsToRecipeDetail(recipe, recipeDetail)
                        }
                    }
            } else {
                apolloClient.mutate(CreateRecipeMutation(input, Input.absent()))
                    .toBuilder()
                    .requestHeaders(
                        RequestHeaders.builder()
                            .addHeader("Authorization", "Bearer $authToken")
                            .build()
                    )
                    .build()
                    .await()
                    .toResult()
                    .map { data ->
                        data.createRecipe.let {
                            val recipe = it.fragments.recipeFragment
                            val recipeDetail = it.fragments.recipeDetailFragment

                            try {
                                val cachedRecipes = apolloClient.apolloStore
                                    .read(RecipeListQuery())
                                    .await()
                                    .recipes

                                apolloClient.apolloStore
                                    .writeAndPublish(
                                        RecipeListQuery(),
                                        RecipeListQuery.Data(
                                            cachedRecipes + RecipeListQuery.Recipe(
                                                fragments = RecipeListQuery.Recipe.Fragments(recipe)
                                            )
                                        )
                                    )
                                    .await()

                                apolloClient.apolloStore
                                    .writeAndPublish(
                                        RecipeDetailQuery(recipe.id),
                                        RecipeDetailQuery.Data(
                                            RecipeDetailQuery.Recipe(
                                                it.__typename,
                                                RecipeDetailQuery.Recipe.Fragments(
                                                    recipe,
                                                    recipeDetail
                                                )
                                            )
                                        )
                                    )
                                    .await()
                            } catch (e: Exception) {
                                Timber.e(e)
                            }

                            mapFragmentsToRecipeDetail(recipe, recipeDetail)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun mapFragmentsToRecipeDetail(
        recipe: RecipeFragment,
        recipeDetail: RecipeDetailFragment,
    ) =
        RecipeDetail(
            id = recipe.id,
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
