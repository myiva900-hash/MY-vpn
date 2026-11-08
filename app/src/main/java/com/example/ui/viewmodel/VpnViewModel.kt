package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.CustomConfigEntity
import com.example.data.local.entities.LogEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import com.example.data.model.NetworkInfo
import com.example.data.model.PayloadType
import com.example.data.model.VipPackage
import com.example.data.model.VipPackages
import com.example.data.model.VipTier
import com.example.data.model.VpnStatus
import com.example.data.repository.VpnRepository
import com.example.vpn.MutigandaVpnService
import com.example.vpn.VpnConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME,
    LOGS,
    SERVERS,
    CUSTOM_SETUP,
    VIP_SUBSCRIPTION,
    PAYMENT_HISTORY,
    ADMIN_PANEL,
    WHATSAPP_SUPPORT,
    SETTINGS,
    ABOUT
}

class VpnViewModel(
    private val repository: VpnRepository
) : ViewModel() {

    val vpnConnectionState: StateFlow<VpnConnectionState> = MutigandaVpnService.vpnState

    val servers: StateFlow<List<VpnServerEntity>> = repository.allServers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val userAccount: StateFlow<UserAccountEntity?> = repository.userAccount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val connectionLogs: StateFlow<List<LogEntity>> = repository.connectionLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val customConfigs: StateFlow<List<CustomConfigEntity>> = repository.customConfigs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _selectedServer = MutableStateFlow<VpnServerEntity?>(null)
    val selectedServer: StateFlow<VpnServerEntity?> = _selectedServer.asStateFlow()

    private val _customSetupEnabled = MutableStateFlow(false)
    val customSetupEnabled: StateFlow<Boolean> = _customSetupEnabled.asStateFlow()

    private val _selectedPayloadType = MutableStateFlow(PayloadType.SSL_DIRECT)
    val selectedPayloadType: StateFlow<PayloadType> = _selectedPayloadType.asStateFlow()

    private val _customSni = MutableStateFlow("mtn.co.rw")
    val customSni: StateFlow<String> = _customSni.asStateFlow()

    private val _customPayload = MutableStateFlow("GET / HTTP/1.1[crlf]Host: [host][crlf]Upgrade: websocket[crlf][crlf]")
    val customPayload: StateFlow<String> = _customPayload.asStateFlow()

    private val _networkInfo = MutableStateFlow(NetworkInfo())
    val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    private val _adminUnlocked = MutableStateFlow(false)
    val adminUnlocked: StateFlow<Boolean> = _adminUnlocked.asStateFlow()

    private val _systemAnnouncement = MutableStateFlow<String?>(null)
    val systemAnnouncement: StateFlow<String?> = _systemAnnouncement.asStateFlow()

    // Countdown timer for VIP or Free trial
    private val _remainingTimeFormatted = MutableStateFlow("12h:00m:00s")
    val remainingTimeFormatted: StateFlow<String> = _remainingTimeFormatted.asStateFlow()

    init {
        // Auto-select first active server if none selected
        viewModelScope.launch {
            servers.collect { list ->
                if (_selectedServer.value == null && list.isNotEmpty()) {
                    _selectedServer.value = list.firstOrNull { it.isActive } ?: list.first()
                }
            }
        }

        // Start countdown ticker for VIP / Free time
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateTimeCountdown()
            }
        }
    }

    private fun updateTimeCountdown() {
        val account = userAccount.value ?: return
        val now = System.currentTimeMillis()
        if (account.vipExpiresAt > now) {
            val diffSec = (account.vipExpiresAt - now) / 1000
            val days = diffSec / 86400
            val hours = (diffSec % 86400) / 3600
            val mins = (diffSec % 3600) / 60
            val secs = diffSec % 60
            _remainingTimeFormatted.value = if (days > 0) {
                "${days}d ${hours}h:${mins}m"
            } else {
                String.format("%02dh:%02dm:%02ds", hours, mins, secs)
            }
        } else {
            val freeSec = account.freeRemainingSeconds
            val hours = freeSec / 3600
            val mins = (freeSec % 3600) / 60
            val secs = freeSec % 60
            _remainingTimeFormatted.value = String.format("%02dh:%02dm:%02ds", hours, mins, secs)
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun selectServer(server: VpnServerEntity) {
        _selectedServer.value = server
        viewModelScope.launch {
            repository.addLog("INFO", "Selected Server: ${server.name} (${server.countryName}) - Ping: ${server.pingMs}ms")
        }
    }

    fun toggleCustomSetup(enabled: Boolean) {
        _customSetupEnabled.value = enabled
        viewModelScope.launch {
            repository.addLog("INFO", "Custom Setup ${if (enabled) "ENABLED" else "DISABLED"}")
        }
    }

    fun setPayloadType(type: PayloadType) {
        _selectedPayloadType.value = type
        viewModelScope.launch {
            repository.addLog("INFO", "Selected Protocol: ${type.label}")
        }
    }

    fun setCustomSni(sni: String) {
        _customSni.value = sni
    }

    fun setCustomPayload(payload: String) {
        _customPayload.value = payload
    }

    fun toggleVpnConnection(context: Context, onPrepareVpn: () -> Unit) {
        val currentState = vpnConnectionState.value.status
        if (currentState == VpnStatus.CONNECTED || currentState == VpnStatus.CONNECTING) {
            // Disconnect
            val intent = Intent(context, MutigandaVpnService::class.java).apply {
                action = MutigandaVpnService.ACTION_DISCONNECT
            }
            context.startService(intent)
        } else {
            // Check VIP server restriction
            val server = _selectedServer.value
            val account = userAccount.value
            val isVipActive = (account?.vipExpiresAt ?: 0L) > System.currentTimeMillis()

            if (server != null && server.isVipOnly && !isVipActive) {
                Toast.makeText(context, "This server requires VIP membership. Please upgrade!", Toast.LENGTH_LONG).show()
                _currentScreen.value = ScreenDestination.VIP_SUBSCRIPTION
                return
            }

            // Check if VPN permission prepared
            onPrepareVpn()
        }
    }

    fun startVpnService(context: Context) {
        val server = _selectedServer.value ?: return
        val intent = Intent(context, MutigandaVpnService::class.java).apply {
            action = MutigandaVpnService.ACTION_CONNECT
            putExtra(MutigandaVpnService.EXTRA_SERVER_ID, server.id)
            putExtra(MutigandaVpnService.EXTRA_SERVER_NAME, server.name)
            putExtra(MutigandaVpnService.EXTRA_SERVER_HOST, server.host)
            putExtra(MutigandaVpnService.EXTRA_SERVER_PORT, server.port)
            putExtra(MutigandaVpnService.EXTRA_PROTOCOL, server.protocol)
            putExtra(MutigandaVpnService.EXTRA_FLAG_EMOJI, server.flagEmoji)
            putExtra(MutigandaVpnService.EXTRA_PAYLOAD_TYPE, _selectedPayloadType.value.name)
            putExtra(MutigandaVpnService.EXTRA_CUSTOM_SNI, _customSni.value)
        }
        context.startService(intent)
    }

    fun addFreeHours(context: Context, hours: Int = 4) {
        viewModelScope.launch {
            repository.addFreeHours(hours)
            Toast.makeText(context, "+$hours Hours Added to Trial Balance!", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitPayment(
        context: Context,
        txId: String,
        amount: Long,
        phone: String,
        selectedPackage: VipPackage,
        notes: String
    ) {
        if (txId.isBlank()) {
            Toast.makeText(context, "Please enter the MTN Mobile Money Transaction ID", Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isBlank()) {
            Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            repository.submitPayment(
                txId = txId,
                amountRwf = amount,
                phoneNumber = phone,
                packageId = selectedPackage.id,
                packageName = selectedPackage.name,
                durationHours = selectedPackage.durationHours,
                notes = notes
            )
            Toast.makeText(
                context,
                "Payment submitted! Administrator Yvan Mutiganda will verify and activate your VIP shortly.",
                Toast.LENGTH_LONG
            ).show()
            _currentScreen.value = ScreenDestination.PAYMENT_HISTORY
        }
    }

    fun verifyAdminPin(pin: String): Boolean {
        // Secure admin gate for Yvan Mutiganda (e.g. 780949484 or 2238763)
        return if (pin == "780949484" || pin == "2238763" || pin == "admin2026") {
            _adminUnlocked.value = true
            viewModelScope.launch {
                repository.toggleAdminMode(true)
            }
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _adminUnlocked.value = false
        viewModelScope.launch {
            repository.toggleAdminMode(false)
        }
    }

    fun approvePayment(paymentId: Long, durationHours: Long) {
        viewModelScope.launch {
            repository.approvePayment(paymentId, durationHours)
        }
    }

    fun rejectPayment(paymentId: Long, reason: String) {
        viewModelScope.launch {
            repository.rejectPayment(paymentId, reason)
        }
    }

    fun addServer(
        name: String,
        countryCode: String,
        countryName: String,
        flagEmoji: String,
        host: String,
        port: Int,
        protocol: String,
        isVipOnly: Boolean
    ) {
        viewModelScope.launch {
            val server = VpnServerEntity(
                id = "server_" + System.currentTimeMillis(),
                name = name,
                countryCode = countryCode,
                countryName = countryName,
                flagEmoji = flagEmoji,
                host = host,
                port = port,
                protocol = protocol,
                isVipOnly = isVipOnly,
                pingMs = (25..95).random(),
                isActive = true,
                description = if (isVipOnly) "VIP Dedicated Node" else "Public Standard Node"
            )
            repository.addServer(server)
        }
    }

    fun toggleServerStatus(id: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.setServerStatus(id, isActive)
        }
    }

    fun deleteServer(id: String) {
        viewModelScope.launch {
            repository.deleteServer(id)
        }
    }

    fun broadcastAnnouncement(message: String) {
        _systemAnnouncement.value = message
        viewModelScope.launch {
            repository.addLog("WARN", "[BROADCAST]: $message")
        }
    }

    fun clearAnnouncement() {
        _systemAnnouncement.value = null
    }

    fun openWhatsAppSupport(context: Context, topic: String) {
        val phone = "+250787699523"
        val hwid = userAccount.value?.hwid ?: "Unknown"
        val message = "Hello Yvan Mutiganda, I need assistance with MUTIGANDA YVAN VPN.\n\n" +
                "Topic: $topic\n" +
                "Device HWID: $hwid\n" +
                "Network: ${_networkInfo.value.carrierName}"

        try {
            val url = "https://api.whatsapp.com/send?phone=250787699523&text=" + Uri.encode(message)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed. Contact +250787699523 directly.", Toast.LENGTH_LONG).show()
        }
    }

    fun openTelegram(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mutigandavpn"))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Telegram channel: @mutigandavpn", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.addLog("INFO", "Connection logs cleared.")
        }
    }
}
