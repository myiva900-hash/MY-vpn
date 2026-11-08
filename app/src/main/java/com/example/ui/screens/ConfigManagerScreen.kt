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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.crypto.CryptoManager
import com.example.data.local.entities.OvpnConfigEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavyElevated
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
fun ConfigManagerScreen(
    configs: List<OvpnConfigEntity>,
    onSelectConfig: (Long) -> Unit,
    onDeleteConfig: (Long) -> Unit,
    onImportTextConfig: (name: String, content: String, isEncrypted: Boolean, password: String) -> Pair<Boolean, String>,
    onConnectWithConfig: (OvpnConfigEntity) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var showPasswordDecryptDialog by remember { mutableStateOf(false) }
    var pendingEncryptedPayload by remember { mutableStateOf<ByteArray?>(null) }
    var pendingConfigName by remember { mutableStateOf("") }

    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var selectedConfigToExport by remember { mutableStateOf<OvpnConfigEntity?>(null) }

    var showQrDialog by remember { mutableStateOf(false) }
    var qrPayload by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Tab Header
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberNavyDark,
            contentColor = NeonCyan,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Configs (${configs.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Import .ovpn / .myvpn", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Encrypt & Export", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Backup & Restore", fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> StoredConfigsTab(
                    configs = configs,
                    onSelect = onSelectConfig,
                    onDelete = onDeleteConfig,
                    onExport = { config ->
                        selectedConfigToExport = config
                        showExportPasswordDialog = true
                    },
                    onConnect = onConnectWithConfig,
                    onShowQr = { config ->
                        qrPayload = "MUTIGANDA_YVAN_VPN://${config.remoteHost}:${config.remotePort}?proto=${config.transport}&name=${config.name}"
                        showQrDialog = true
                    }
                )
                1 -> ImportConfigTab(
                    onImport = { name, text, password ->
                        val result = onImportTextConfig(name, text, text.startsWith("MYVPN1"), password)
                        if (result.first) {
                            Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            selectedTab = 0
                        } else {
                            Toast.makeText(context, "Error: ${result.second}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
                2 -> EncryptExportTab(
                    configs = configs,
                    onExportEncrypted = { config, password ->
                        try {
                            val encryptedBytes = CryptoManager.encryptMyVpnProfile(config.rawConfig, password)
                            val base64 = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)
                            clipboardManager.setText(AnnotatedString(base64))
                            Toast.makeText(context, "Encrypted .myvpn package copied to clipboard! (AES-256-GCM + PBKDF2)", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Encryption error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                3 -> BackupRestoreTab(
                    configs = configs,
                    onRestore = { backupText, password ->
                        try {
                            val bytes = android.util.Base64.decode(backupText.trim(), android.util.Base64.NO_WRAP)
                            val plain = CryptoManager.decryptMyVpnProfile(bytes, password)
                            onImportTextConfig("Restored Profile", plain, false, "")
                            Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                            selectedTab = 0
                        } catch (e: Exception) {
                            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    if (showExportPasswordDialog && selectedConfigToExport != null) {
        ExportPasswordModal(
            config = selectedConfigToExport!!,
            onDismiss = { showExportPasswordDialog = false },
            onExportPlain = {
                clipboardManager.setText(AnnotatedString(selectedConfigToExport!!.rawConfig))
                Toast.makeText(context, "Standard .ovpn copied to clipboard", Toast.LENGTH_SHORT).show()
                showExportPasswordDialog = false
            },
            onExportEncrypted = { password ->
                val bytes = CryptoManager.encryptMyVpnProfile(selectedConfigToExport!!.rawConfig, password)
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                clipboardManager.setText(AnnotatedString(base64))
                Toast.makeText(context, "Encrypted .myvpn package copied! AES-256-GCM protected", Toast.LENGTH_LONG).show()
                showExportPasswordDialog = false
            }
        )
    }

    if (showQrDialog) {
        QrDisplayModal(
            payload = qrPayload,
            onDismiss = { showQrDialog = false }
        )
    }
}

@Composable
private fun StoredConfigsTab(
    configs: List<OvpnConfigEntity>,
    onSelect: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onExport: (OvpnConfigEntity) -> Unit,
    onConnect: (OvpnConfigEntity) -> Unit,
    onShowQr: (OvpnConfigEntity) -> Unit
) {
    if (configs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Security, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No OpenVPN configurations stored.", color = TextMuted)
                Text("Import a standard .ovpn file or encrypted .myvpn package.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(configs) { cfg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (cfg.isSelected) NeonTurquoise else CyberNavyBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cfg.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    if (cfg.isEncrypted) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Encrypted",
                                            tint = CyberPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Remote: ${cfg.remoteHost}:${cfg.remotePort} (${cfg.transport})",
                                    color = NeonCyan,
                                    fontSize = 12.sp
                                )
                            }

                            Row {
                                IconButton(onClick = { onShowQr(cfg) }) {
                                    Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onExport(cfg) }) {
                                    Icon(Icons.Default.FileUpload, contentDescription = "Export", tint = ElectricBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onDelete(cfg.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Added: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(cfg.createdAt))}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            Button(
                                onClick = {
                                    onSelect(cfg.id)
                                    onConnect(cfg)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cfg.isSelected) NeonTurquoise else CyberNavyElevated,
                                    contentColor = if (cfg.isSelected) CyberBlack else NeonCyan
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(34.dp).testTag("connect_ovpn_${cfg.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (cfg.isSelected) "ACTIVE" else "SELECT & CONNECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportConfigTab(
    onImport: (name: String, content: String, password: String) -> Unit
) {
    var configName by remember { mutableStateOf("") }
    var configText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEncryptedInput by remember { mutableStateOf(false) }

    Column {
        Text("Import Configuration (.ovpn or .myvpn)", fontWeight = FontWeight.Bold, color = NeonCyan)
        Text("Paste standard OpenVPN directives or base64 .myvpn encrypted bundle", color = TextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = configName,
            onValueChange = { configName = it },
            label = { Text("Profile Name (Optional)") },
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = configText,
            onValueChange = {
                configText = it
                isEncryptedInput = it.startsWith("MYVPN1") || it.contains("MYVPN1")
            },
            label = { Text("Paste .ovpn or .myvpn content") },
            placeholder = { Text("client\ndev tun\nproto udp\nremote ...") },
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth().height(160.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isEncryptedInput) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Decryption Password (AES-256-GCM)") },
                visualTransformation = PasswordVisualTransformation(),
                colors = cyberTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (configText.isNotBlank()) {
                    onImport(configName, configText, password)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("import_config_submit_button")
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("VALIDATE & IMPORT CONFIGURATION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EncryptExportTab(
    configs: List<OvpnConfigEntity>,
    onExportEncrypted: (OvpnConfigEntity, String) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(configs.firstOrNull()?.id) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column {
        Text("Export Password-Protected (.myvpn)", fontWeight = FontWeight.Bold, color = NeonCyan)
        Text("Secured with AES-256-GCM & PBKDF2 (65,536 iterations). Safe to share.", color = TextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Text("Select Stored Profile:", color = TextSecondary, fontSize = 12.sp)
        configs.forEach { cfg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { selectedId = cfg.id }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedId == cfg.id) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (selectedId == cfg.id) NeonGreen else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(cfg.name, color = if (selectedId == cfg.id) NeonCyan else TextPrimary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Encryption Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val config = configs.firstOrNull { it.id == selectedId }
                if (config == null) {
                    Toast.makeText(context, "Please select a configuration first", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password.length < 4) {
                    Toast.makeText(context, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                onExportEncrypted(config, password)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("GENERATE ENCRYPTED .MYVPN PACKAGE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BackupRestoreTab(
    configs: List<OvpnConfigEntity>,
    onRestore: (backupText: String, password: String) -> Unit
) {
    var restoreText by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }

    Column {
        Text("Restore from Encrypted Backup", fontWeight = FontWeight.Bold, color = NeonCyan)
        Text("Decrypt an existing .myvpn bundle using authenticated AES-256-GCM", color = TextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = restoreText,
            onValueChange = { restoreText = it },
            label = { Text("Paste Encrypted Backup Data") },
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = restorePassword,
            onValueChange = { restorePassword = it },
            label = { Text("Backup Decryption Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = cyberTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                onRestore(restoreText, restorePassword)
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESTORE CONFIGURATION", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExportPasswordModal(
    config: OvpnConfigEntity,
    onDismiss: () -> Unit,
    onExportPlain: () -> Unit,
    onExportEncrypted: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
            modifier = Modifier.fillMaxWidth().border(1.dp, NeonCyan, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("EXPORT CONFIGURATION", fontWeight = FontWeight.Bold, color = NeonCyan)
                Text(config.name, color = TextPrimary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onExportPlain,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNavyElevated, contentColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Standard Plain (.ovpn)")
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("OR Encrypt with Password:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password for .myvpn") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (password.isNotBlank()) onExportEncrypted(password)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Encrypted (.myvpn)")
                }
            }
        }
    }
}

@Composable
private fun QrDisplayModal(
    payload: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
            modifier = Modifier.fillMaxWidth().border(1.dp, NeonTurquoise, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CONFIGURATION QR CODE", fontWeight = FontWeight.Bold, color = NeonCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black, modifier = Modifier.size(150.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = payload.take(60) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack)
                ) {
                    Text("CLOSE")
                }
            }
        }
    }
}
