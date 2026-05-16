package com.navrotskyi.trippyapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

object NetworkMonitor {

    private const val TAG = "NetworkMonitor"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d(TAG, "init()")
    }

    val isOnline: Flow<Boolean> by lazy {
        callbackFlow {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val initial = currentlyOnline(cm)
            Log.d(TAG, "initial online = $initial")
            trySend(initial)

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "onAvailable → true")
                    trySend(true)
                }
                override fun onLost(network: Network) {
                    val v = currentlyOnline(cm)
                    Log.d(TAG, "onLost → $v")
                    trySend(v)
                }
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    val hasInternet =
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    Log.d(TAG, "onCapabilitiesChanged → $hasInternet")
                    trySend(hasInternet)
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, callback)

            awaitClose { cm.unregisterNetworkCallback(callback) }
        }
            .distinctUntilChanged()
            .shareIn(scope, SharingStarted.Eagerly, replay = 1)
    }

    private fun currentlyOnline(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}