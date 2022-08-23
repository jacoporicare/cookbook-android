package cz.jakubricar.zradelnik.network

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <D : Operation.Data> Flow<ApolloResponse<D>>.mapToData(defValue: D? = null): Flow<D> =
    map { response ->
        response.data
            ?: response.parseApiException()?.let { throw it }
            ?: defValue
            ?: throw Exception("No data, no error")
    }

fun <D : Operation.Data> ApolloResponse<D>.toResult(): Result<D> =
    data?.let { Result.success(it) }
        ?: parseApiException()?.let { Result.failure(it) }
        ?: Result.failure(Exception("No data, no error."))

private fun ApolloResponse<*>.parseApiException(): Exception? =
    errors?.first()?.let {
        Exception(it.message)
    }
