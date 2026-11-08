package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CustomConfigEntity
import com.example.data.local.entities.LogEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM vpn_servers ORDER BY isVipOnly ASC, pingMs ASC")
    fun getAllServers(): Flow<List<VpnServerEntity>>

    @Query("SELECT * FROM vpn_servers WHERE isActive = 1 ORDER BY pingMs ASC")
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

    @Query("DELETE FROM vpn_servers WHERE id = :id")
    suspend fun deleteServer(id: String)
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
