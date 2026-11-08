package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_servers")
data class VpnServerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val countryCode: String, // e.g. "FR", "US", "DE", "SG", "RW"
    val countryName: String,
    val flagEmoji: String,
    val host: String,
    val port: Int,
    val protocol: String, // "OVPN", "WIREGUARD", "SSH", "V2RAY", "HYSTERIA", "SLOWDNS"
    val isVipOnly: Boolean,
    val pingMs: Int,
    val isActive: Boolean,
    val description: String = "Stable | Fast | Universal",
    val configData: String = "" // Profile / Ovpn / Wireguard config string
)

@Entity(tableName = "payment_records")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val txId: String,
    val amountRwf: Long,
    val phoneNumber: String,
    val packageId: String,
    val packageName: String,
    val durationHours: Long,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val submittedAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey
    val hwid: String,
    val username: String = "Client",
    val phoneNumber: String = "0780949484",
    val vipTier: String = "FREE", // "FREE", "VIP", "PREMIUM"
    val vipExpiresAt: Long = 0L, // Timestamp in milliseconds (0 = expired or free)
    val freeRemainingSeconds: Long = 4200L, // Free trial / ad rewarded minutes
    val totalUploadBytes: Long = 0L,
    val totalDownloadBytes: Long = 0L,
    val isAdmin: Boolean = false
)

@Entity(tableName = "custom_configs")
data class CustomConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: String = "SSH",
    val payloadType: String = "SSL_DIRECT",
    val host: String = "",
    val port: Int = 443,
    val sni: String = "",
    val payload: String = "",
    val username: String = "",
    val password: String = "",
    val isSelected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "connection_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO", // "INFO", "WARN", "ERROR", "SUCCESS"
    val message: String
)
