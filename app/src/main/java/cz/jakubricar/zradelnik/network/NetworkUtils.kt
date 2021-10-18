package cz.jakubricar.zradelnik.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalCoroutinesApi::class)
fun Context.observeConnectedAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}

@Composable
fun connectedState(): State<Boolean> {
    val context = LocalContext.current

    val activeNetworks by produceState(initialValue = 0) {
        // In a coroutine, can make suspend calls
        context.observeConnectedAsFlow()
            .collect {
                value = if (it) value + 1 else value - 1
            }
    }

    return remember { derivedStateOf { activeNetworks > 0 } }
}
