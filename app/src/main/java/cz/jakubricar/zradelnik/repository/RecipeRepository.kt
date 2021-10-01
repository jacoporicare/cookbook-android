package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.network.mapToData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.NumberFormat
import javax.inject.Inject
import kotlin.math.floor

class RecipeRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {

    fun getRecipes(): Flow<List<Recipe>> =
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

    fun getRecipe(slug: String): Flow<RecipeDetail?> =
        apolloClient.query(RecipeDetailQuery(slug))
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
            .build()
            .watcher()
            .toFlow()
            .mapToData(RecipeDetailQuery.Data(null))
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