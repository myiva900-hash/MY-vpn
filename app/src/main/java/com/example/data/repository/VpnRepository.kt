package com.example.data.repository

import com.example.crypto.CryptoManager
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
import com.example.data.model.PaymentStatus
import com.example.data.model.VipTier
import com.example.vpn.OvpnParser
import com.example.vpn.VpsHealthMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VpnRepository(
    private val serverDao: ServerDao,
    private val ovpnConfigDao: OvpnConfigDao,
    private val paymentDao: PaymentDao,
    private val accountDao: AccountDao,
    private val customConfigDao: CustomConfigDao,
    private val logDao: LogDao,
    private val auditLogDao: AuditLogDao,
    private val supportMessageDao: SupportMessageDao
) {
    val allServers: Flow<List<VpnServerEntity>> = serverDao.getAllServers()
    val activeServers: Flow<List<VpnServerEntity>> = serverDao.getActiveServers()
    val allOvpnConfigs: Flow<List<OvpnConfigEntity>> = ovpnConfigDao.getAllConfigs()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val userAccount: Flow<UserAccountEntity?> = accountDao.getAccountFlow()
    val customConfigs: Flow<List<CustomConfigEntity>> = customConfigDao.getAllConfigs()
    val connectionLogs: Flow<List<LogEntity>> = logDao.getLatestLogs()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()
    val allSupportMessages: Flow<List<SupportMessageEntity>> = supportMessageDao.getAllMessages()

    suspend fun getServerById(id: String): VpnServerEntity? = serverDao.getServerById(id)

    suspend fun addServer(server: VpnServerEntity) {
        serverDao.insertServer(server)
        addLog("INFO", "Admin added VPS server: ${server.name} (${server.host}:${server.port})")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "ADD_VPS_SERVER",
                target = server.id,
                details = "Server ${server.name} added. Host: ${server.host}, Protocol: ${server.protocol} (${server.transport})"
            )
        )
    }

    suspend fun updateServer(server: VpnServerEntity) {
        serverDao.updateServer(server)
        addLog("INFO", "Admin updated VPS server: ${server.name}")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "UPDATE_VPS_SERVER",
                target = server.id,
                details = "Server ${server.name} configuration updated."
            )
        )
    }

    suspend fun setServerStatus(id: String, isActive: Boolean) {
        serverDao.setServerStatus(id, isActive)
        addLog("INFO", "VPS Server $id status changed to: ${if (isActive) "ACTIVE" else "DISABLED"}")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "TOGGLE_SERVER_STATUS",
                target = id,
                details = "Status changed to ${if (isActive) "ACTIVE" else "DISABLED"}"
            )
        )
    }

    suspend fun deleteServer(id: String) {
        serverDao.deleteServer(id)
        addLog("WARN", "VPS Server $id deleted by administrator")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "DELETE_VPS_SERVER",
                target = id,
                details = "Server removed from active fleet."
            )
        )
    }

    /**
     * Executes real socket health probe against an authorized VPS server.
     */
    suspend fun probeServerHealth(server: VpnServerEntity) {
        val result = VpsHealthMonitor.checkServerHealth(server.id, server.host, server.port)
        val status = if (result.isOnline) "ONLINE" else "OFFLINE"
        serverDao.updateServerHealth(
            id = server.id,
            status = status,
            pingMs = result.latencyMs,
            capacity = result.capacityPercent
        )
        val msg = "VPS Probe [${server.name}]: $status (${result.latencyMs}ms, Load: ${result.capacityPercent}%)"
        addLog(if (result.isOnline) "SUCCESS" else "WARN", msg)
    }

    /**
     * Probes all active servers in parallel.
     */
    suspend fun probeAllActiveServers() {
        val list = serverDao.getActiveServers().firstOrNull() ?: emptyList()
        for (server in list) {
            probeServerHealth(server)
        }
    }

    // ==========================================
    // OVPN & ENCRYPTED CONFIGURATION MANAGEMENT
    // ==========================================

    suspend fun saveOvpnConfig(name: String, rawContent: String, isEncrypted: Boolean = false): Long {
        val parsed = OvpnParser.parse(rawContent)
        val config = OvpnConfigEntity(
            name = name.ifBlank { "OVPN Profile (${parsed.remoteHost})" },
            remoteHost = parsed.remoteHost,
            remotePort = parsed.remotePort,
            transport = parsed.protocol,
            rawConfig = rawContent,
            isEncrypted = isEncrypted,
            isSelected = false
        )
        val id = ovpnConfigDao.insertConfig(config)
        addLog("SUCCESS", "Stored valid OpenVPN profile for ${parsed.remoteHost}:${parsed.remotePort}")
        return id
    }

    suspend fun selectActiveOvpnConfig(id: Long) {
        ovpnConfigDao.selectActiveConfig(id)
        addLog("INFO", "Active OpenVPN profile switched to ID: $id")
    }

    suspend fun deleteOvpnConfig(id: Long) {
        ovpnConfigDao.deleteConfigById(id)
        addLog("INFO", "Deleted OpenVPN profile ID: $id")
    }

    /**
     * Imports configuration file bytes (.ovpn or password-protected .myvpn).
     */
    suspend fun importConfigFile(
        filename: String,
        fileBytes: ByteArray,
        password: String = ""
    ): Pair<Boolean, String> {
        return try {
            val decryptedContent = if (CryptoManager.isMyVpnEncrypted(fileBytes)) {
                if (password.isBlank()) {
                    return Pair(false, "PASSWORD_REQUIRED")
                }
                CryptoManager.decryptMyVpnProfile(fileBytes, password)
            } else {
                String(fileBytes, Charsets.UTF_8)
            }

            // Validate configuration through OpenVPN parser
            val parsed = OvpnParser.parse(decryptedContent)
            val name = filename.substringBeforeLast(".")
            saveOvpnConfig(name, decryptedContent, isEncrypted = CryptoManager.isMyVpnEncrypted(fileBytes))
            Pair(true, "Successfully imported valid configuration for ${parsed.remoteHost}:${parsed.remotePort}")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to import configuration file.")
        }
    }

    /**
     * Exports a configuration as an AES-256-GCM + PBKDF2 password-protected .myvpn package.
     */
    fun exportEncryptedMyVpn(configContent: String, password: String): ByteArray {
        return CryptoManager.encryptMyVpnProfile(configContent, password)
    }

    // ==========================================
    // PAYMENT & VIP SUBSCRIPTIONS
    // ==========================================

    suspend fun submitPayment(
        txId: String,
        amountRwf: Long,
        phoneNumber: String,
        packageId: String,
        packageName: String,
        durationHours: Long,
        notes: String
    ): Long {
        val payment = PaymentEntity(
            txId = txId.trim(),
            amountRwf = amountRwf,
            phoneNumber = phoneNumber.trim(),
            packageId = packageId,
            packageName = packageName,
            durationHours = durationHours,
            status = PaymentStatus.PENDING.name,
            submittedAt = System.currentTimeMillis(),
            notes = notes.trim()
        )
        val id = paymentDao.insertPayment(payment)
        addLog("INFO", "VIP Payment submitted: TxID $txId for $packageName ($amountRwf RWF) - Pending Admin Verification")
        return id
    }

    suspend fun approvePayment(paymentId: Long, durationHours: Long) {
        paymentDao.updatePaymentStatus(paymentId, PaymentStatus.APPROVED.name)
        val account = accountDao.getAccount()
        if (account != null) {
            val now = System.currentTimeMillis()
            val currentExpire = if (account.vipExpiresAt > now) account.vipExpiresAt else now
            val newExpiresAt = currentExpire + (durationHours * 3600 * 1000L)
            accountDao.updateVipStatus(account.hwid, VipTier.VIP.name, newExpiresAt)
            addLog("SUCCESS", "Payment #$paymentId APPROVED! VIP activated until ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(newExpiresAt))}")
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    action = "APPROVE_PAYMENT",
                    target = "PAYMENT_$paymentId",
                    details = "Approved VIP payment. Credited $durationHours hours to ${account.hwid}."
                )
            )
        }
    }

    suspend fun rejectPayment(paymentId: Long, reason: String = "") {
        paymentDao.updatePaymentStatus(paymentId, PaymentStatus.REJECTED.name)
        addLog("WARN", "Payment #$paymentId rejected by administrator. $reason")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "REJECT_PAYMENT",
                target = "PAYMENT_$paymentId",
                details = "Payment rejected. Reason: $reason"
            )
        )
    }

    suspend fun addFreeHours(hours: Int) {
        val account = accountDao.getAccount()
        if (account != null) {
            val newTime = account.freeRemainingSeconds + (hours * 3600L)
            accountDao.updateFreeTime(account.hwid, newTime)
            addLog("SUCCESS", "Added $hours free trial hours to account balance!")
        }
    }

    suspend fun toggleAdminMode(enable: Boolean) {
        val account = accountDao.getAccount()
        if (account != null) {
            accountDao.saveAccount(account.copy(isAdmin = enable))
            addLog("INFO", "Administrator mode ${if (enable) "ENABLED" else "DISABLED"}")
        }
    }

    suspend fun addCustomConfig(config: CustomConfigEntity): Long {
        val id = customConfigDao.insertConfig(config)
        addLog("INFO", "Saved custom configuration: ${config.name}")
        return id
    }

    suspend fun deleteCustomConfig(config: CustomConfigEntity) {
        customConfigDao.deleteConfig(config)
        addLog("INFO", "Removed custom configuration: ${config.name}")
    }

    suspend fun addLog(level: String, message: String) {
        logDao.insertLog(LogEntity(level = level, message = message))
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }

    suspend fun recordTraffic(uploadBytes: Long, downloadBytes: Long) {
        val account = accountDao.getAccount()
        if (account != null) {
            accountDao.incrementTraffic(account.hwid, uploadBytes, downloadBytes)
        }
    }
}
