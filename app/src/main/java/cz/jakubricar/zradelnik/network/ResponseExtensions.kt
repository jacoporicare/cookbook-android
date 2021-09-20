package cz.jakubricar.zradelnik.network

import com.apollographql.apollo.api.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <Data> Flow<Response<Data>>.mapToData(defValue: Data? = null): Flow<Data> =
    map { response ->
        response.data
            ?: response.parseApiException()?.let { throw it }
            ?: defValue
            ?: throw Exception("No data, no error")
    }

fun <T> Response<T>.toResult(): Result<T> =
    data?.let { Result.success(it) }
        ?: parseApiException()?.let { Result.failure(it) }
        ?: Result.failure(Exception("No data, no error."))

private fun Response<*>.parseApiException(): Exception? =
    errors?.first()?.let {
        Exception(it.message)
    }
