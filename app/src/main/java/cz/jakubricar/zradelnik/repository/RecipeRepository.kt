package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.DefaultUpload
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.refetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import cz.jakubricar.zradelnik.CreateRecipeMutation
import cz.jakubricar.zradelnik.DeleteRecipeMutation
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.RecipeEditQuery
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.UpdateRecipeMutation
import cz.jakubricar.zradelnik.fragment.RecipeFragment
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.model.RecipeEdit
import cz.jakubricar.zradelnik.network.mapToData
import cz.jakubricar.zradelnik.network.toResult
import cz.jakubricar.zradelnik.type.RecipeInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.floor

class RecipeRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) {

    fun observeRecipes(): Flow<List<Recipe>> =
        apolloClient.query(RecipeListQuery())
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .refetchPolicy(FetchPolicy.CacheFirst)
            .watch()
            .mapToData(RecipeListQuery.Data(emptyList()))
            .map { data ->
                data.recipes.map {
                    mapFragmentToRecipe(it.recipeFragment)
                }
            }

    suspend fun getRecipeDetail(id: String): Result<Recipe?> =
        try {
            apolloClient.query(RecipeDetailQuery(id = id))
                .fetchPolicy(FetchPolicy.CacheFirst)
                .execute()
                .toResult()
                .map { data -> data.recipe?.recipeFragment?.let { mapFragmentToRecipe(it) } }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getRecipeEdit(id: String): Result<RecipeEdit?> =
        try {
            apolloClient.query(RecipeEditQuery(id = id))
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()
                .toResult()
                .map { data ->
                    data.recipe?.let {
                        val recipe = it.recipeFragment

                        RecipeEdit(
                            id = recipe.id,
                            title = recipe.title,
                            imageUrl = recipe.fullImageUrl,
                            directions = recipe.directions,
                            ingredients = recipe.ingredients?.map { ingredient ->
                                RecipeEdit.Ingredient(
                                    id = ingredient.id,
                                    name = ingredient.name,
                                    isGroup = ingredient.isGroup,
                                    amount = ingredient.amount,
                                    amountUnit = ingredient.amountUnit,
                                )
                            } ?: emptyList(),
                            preparationTime = recipe.preparationTime,
                            servingCount = recipe.servingCount,
                            sideDish = recipe.sideDish,
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
        newImage: RecipeEdit.NewImage?,
    ): Result<Recipe> =
        try {
            val imageUpload = newImage?.let {
                DefaultUpload.Builder()
                    .content(it.bytes)
                    .contentType(it.mimeType)
                    .fileName("photo")
                    .build()
            }

            if (id != null) {
                apolloClient.mutation(
                    UpdateRecipeMutation(
                        id,
                        input,
                        Optional.presentIfNotNull(imageUpload)
                    )
                )
                    .addHttpHeader("Authorization", "Bearer $authToken")
                    .execute()
                    .toResult()
                    .map { mapFragmentToRecipe(it.updateRecipe.recipeFragment) }
            } else {
                apolloClient.mutation(
                    CreateRecipeMutation(
                        input,
                        Optional.presentIfNotNull(imageUpload)
                    )
                )
                    .addHttpHeader("Authorization", "Bearer $authToken")
                    .execute()
                    .toResult()
                    .map { mapFragmentToRecipe(it.createRecipe.recipeFragment) }
                    .also { refetchRecipes() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun deleteRecipe(authToken: String, id: String): Result<Unit> =
        try {
            apolloClient.mutation(DeleteRecipeMutation(id))
                .addHttpHeader("Authorization", "Bearer $authToken")
                .execute()
                .toResult()
                .map { }
                .also { refetchRecipes() }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun refetchRecipes() =
        try {
            apolloClient.query(RecipeListQuery())
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()
                .toResult()
                .map { }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun mapFragmentToRecipe(recipe: RecipeFragment) =
        Recipe(
            id = recipe.id,
            title = recipe.title,
            imageUrl = recipe.fullImageUrl,
            directions = recipe.directions,
            ingredients = recipe.ingredients?.map { ingredient ->
                Recipe.Ingredient(
                    id = ingredient.id,
                    name = ingredient.name,
                    isGroup = ingredient.isGroup,
                    amount = ingredient.amount?.let { amount ->
                        NumberFormat.getInstance().format(amount)
                    },
                    amountUnit = ingredient.amountUnit,
                )
            } ?: emptyList(),
            preparationTime = recipe.preparationTime?.let { time ->
                formatTime(time)
            },
            servingCount = recipe.servingCount?.toString(),
            sideDish = recipe.sideDish,
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
