package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            val ipCandidates = mutableListOf<String>()

            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val name = networkInterface.name.lowercase()
                // skip virtual interfaces
                if (name.startsWith("dummy") || name.startsWith("p2p") || name.startsWith("tun")) continue

                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val hostAddress = address.hostAddress ?: continue
                        // wlan or ap interfaces have highest priority
                        if (name.startsWith("wlan") || name.startsWith("ap") || name.startsWith("swlan")) {
                            return hostAddress
                        }
                        ipCandidates.add(hostAddress)
                    }
                }
            }

            return ipCandidates.firstOrNull { !it.startsWith("127.") } ?: "127.0.0.1"
        } catch (e: Exception) {
            e.printStackTrace()
            return "127.0.0.1"
        }
    }

    fun getNetworkName(context: Context): Pair<String, Boolean> {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)

            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val info = wifiManager?.connectionInfo
                val rawSsid = info?.ssid
                val cleanSsid = if (rawSsid != null && rawSsid != "<unknown ssid>") {
                    rawSsid.trim('"')
                } else {
                    "شبكة Wi-Fi"
                }
                return Pair(cleanSsid, false)
            }

            // Check if hotspot/tethering is active (common IP range 192.168.43.x)
            val ip = getLocalIpAddress()
            if (ip.startsWith("192.168.43.") || ip.startsWith("192.168.49.")) {
                return Pair("نقطة اتصال الهاتف (Hotspot)", true)
            }

            if (ip != "127.0.0.1") {
                return Pair("شبكة محلية LAN", false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair("غير متصل بشبكة", false)
    }
}
