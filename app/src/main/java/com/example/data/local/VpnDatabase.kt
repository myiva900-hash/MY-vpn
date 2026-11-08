package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CustomConfigDao
import com.example.data.local.dao.LogDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ServerDao
import com.example.data.local.entities.CustomConfigEntity
import com.example.data.local.entities.LogEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VpnServerEntity::class,
        PaymentEntity::class,
        UserAccountEntity::class,
        CustomConfigEntity::class,
        LogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun paymentDao(): PaymentDao
    abstract fun accountDao(): AccountDao
    abstract fun customConfigDao(): CustomConfigDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: VpnDatabase? = null

        fun getDatabase(context: Context): VpnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VpnDatabase::class.java,
                    "mutiganda_yvan_vpn.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: VpnDatabase) {
            // Seed default servers matching the user's screenshots & requirements
            val initialServers = listOf(
                VpnServerEntity(
                    id = "ovpn_fr_1",
                    name = "OVPN PREMIUM SERVER 1",
                    countryCode = "FR",
                    countryName = "France",
                    flagEmoji = "🇫🇷",
                    host = "fr1.mutigandavpn.com",
                    port = 1194,
                    protocol = "OVPN",
                    isVipOnly = false,
                    pingMs = 45,
                    isActive = true,
                    description = "Stable | Fast | Universal",
                    configData = "client\ndev tun\nproto udp\nremote fr1.mutigandavpn.com 1194\nresolv-retry infinite\nnobind\npersist-key\npersist-tun"
                ),
                VpnServerEntity(
                    id = "ovpn_fr_2",
                    name = "OVPN PREMIUM SERVER 2",
                    countryCode = "FR",
                    countryName = "France",
                    flagEmoji = "🇫🇷",
                    host = "fr2.mutigandavpn.com",
                    port = 1194,
                    protocol = "OVPN",
                    isVipOnly = false,
                    pingMs = 52,
                    isActive = true,
                    description = "Stable | Fast | Universal",
                    configData = "client\ndev tun\nproto udp\nremote fr2.mutigandavpn.com 1194\nresolv-retry infinite\nnobind\npersist-key\npersist-tun"
                ),
                VpnServerEntity(
                    id = "ssh_us_1",
                    name = "PREMIUM SERVER 1",
                    countryCode = "US",
                    countryName = "United States",
                    flagEmoji = "🇺🇸",
                    host = "us1.mutigandavpn.com",
                    port = 443,
                    protocol = "SSH",
                    isVipOnly = false,
                    pingMs = 118,
                    isActive = true,
                    description = "Stable | Fast | Universal",
                    configData = "ssh://us1.mutigandavpn.com:443"
                ),
                VpnServerEntity(
                    id = "ssh_us_2",
                    name = "PREMIUM SERVER 2",
                    countryCode = "US",
                    countryName = "United States",
                    flagEmoji = "🇺🇸",
                    host = "us2.mutigandavpn.com",
                    port = 443,
                    protocol = "SSH",
                    isVipOnly = true,
                    pingMs = 124,
                    isActive = true,
                    description = "VIP Ultra Speed | Gaming",
                    configData = "ssh://us2.mutigandavpn.com:443"
                ),
                VpnServerEntity(
                    id = "wg_de_1",
                    name = "WIREGUARD ULTRA VIP",
                    countryCode = "DE",
                    countryName = "Germany",
                    flagEmoji = "🇩🇪",
                    host = "de.mutigandavpn.com",
                    port = 51820,
                    protocol = "WIREGUARD",
                    isVipOnly = true,
                    pingMs = 62,
                    isActive = true,
                    description = "Kernel Tunnel | Low Latency",
                    configData = "[Interface]\nAddress = 10.14.0.2/24\nDNS = 1.1.1.1\n[Peer]\nPublicKey = yvanVpnServerPublicKey=\nEndpoint = de.mutigandavpn.com:51820\nAllowedIPs = 0.0.0.0/0"
                ),
                VpnServerEntity(
                    id = "v2ray_sg_1",
                    name = "V2RAY XRAY CLOUD",
                    countryCode = "SG",
                    countryName = "Singapore",
                    flagEmoji = "🇸🇬",
                    host = "sg.mutigandavpn.com",
                    port = 443,
                    protocol = "V2RAY",
                    isVipOnly = true,
                    pingMs = 89,
                    isActive = true,
                    description = "WebSocket CDN | Anti-Block",
                    configData = "vmess://eyJhZGQiOiJzZy5tdXRpZ2FuZGF2cG4uY29tIiwicG9ydCI6NDQzLCJpZCI6ImYyZWM3YmQ2LTY4YTYtNGU3My04OTBiLTRiMzYzODQyN2UzYyIsImFpZCI6MCwibmV0Ijoid3MiLCJwYXRoIjoiL3ZwbndzIiwidGxzIjoidGxzIn0="
                ),
                VpnServerEntity(
                    id = "rw_momo_lowping",
                    name = "RWANDA MTN FAST NODE",
                    countryCode = "RW",
                    countryName = "Rwanda",
                    flagEmoji = "🇷🇼",
                    host = "rw1.mutigandavpn.com",
                    port = 8080,
                    protocol = "SSH",
                    isVipOnly = false,
                    pingMs = 16,
                    isActive = true,
                    description = "Local Gateway | Kigali Fiber",
                    configData = "ssh://rw1.mutigandavpn.com:8080"
                )
            )
            db.serverDao().insertServers(initialServers)

            // Seed user account
            val hwid = "HWID-" + (100000..999999).random().toString()
            db.accountDao().saveAccount(
                UserAccountEntity(
                    hwid = hwid,
                    username = "Yvan Client",
                    phoneNumber = "0780949484",
                    vipTier = "FREE",
                    vipExpiresAt = 0L,
                    freeRemainingSeconds = 43200L, // 12 hours initial free time
                    totalUploadBytes = 1024L * 1024L * 24L,
                    totalDownloadBytes = 1024L * 1024L * 185L,
                    isAdmin = false
                )
            )

            // Seed default connection logs
            db.logDao().insertLog(LogEntity(level = "SUCCESS", message = "MUTIGANDA YVAN VPN Core Initialized v1.3.35"))
            db.logDao().insertLog(LogEntity(level = "INFO", message = "Developer & Owner: Yvan Mutiganda"))
            db.logDao().insertLog(LogEntity(level = "INFO", message = "Network Carrier: MTN Rwanda [RW] detected"))
            db.logDao().insertLog(LogEntity(level = "INFO", message = "Ready to start secure tunnel."))
        }
    }
}
