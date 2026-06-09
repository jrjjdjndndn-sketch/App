package com.aicompanion.pro.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkMonitor(ctx: Context) {
    private val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    private val _slow = MutableStateFlow(false)
    val slow: StateFlow<Boolean> = _slow

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(net: Network, caps: NetworkCapabilities) {
            val downKbps = caps.linkDownstreamBandwidthKbps
            val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            _slow.value = (downKbps in 1..1500) || (isCellular && downKbps < 2000)
        }

        override fun onLost(network: Network) {
            _slow.value = true
        }
    }

    fun start() {
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { cm.registerNetworkCallback(req, callback) }
    }

    fun stop() {
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    fun isSlow(): Boolean = _slow.value
}
