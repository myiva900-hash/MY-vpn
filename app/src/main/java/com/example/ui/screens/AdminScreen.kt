package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.UserAccountEntity
import com.example.data.local.entities.VpnServerEntity
import com.example.data.model.PaymentStatus
import com.example.ui.components.formatSpeed
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    isAdminUnlocked: Boolean,
    servers: List<VpnServerEntity>,
    payments: List<PaymentEntity>,
    userAccount: UserAccountEntity?,
    onVerifyPin: (String) -> Boolean,
    onLogout: () -> Unit,
    onApprovePayment: (Long, Long) -> Unit,
    onRejectPayment: (Long, String) -> Unit,
    onToggleServer: (String, Boolean) -> Unit,
    onDeleteServer: (String) -> Unit,
    onAddServer: (String, String, String, String, String, Int, String, Boolean) -> Unit,
    onBroadcast: (String) -> Unit
) {
    val context = LocalContext.current

    if (!isAdminUnlocked) {
        AdminPinGate(
            onUnlock = { pin ->
                val ok = onVerifyPin(pin)
                if (!ok) {
                    Toast.makeText(context, "Incorrect PIN. Default is 780949484", Toast.LENGTH_SHORT).show()
                }
            }
        )
    } else {
        AdminDashboardContent(
            servers = servers,
            payments = payments,
            userAccount = userAccount,
            onLogout = onLogout,
            onApprovePayment = onApprovePayment,
            onRejectPayment = onRejectPayment,
            onToggleServer = onToggleServer,
            onDeleteServer = onDeleteServer,
            onAddServer = onAddServer,
            onBroadcast = onBroadcast
        )
    }
}

@Composable
private fun AdminPinGate(onUnlock: (String) -> Unit) {
    var pinText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberPurple.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CyberPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberPurple,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ADMINISTRATOR PORTAL",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Owner: Yvan Mutiganda",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = pinText,
                    onValueChange = { pinText = it },
                    label = { Text("Enter Admin PIN / Passcode", color = TextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )

                Text(
                    text = "Authorized access only (Owner PIN: 780949484)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onUnlock(pinText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("UNLOCK DASHBOARD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    servers: List<VpnServerEntity>,
    payments: List<PaymentEntity>,
    userAccount: UserAccountEntity?,
    onLogout: () -> Unit,
    onApprovePayment: (Long, Long) -> Unit,
    onRejectPayment: (Long, String) -> Unit,
    onToggleServer: (String, Boolean) -> Unit,
    onDeleteServer: (String) -> Unit,
    onAddServer: (String, String, String, String, String, Int, String, Boolean) -> Unit,
    onBroadcast: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddServerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Top Admin header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberNavyDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MUTIGANDA YVAN VPN ADMIN",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CyberPurple
                )
                Text(
                    text = "Welcome, Yvan Mutiganda (Owner)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonTurquoise
                )
            }
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("admin_logout_button")
            ) {
                Text("Lock", color = CyberPink, fontSize = 11.sp)
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberNavyCard,
            contentColor = NeonCyan,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Payments (${payments.count { it.status == PaymentStatus.PENDING.name }})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Servers (${servers.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Broadcast") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Users & Stats") }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> AdminPaymentsTab(payments, onApprovePayment, onRejectPayment)
                1 -> AdminServersTab(servers, onToggleServer, onDeleteServer, onAddClicked = { showAddServerDialog = true })
                2 -> AdminBroadcastTab(onBroadcast)
                3 -> AdminUsersStatsTab(userAccount)
            }
        }
    }

    if (showAddServerDialog) {
        AddServerDialog(
            onDismiss = { showAddServerDialog = false },
            onAdd = { name, cc, country, flag, host, port, proto, isVip ->
                onAddServer(name, cc, country, flag, host, port, proto, isVip)
                showAddServerDialog = false
            }
        )
    }
}

