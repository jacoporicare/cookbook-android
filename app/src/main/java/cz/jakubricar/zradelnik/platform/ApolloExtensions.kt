package cz.jakubricar.zradelnik.platform

import com.apollographql.apollo.cache.normalized.ApolloStoreOperation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> ApolloStoreOperation<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        enqueue(
            object : ApolloStoreOperation.Callback<T> {
                private val wasCalled = AtomicBoolean(false)

                override fun onSuccess(result: T) {
                    if (!wasCalled.getAndSet(true)) {
                        cont.resume(result)
                    }
                }

                override fun onFailure(t: Throwable) {
                    if (!wasCalled.getAndSet(true)) {
                        cont.resumeWithException(t)
                    }
                }
            }
        )
    }
