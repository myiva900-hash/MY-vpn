package com.example.data.model

enum class VpnStatus(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    DISCONNECTING("Disconnecting..."),
    RECONNECTING("Reconnecting..."),
    ERROR("Connection Failed")
}

enum class ProtocolType(val displayName: String, val badgeColorHex: String) {
    OVPN("OVPN", "#00E5FF"),
    WIREGUARD("WG", "#00BFA5"),
    SSH("SSH", "#2979FF"),
    V2RAY("V2RAY", "#A855F7"),
    HYSTERIA("HYSTERIA", "#FF1744"),
    SLOWDNS("DNSTT", "#FFAB00")
}

enum class PayloadType(val label: String, val description: String) {
    HTTP_DIRECT("HTTP > DIRECT", "Direct HTTP payload tunnel"),
    HTTP_PROXY("HTTP > PROXY", "HTTP proxy with custom payload"),
    SSL_DIRECT("SSL > DIRECT", "Direct SSL/TLS SNI tunneling"),
    SSL_PAYLOAD("SSL > PAYLOAD", "SSL with injected custom HTTP header"),
    SSL_WS_PROXY("SSL > WS PROXY", "WebSocket over SSL reverse proxy"),
    CUSTOM_SNI("CUSTOM SNI", "Custom Server Name Indication spoofing"),
    DNSTT_SLOWDNS("DNSTT/SlowDNS", "DNS tunnel for restricted firewall bypass"),
    UDP_HYSTERIA("UDP HYSTERIA", "High-speed UDP congestion-resistant tunnel"),
    WIREGUARD("WIREGUARD", "Kernel-speed WireGuard encryption"),
    V2RAY_XRAY("V2RAY/XRAY", "VMess / VLESS multi-layer proxy"),
    IMPORTED_CONFIG("IMPORTED CONFIG", "Custom imported .myvpn profile")
}

enum class VipTier(val label: String) {
    FREE("Free Tier"),
    VIP("VIP Active"),
    PREMIUM("Premium VIP")
}

enum class PaymentStatus(val label: String) {
    PENDING("Pending Verification"),
    APPROVED("Approved & Activated"),
    REJECTED("Rejected")
}

data class VipPackage(
    val id: String,
    val name: String,
    val durationDays: Int,
    val priceRwf: Long,
    val durationHours: Long,
    val features: List<String>,
    val isPopular: Boolean = false
)

object VipPackages {
    val ALL = listOf(
        VipPackage(
            id = "pkg_1month",
            name = "VIP 30 Days",
            durationDays = 30,
            durationHours = 30L * 24,
            priceRwf = 3000,
            features = listOf(
                "Unlimited High-Speed Bandwidth",
                "Unlock All European & US Servers",
                "Zero Connection Dropping",
                "Full VIP WhatsApp Support",
                "Multi-Protocol (OVPN, SSH, V2Ray)"
            ),
            isPopular = true
        ),
        VipPackage(
            id = "pkg_3month",
            name = "VIP 90 Days",
            durationDays = 90,
            durationHours = 90L * 24,
            priceRwf = 7500,
            features = listOf(
                "3 Months Continuous Protection",
                "Ultra-Low Ping Gaming Servers",
                "Bypass Strict Firewalls & Throttling",
                "Priority VIP Bandwidth Allocation",
                "Direct Setup Help from Yvan Mutiganda"
            )
        ),
        VipPackage(
            id = "pkg_lifetime",
            name = "VIP Lifetime Pass",
            durationDays = 3650,
            durationHours = 3650L * 24,
            priceRwf = 15000,
            features = listOf(
                "Permanent VIP Status",
                "Access to Private VPS Nodes",
                "Custom SNI & Payload Direct Tuning",
                "VIP Telegram & WhatsApp Channel",
                "Lifetime Updates & Priority Queuing"
            )
        )
    )
}

data class NetworkInfo(
    val carrierName: String = "MTN Rwanda [RW]",
    val networkType: String = "4G LTE / 5G",
    val localIp: String = "10.158.42.19",
    val gateway: String = "10.158.0.1",
    val publicIp: String = "197.243.102.8",
    val dns: String = "1.1.1.1 / 8.8.8.8"
)
