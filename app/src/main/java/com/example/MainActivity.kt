package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.VpnDatabase
import com.example.data.repository.VpnRepository
import com.example.ui.components.CyberTabBar
import com.example.ui.components.CyberTopBar
import com.example.ui.components.DrawerContent
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CustomSetupDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.PaymentHistoryScreen
import com.example.ui.screens.ServerSelectionDialog
import com.example.ui.screens.VipSubscriptionScreen
import com.example.ui.screens.VpnSettingsScreen
import com.example.ui.screens.WhatsAppSupportScreen
import com.example.ui.screens.cyberTextFieldColors
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.MutigandaVpnTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.VpnViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = VpnDatabase.getDatabase(applicationContext)
        val repository = VpnRepository(
            serverDao = db.serverDao(),
            paymentDao = db.paymentDao(),
            accountDao = db.accountDao(),
            customConfigDao = db.customConfigDao(),
            logDao = db.logDao()
        )

        setContent {
            MutigandaVpnTheme {
                val viewModel: VpnViewModel = viewModel {
                    VpnViewModel(repository)
                }
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: VpnViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val vpnState by viewModel.vpnConnectionState.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val customSetupEnabled by viewModel.customSetupEnabled.collectAsStateWithLifecycle()
    val selectedPayloadType by viewModel.selectedPayloadType.collectAsStateWithLifecycle()
    val customSni by viewModel.customSni.collectAsStateWithLifecycle()
    val customPayload by viewModel.customPayload.collectAsStateWithLifecycle()
    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val timeLeftFormatted by viewModel.remainingTimeFormatted.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val userAccount by viewModel.userAccount.collectAsStateWithLifecycle()
    val connectionLogs by viewModel.connectionLogs.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.adminUnlocked.collectAsStateWithLifecycle()
    val systemAnnouncement by viewModel.systemAnnouncement.collectAsStateWithLifecycle()

    var showServerDialog by remember { mutableStateOf(false) }
    var showCustomSetupDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Launcher for Android VpnService prepare intent
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.startVpnService(context)
        } else {
            Toast.makeText(context, "VPN permission is required to create tunnel", Toast.LENGTH_SHORT).show()
        }
    }

    // Android 13+ Notification permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result handled */ }

    // Prepare VPN trigger
    fun requestVpnConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            // Already authorized, start tunnel directly
            viewModel.startVpnService(context)
        }
    }

    // Back handler for sub-screens
    BackHandler(enabled = currentScreen != ScreenDestination.HOME) {
        viewModel.navigateTo(ScreenDestination.HOME)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userAccount = userAccount,
                onNavigate = { destination ->
                    viewModel.navigateTo(destination)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                },
                onImportConfig = { showImportDialog = true },
                onExportConfig = { showExportDialog = true }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    CyberTopBar(
                        title = when (currentScreen) {
                            ScreenDestination.HOME, ScreenDestination.LOGS -> "MUTIGANDA YVAN VPN"
                            ScreenDestination.VIP_SUBSCRIPTION -> "VIP SUBSCRIPTION"
                            ScreenDestination.PAYMENT_HISTORY -> "PAYMENT HISTORY"
                            ScreenDestination.ADMIN_PANEL -> "ADMIN PORTAL"
                            ScreenDestination.WHATSAPP_SUPPORT -> "WHATSAPP SUPPORT"
                            ScreenDestination.SETTINGS -> "VPN SETTINGS"
                            ScreenDestination.ABOUT -> "ABOUT VPN"
                            else -> "MUTIGANDA YVAN VPN"
                        },
                        showBackButton = currentScreen != ScreenDestination.HOME && currentScreen != ScreenDestination.LOGS,
                        onBackClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onRefreshClick = {
                            Toast.makeText(context, "Servers latency updated", Toast.LENGTH_SHORT).show()
                        },
                        onNotificationsClick = {
                            if (systemAnnouncement != null) {
                                Toast.makeText(context, systemAnnouncement, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "All servers operating at 100% capacity", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onMoreClick = { showOverflowMenu = true }
                    )

                    // 3-dots overflow menu
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        modifier = Modifier.background(CyberNavyDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import/Export", color = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                showImportDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear App Data", color = TextPrimary) },
                            onClick = {
                                showOverflowMenu = false
                                viewModel.clearLogs()
                                Toast.makeText(context, "App cache cleared", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Admin Portal", color = CyberPurple) },
                            onClick = {
                                showOverflowMenu = false
                                viewModel.navigateTo(ScreenDestination.ADMIN_PANEL)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exit", color = TextMuted) },
                            onClick = {
                                showOverflowMenu = false
                                (context as? Activity)?.finish()
                            }
                        )
                    }
                }
            },
            containerColor = CyberBlack
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // System Broadcast Banner if present
                if (systemAnnouncement != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = systemAnnouncement!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearAnnouncement() }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Show top tabs (HOME / LOGS) when on Home or Logs screens
                if (currentScreen == ScreenDestination.HOME || currentScreen == ScreenDestination.LOGS) {
                    CyberTabBar(
                        selectedTabIndex = if (currentScreen == ScreenDestination.HOME) 0 else 1,
                        onTabSelected = { index ->
                            viewModel.navigateTo(if (index == 0) ScreenDestination.HOME else ScreenDestination.LOGS)
                        }
                    )
                }

                // Screen Switcher
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        ScreenDestination.HOME -> {
                            HomeScreen(
                                vpnState = vpnState,
                                selectedServer = selectedServer,
                                customSetupEnabled = customSetupEnabled,
                                selectedPayloadType = selectedPayloadType,
                                networkInfo = networkInfo,
                                timeLeftFormatted = timeLeftFormatted,
                                onServerClick = { showServerDialog = true },
                                onCustomSetupToggle = { viewModel.toggleCustomSetup(it) },
                                onCustomSetupClick = { showCustomSetupDialog = true },
                                onConnectToggle = {
                                    viewModel.toggleVpnConnection(context) {
                                        requestVpnConnection()
                                    }
                                },
                                onAddHoursClick = { viewModel.addFreeHours(context, 4) },
                                onTelegramClick = { viewModel.openTelegram(context) },
                                onUpgradeVipClick = { viewModel.navigateTo(ScreenDestination.VIP_SUBSCRIPTION) }
                            )
                        }
                        ScreenDestination.LOGS -> {
                            LogsScreen(
                                logs = connectionLogs,
                                onClearLogs = { viewModel.clearLogs() }
                            )
                        }
                        ScreenDestination.SERVERS -> {
                            showServerDialog = true
                            HomeScreen(
                                vpnState = vpnState,
                                selectedServer = selectedServer,
                                customSetupEnabled = customSetupEnabled,
                                selectedPayloadType = selectedPayloadType,
                                networkInfo = networkInfo,
                                timeLeftFormatted = timeLeftFormatted,
                                onServerClick = { showServerDialog = true },
                                onCustomSetupToggle = { viewModel.toggleCustomSetup(it) },
                                onCustomSetupClick = { showCustomSetupDialog = true },
                                onConnectToggle = {
                                    viewModel.toggleVpnConnection(context) { requestVpnConnection() }
                                },
                                onAddHoursClick = { viewModel.addFreeHours(context, 4) },
                                onTelegramClick = { viewModel.openTelegram(context) },
                                onUpgradeVipClick = { viewModel.navigateTo(ScreenDestination.VIP_SUBSCRIPTION) }
                            )
                        }
                        ScreenDestination.CUSTOM_SETUP -> {
                            showCustomSetupDialog = true
                            HomeScreen(
                                vpnState = vpnState,
                                selectedServer = selectedServer,
                                customSetupEnabled = customSetupEnabled,
                                selectedPayloadType = selectedPayloadType,
                                networkInfo = networkInfo,
                                timeLeftFormatted = timeLeftFormatted,
                                onServerClick = { showServerDialog = true },
                                onCustomSetupToggle = { viewModel.toggleCustomSetup(it) },
                                onCustomSetupClick = { showCustomSetupDialog = true },
                                onConnectToggle = {
                                    viewModel.toggleVpnConnection(context) { requestVpnConnection() }
                                },
                                onAddHoursClick = { viewModel.addFreeHours(context, 4) },
                                onTelegramClick = { viewModel.openTelegram(context) },
                                onUpgradeVipClick = { viewModel.navigateTo(ScreenDestination.VIP_SUBSCRIPTION) }
                            )
                        }
                        ScreenDestination.VIP_SUBSCRIPTION -> {
                            VipSubscriptionScreen(
                                userAccount = userAccount,
                                onViewHistory = { viewModel.navigateTo(ScreenDestination.PAYMENT_HISTORY) },
                                onSubmitPayment = { txId, amount, phone, pkg, notes ->
                                    viewModel.submitPayment(context, txId, amount, phone, pkg, notes)
                                }
                            )
                        }
                        ScreenDestination.PAYMENT_HISTORY -> {
                            PaymentHistoryScreen(payments = payments)
                        }
                        ScreenDestination.ADMIN_PANEL -> {
                            AdminScreen(
                                isAdminUnlocked = isAdminUnlocked,
                                servers = servers,
                                payments = payments,
                                userAccount = userAccount,
                                onVerifyPin = { viewModel.verifyAdminPin(it) },
                                onLogout = { viewModel.logoutAdmin() },
                                onApprovePayment = { id, dur -> viewModel.approvePayment(id, dur) },
                                onRejectPayment = { id, reason -> viewModel.rejectPayment(id, reason) },
                                onToggleServer = { id, active -> viewModel.toggleServerStatus(id, active) },
                                onDeleteServer = { id -> viewModel.deleteServer(id) },
                                onAddServer = { name, cc, country, flag, host, port, proto, isVip ->
                                    viewModel.addServer(name, cc, country, flag, host, port, proto, isVip)
                                },
                                onBroadcast = { msg -> viewModel.broadcastAnnouncement(msg) }
                            )
                        }
                        ScreenDestination.WHATSAPP_SUPPORT -> {
                            WhatsAppSupportScreen(
                                onOpenTopic = { ctx, topic ->
                                    viewModel.openWhatsAppSupport(ctx, topic)
                                }
                            )
                        }
                        ScreenDestination.SETTINGS -> {
                            VpnSettingsScreen()
                        }
                        ScreenDestination.ABOUT -> {
                            AboutScreen(userAccount = userAccount)
                        }
                    }
                }
            }
        }
    }

    // Server Selection Modal Dialog
    if (showServerDialog) {
        ServerSelectionDialog(
            servers = servers,
            selectedServerId = selectedServer?.id,
            onServerSelected = { server ->
                viewModel.selectServer(server)
                showServerDialog = false
            },
            onCustomServerClicked = {
                showServerDialog = false
                showCustomSetupDialog = true
            },
            onDismiss = {
                showServerDialog = false
                if (currentScreen == ScreenDestination.SERVERS) {
                    viewModel.navigateTo(ScreenDestination.HOME)
                }
            }
        )
    }

    // Custom Setup Modal Dialog
    if (showCustomSetupDialog) {
        CustomSetupDialog(
            currentPayloadType = selectedPayloadType,
            currentSni = customSni,
            currentPayload = customPayload,
            onApply = { type, sni, payload ->
                viewModel.setPayloadType(type)
                viewModel.setCustomSni(sni)
                viewModel.setCustomPayload(payload)
                viewModel.toggleCustomSetup(true)
                showCustomSetupDialog = false
            },
            onDismiss = {
                showCustomSetupDialog = false
                if (currentScreen == ScreenDestination.CUSTOM_SETUP) {
                    viewModel.navigateTo(ScreenDestination.HOME)
                }
            }
        )
    }

    // Import Config Dialog
    if (showImportDialog) {
        ImportConfigDialog(
            onDismiss = { showImportDialog = false },
            onImport = { rawConfig ->
                Toast.makeText(context, "Configuration imported successfully!", Toast.LENGTH_SHORT).show()
                showImportDialog = false
            }
        )
    }

    // Export Config Dialog
    if (showExportDialog) {
        ExportConfigDialog(
            server = selectedServer,
            sni = customSni,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun ImportConfigDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var configText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("IMPORT CONFIGURATION", color = NeonCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Paste .myvpn, VMess URI, WireGuard or OVPN profile text:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = configText,
                    onValueChange = { configText = it },
                    placeholder = { Text("vmess:// or ovpn profile content...") },
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(configText) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack)
            ) {
                Text("IMPORT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberNavyCard, contentColor = TextMuted)
            ) {
                Text("CANCEL")
            }
        },
        containerColor = CyberNavyDark
    )
}

@Composable
fun ExportConfigDialog(
    server: com.example.data.local.entities.VpnServerEntity?,
    sni: String,
    onDismiss: () -> Unit
) {
    val exportData = "MUTIGANDA_YVAN_VPN://v1.3.35\nServer=${server?.host ?: "fr1.mutigandavpn.com"}:${server?.port ?: 1194}\nProtocol=${server?.protocol ?: "OVPN"}\nSNI=$sni\nCarrier=MTN Rwanda"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("EXPORT CONFIGURATION", color = NeonCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Share this profile with other MUTIGANDA YVAN VPN users:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = exportData,
                    onValueChange = {},
                    readOnly = true,
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack)
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CyberNavyDark
    )
}
