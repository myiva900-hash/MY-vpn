package com.example.data.repository

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
import com.example.data.model.PaymentStatus
import com.example.data.model.VipTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class VpnRepository(
    private val serverDao: ServerDao,
    private val paymentDao: PaymentDao,
    private val accountDao: AccountDao,
    private val customConfigDao: CustomConfigDao,
    private val logDao: LogDao
) {
    val allServers: Flow<List<VpnServerEntity>> = serverDao.getAllServers()
    val activeServers: Flow<List<VpnServerEntity>> = serverDao.getActiveServers()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val userAccount: Flow<UserAccountEntity?> = accountDao.getAccountFlow()
    val customConfigs: Flow<List<CustomConfigEntity>> = customConfigDao.getAllConfigs()
    val connectionLogs: Flow<List<LogEntity>> = logDao.getLatestLogs()

    suspend fun getServerById(id: String): VpnServerEntity? = serverDao.getServerById(id)

    suspend fun addServer(server: VpnServerEntity) {
        serverDao.insertServer(server)
        addLog("INFO", "Admin added server: ${server.name} (${server.protocol})")
    }

    suspend fun updateServer(server: VpnServerEntity) {
        serverDao.updateServer(server)
        addLog("INFO", "Admin updated server: ${server.name}")
    }

    suspend fun setServerStatus(id: String, isActive: Boolean) {
        serverDao.setServerStatus(id, isActive)
        addLog("INFO", "Server $id status changed to: ${if (isActive) "ACTIVE" else "DISABLED"}")
    }

    suspend fun deleteServer(id: String) {
        serverDao.deleteServer(id)
        addLog("WARN", "Server $id deleted by administrator")
    }

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
            addLog("SUCCESS", "Payment #$paymentId APPROVED! VIP activated until ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(newExpiresAt))}")
        }
    }

    suspend fun rejectPayment(paymentId: Long, reason: String = "") {
        paymentDao.updatePaymentStatus(paymentId, PaymentStatus.REJECTED.name)
        addLog("WARN", "Payment #$paymentId rejected by administrator. $reason")
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
