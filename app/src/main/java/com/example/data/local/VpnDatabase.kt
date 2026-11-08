package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CustomConfigDao
import com.example.data.local.dao.LogDao
import com.example.data.local.dao.OvpnConfigDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ServerDao
import com.example.data.local.dao.SupportMessageDao
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.CustomConfigEntity
import com.example.data.local.entities.LogEntity
import com.example.data.local.entities.OvpnConfigEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.SupportMessageEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VpnServerEntity::class,
        OvpnConfigEntity::class,
        PaymentEntity::class,
        UserAccountEntity::class,
        CustomConfigEntity::class,
        LogEntity::class,
        AuditLogEntity::class,
        SupportMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun ovpnConfigDao(): OvpnConfigDao
    abstract fun paymentDao(): PaymentDao
    abstract fun accountDao(): AccountDao
    abstract fun customConfigDao(): CustomConfigDao
    abstract fun logDao(): LogDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun supportMessageDao(): SupportMessageDao

    companion object {
        @Volatile
        private var INSTANCE: VpnDatabase? = null

        fun getDatabase(context: Context): VpnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VpnDatabase::class.java,
                    "mutiganda_yvan_vpn.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: VpnDatabase) {
            // Seed multi-VPS server fleet with real OpenVPN configurations
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
                    transport = "UDP",
                    isVipOnly = false,
                    pingMs = 45,
                    isActive = true,
                    capacityPercent = 38,
                    healthStatus = "ONLINE",
                    priority = 1,
                    description = "Stable | Fast | Universal",
                    configData = "client\ndev tun\nproto udp\nremote fr1.mutigandavpn.com 1194\nresolv-retry infinite\nnobind\npersist-key\npersist-tun\ncipher AES-256-GCM\nverb 3"
                ),
                VpnServerEntity(
                    id = "ovpn_fr_2",
                    name = "OVPN PREMIUM SERVER 2",
                    countryCode = "FR",
                    countryName = "France",
                    flagEmoji = "🇫🇷",
                    host = "fr2.mutigandavpn.com",
                    port = 443,
                    protocol = "OVPN",
                    transport = "TCP",
                    isVipOnly = false,
                    pingMs = 52,
                    isActive = true,
                    capacityPercent = 42,
                    healthStatus = "ONLINE",
                    priority = 2,
                    description = "TCP Tunnel | Firewall Bypass",
                    configData = "client\ndev tun\nproto tcp\nremote fr2.mutigandavpn.com 443\nresolv-retry infinite\nnobind\npersist-key\npersist-tun\ncipher AES-256-GCM\nverb 3"
                ),
                VpnServerEntity(
                    id = "ovpn_us_1",
                    name = "OVPN US ULTRA FAST",
                    countryCode = "US",
                    countryName = "United States",
                    flagEmoji = "🇺🇸",
                    host = "us1.mutigandavpn.com",
                    port = 1194,
                    protocol = "OVPN",
                    transport = "UDP",
                    isVipOnly = false,
                    pingMs = 115,
                    isActive = true,
                    capacityPercent = 55,
                    healthStatus = "ONLINE",
                    priority = 3,
                    description = "Streaming & Gaming Node",
                    configData = "client\ndev tun\nproto udp\nremote us1.mutigandavpn.com 1194\nresolv-retry infinite\nnobind\npersist-key\npersist-tun\ncipher AES-256-GCM\nverb 3"
                ),
                VpnServerEntity(
                    id = "ovpn_de_vip",
                    name = "OVPN GERMANY VIP DEDICATED",
                    countryCode = "DE",
                    countryName = "Germany",
                    flagEmoji = "🇩🇪",
                    host = "de1.mutigandavpn.com",
                    port = 1194,
                    protocol = "OVPN",
                    transport = "UDP",
                    isVipOnly = true,
                    pingMs = 58,
                    isActive = true,
                    capacityPercent = 18,
                    healthStatus = "ONLINE",
                    priority = 1,
                    description = "VIP Gigabit Fiber | Low Ping",
                    configData = "client\ndev tun\nproto udp\nremote de1.mutigandavpn.com 1194\nresolv-retry infinite\nnobind\npersist-key\npersist-tun\ncipher AES-256-GCM\nverb 3"
                ),
                VpnServerEntity(
                    id = "ovpn_rw_momo",
                    name = "RWANDA MTN FAST NODE",
                    countryCode = "RW",
                    countryName = "Rwanda",
                    flagEmoji = "🇷🇼",
                    host = "rw1.mutigandavpn.com",
                    port = 8080,
                    protocol = "OVPN",
                    transport = "TCP",
                    isVipOnly = false,
                    pingMs = 14,
                    isActive = true,
                    capacityPercent = 29,
                    healthStatus = "ONLINE",
                    priority = 1,
                    description = "Local Kigali Gateway | Low Latency",
                    configData = "client\ndev tun\nproto tcp\nremote rw1.mutigandavpn.com 8080\nresolv-retry infinite\nnobind\npersist-key\npersist-tun\ncipher AES-256-GCM\nverb 3"
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
                    freeRemainingSeconds = 43200L,
                    totalUploadBytes = 1024L * 1024L * 28L,
                    totalDownloadBytes = 1024L * 1024L * 215L,
                    isAdmin = false
                )
            )

            // Seed default OVPN configs
            val defaultOvpn = """
                client
                dev tun
                proto udp
                remote fr1.mutigandavpn.com 1194
                resolv-retry infinite
                nobind
                persist-key
                persist-tun
                remote-cert-tls server
                cipher AES-256-GCM
                verb 3
            """.trimIndent()

            db.ovpnConfigDao().insertConfig(
                OvpnConfigEntity(
                    name = "France Premium OVPN (Default)",
                    remoteHost = "fr1.mutigandavpn.com",
                    remotePort = 1194,
                    transport = "UDP",
                    rawConfig = defaultOvpn,
                    isEncrypted = false,
                    isSelected = true
                )
            )

            // Seed initial connection logs & audit log
            db.logDao().insertLog(LogEntity(level = "SUCCESS", message = "MUTIGANDA YVAN VPN Multi-VPS Core Initialized v1.3.35"))
            db.logDao().insertLog(LogEntity(level = "INFO", message = "Multi-VPS Engine ready: 5 authorized OpenVPN nodes loaded"))
            db.auditLogDao().insertAuditLog(
                AuditLogEntity(
                    action = "SYSTEM_INITIALIZE",
                    target = "MUTIGANDA_YVAN_VPN_FLEET",
                    details = "Multi-VPS fleet initialized with 5 nodes. OpenVPN primary engine active."
                )
            )
        }
    }
}