@Composable
private fun AdminPaymentsTab(
    payments: List<PaymentEntity>,
    onApprove: (Long, Long) -> Unit,
    onReject: (Long, String) -> Unit
) {
    val pending = payments.filter { it.status == PaymentStatus.PENDING.name }

    if (pending.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending payments awaiting approval.", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(pending) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, CyberAmber, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(p.packageName, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${p.amountRwf} RWF", fontWeight = FontWeight.ExtraBold, color = NeonCyan)
                        }
                        Text("TxID: ${p.txId}", color = TextSecondary, fontSize = 12.sp)
                        Text("Phone: ${p.phoneNumber}", color = TextSecondary, fontSize = 12.sp)
                        if (p.notes.isNotBlank()) {
                            Text("Note: ${p.notes}", color = TextMuted, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(p.id, p.durationHours) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
                                modifier = Modifier.weight(1f).testTag("approve_payment_${p.id}")
                            ) {
                                Text("APPROVE & ACTIVATE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReject(p.id, "Invalid TxID") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
                                modifier = Modifier.weight(0.5f).testTag("reject_payment_${p.id}")
                            ) {
                                Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminServersTab(
    servers: List<VpnServerEntity>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onAddClicked: () -> Unit
) {
    Column {
        Button(
            onClick = onAddClicked,
            colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack),
            modifier = Modifier.fillMaxWidth().testTag("admin_add_server_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ADD NEW VPN SERVER", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(servers) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.flagEmoji, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text("${s.host}:${s.port} (${s.protocol})", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = s.isActive,
                            onCheckedChange = { onToggle(s.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = NeonTurquoise
                            )
                        )
                        IconButton(onClick = { onDelete(s.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminBroadcastTab(onBroadcast: (String) -> Unit) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf("") }

    Column {
        Text("Send Announcement to All Users", fontWeight = FontWeight.Bold, color = NeonCyan)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = msg,
            onValueChange = { msg = it },
            label = { Text("Announcement Message", color = TextSecondary) },
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("broadcast_msg_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (msg.isNotBlank()) {
                    onBroadcast(msg)
                    Toast.makeText(context, "Announcement Broadcasted!", Toast.LENGTH_SHORT).show()
                    msg = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("send_broadcast_button")
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("BROADCAST TO ALL USERS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminUsersStatsTab(userAccount: UserAccountEntity?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Connected User Telemetry", fontWeight = FontWeight.Bold, color = NeonCyan)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("HWID: ${userAccount?.hwid ?: "N/A"}", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Registered Phone: ${userAccount?.phoneNumber ?: "0780949484"}", color = TextSecondary)
                Text("Membership: ${userAccount?.vipTier ?: "FREE"}", color = NeonTurquoise)
                Text("Total Uploaded: ${formatSpeed(userAccount?.totalUploadBytes ?: 0L)}", color = TextMuted)
                Text("Total Downloaded: ${formatSpeed(userAccount?.totalDownloadBytes ?: 0L)}", color = TextMuted)
            }
        }
    }
}

@Composable
private fun AddServerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, Int, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var flag by remember { mutableStateOf("🌐") }
    var protocol by remember { mutableStateOf("OVPN") }
    var isVip by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
            modifier = Modifier.fillMaxWidth().border(1.dp, NeonTurquoise, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ADD VPN SERVER", fontWeight = FontWeight.Bold, color = NeonCyan)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Server Name") }, colors = cyberTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Host / IP") }, colors = cyberTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") }, colors = cyberTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = flag, onValueChange = { flag = it }, label = { Text("Flag Emoji (e.g. 🇫🇷)") }, colors = cyberTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("Protocol (OVPN, SSH, WG, V2RAY)") }, colors = cyberTextFieldColors(), modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isVip,
                        onCheckedChange = { isVip = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyberPurple)
                    )
                    Text("VIP Server Only", color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val p = port.toIntOrNull() ?: 443
                        if (name.isNotBlank() && host.isNotBlank()) {
                            onAdd(name, "US", "Global", flag, host, p, protocol.uppercase(), isVip)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE SERVER", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
