package cz.jakubricar.zradelnik.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import cz.jakubricar.zradelnik.RecipeDetailQuery
import cz.jakubricar.zradelnik.domain.RecipeDetail
import cz.jakubricar.zradelnik.network.api.asDomain
import cz.jakubricar.zradelnik.network.mapToData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    fun getRecipe(slug: String): Flow<RecipeDetail> =
        apolloClient.query(RecipeDetailQuery(slug))
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
            .build()
            .watcher()
            .toFlow()
            .mapToData(RecipeDetailQuery.Data(null))
            .map {
                it.asDomain()
            }
}
