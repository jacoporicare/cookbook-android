package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.network.mapToData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeListRepository @Inject constructor(
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
}
