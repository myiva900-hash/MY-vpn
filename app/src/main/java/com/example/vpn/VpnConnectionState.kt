package com.example.vpn

import com.example.data.model.VpnStatus

data class VpnConnectionState(
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val serverId: String = "ovpn_fr_1",
    val serverName: String = "OVPN PREMIUM SERVER 1",
    val serverHost: String = "fr1.mutigandavpn.com",
    val serverPort: Int = 1194,
    val protocol: String = "OVPN",
    val flagEmoji: String = "🇫🇷",
    val uploadSpeedBps: Long = 0L,
    val downloadSpeedBps: Long = 0L,
    val durationSeconds: Long = 0L,
    val connectedAt: Long = 0L,
    val errorMessage: String? = null
)
