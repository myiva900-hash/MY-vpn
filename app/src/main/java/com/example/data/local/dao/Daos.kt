package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.CustomConfigEntity
import com.example.data.local.entities.LogEntity
import com.example.data.local.entities.OvpnConfigEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.SupportMessageEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM vpn_servers ORDER BY priority ASC, pingMs ASC")
    fun getAllServers(): Flow<List<VpnServerEntity>>

    @Query("SELECT * FROM vpn_servers WHERE isActive = 1 ORDER BY priority ASC, pingMs ASC")
    fun getActiveServers(): Flow<List<VpnServerEntity>>

    @Query("SELECT * FROM vpn_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): VpnServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<VpnServerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: VpnServerEntity)

    @Update
    suspend fun updateServer(server: VpnServerEntity)

    @Query("UPDATE vpn_servers SET isActive = :isActive WHERE id = :id")
    suspend fun setServerStatus(id: String, isActive: Boolean)

    @Query("UPDATE vpn_servers SET healthStatus = :status, pingMs = :pingMs, capacityPercent = :capacity, lastCheckedAt = :time WHERE id = :id")
    suspend fun updateServerHealth(id: String, status: String, pingMs: Int, capacity: Int, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM vpn_servers WHERE id = :id")
    suspend fun deleteServer(id: String)
}

@Dao
interface OvpnConfigDao {
    @Query("SELECT * FROM ovpn_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<OvpnConfigEntity>>

    @Query("SELECT * FROM ovpn_configs WHERE id = :id LIMIT 1")
    suspend fun getConfigById(id: Long): OvpnConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: OvpnConfigEntity): Long

    @Query("UPDATE ovpn_configs SET isSelected = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun selectActiveConfig(id: Long)

    @Delete
    suspend fun deleteConfig(config: OvpnConfigEntity)

    @Query("DELETE FROM ovpn_configs WHERE id = :id")
    suspend fun deleteConfigById(id: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_records ORDER BY submittedAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("UPDATE payment_records SET status = :status, verifiedAt = :verifiedAt WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, status: String, verifiedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM payment_records WHERE id = :id")
    suspend fun deletePayment(id: Long)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM user_account LIMIT 1")
    fun getAccountFlow(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_account LIMIT 1")
    suspend fun getAccount(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAccount(account: UserAccountEntity)

    @Query("UPDATE user_account SET vipTier = :tier, vipExpiresAt = :expiresAt WHERE hwid = :hwid")
    suspend fun updateVipStatus(hwid: String, tier: String, expiresAt: Long)

    @Query("UPDATE user_account SET freeRemainingSeconds = :seconds WHERE hwid = :hwid")
    suspend fun updateFreeTime(hwid: String, seconds: Long)

    @Query("UPDATE user_account SET totalUploadBytes = totalUploadBytes + :upload, totalDownloadBytes = totalDownloadBytes + :download WHERE hwid = :hwid")
    suspend fun incrementTraffic(hwid: String, upload: Long, download: Long)
}

@Dao
interface CustomConfigDao {
    @Query("SELECT * FROM custom_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<CustomConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: CustomConfigEntity): Long

    @Delete
    suspend fun deleteConfig(config: CustomConfigEntity)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM connection_logs ORDER BY id DESC LIMIT 200")
    fun getLatestLogs(): Flow<List<LogEntity>>

    @Insert
    suspend fun insertLog(log: LogEntity)

    @Query("DELETE FROM connection_logs")
    suspend fun clearLogs()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface SupportMessageDao {
    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SupportMessageEntity>>

    @Insert
    suspend fun insertMessage(msg: SupportMessageEntity): Long

    @Query("UPDATE support_messages SET adminReply = :reply, status = 'ANSWERED' WHERE id = :id")
    suspend fun replyMessage(id: Long, reply: String)
}
