package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.VpnServerEntity
import com.example.data.model.NetworkInfo
import com.example.data.model.PayloadType
import com.example.data.model.VpnStatus
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberConnectButton
import com.example.ui.components.CyberSpeedIndicator
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
import com.example.vpn.VpnConnectionState

@Composable
fun HomeScreen(
    vpnState: VpnConnectionState,
    selectedServer: VpnServerEntity?,
    customSetupEnabled: Boolean,
    selectedPayloadType: PayloadType,
    networkInfo: NetworkInfo,
    timeLeftFormatted: String,
    onServerClick: () -> Unit,
    onCustomSetupToggle: (Boolean) -> Unit,
    onCustomSetupClick: () -> Unit,
    onConnectToggle: () -> Unit,
    onAddHoursClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onUpgradeVipClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Tunnel Frame Card (matching screenshot)
        CyberCard(
            borderColor = CyberNavyBorder,
            backgroundColor = CyberNavyDark
        ) {
            // 1. Network carrier & Live Speed row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network: ${networkInfo.carrierName}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CyberSpeedIndicator(isUpload = true, bytesPerSec = vpnState.uploadSpeedBps)
                    CyberSpeedIndicator(isUpload = false, bytesPerSec = vpnState.downloadSpeedBps)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Selected Server Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onServerClick)
                    .testTag("home_selected_server_card"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedServer?.flagEmoji ?: "🇫🇷",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedServer?.name ?: "OVPN PREMIUM SERVER 1",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedServer?.description ?: "Stable | Fast | Universal"} • ${selectedServer?.pingMs ?: 45}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    ProtocolBadge(
                        text = selectedServer?.protocol ?: "OVPN",
                        color = when (selectedServer?.protocol) {
                            "WIREGUARD" -> NeonTurquoise
                            "V2RAY" -> CyberPurple
                            else -> NeonCyan
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Select Server",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Status row and Custom Setup toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = vpnState.status.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (vpnState.status) {
                            VpnStatus.CONNECTED -> NeonGreen
                            VpnStatus.CONNECTING -> NeonCyan
                            VpnStatus.ERROR -> CyberRed
                            else -> CyberRed
                        }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Custom Setup",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = customSetupEnabled,
                        onCheckedChange = onCustomSetupToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonTurquoise,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberNavyCard
                        ),
                        modifier = Modifier.testTag("custom_setup_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Custom Setup Enabled row (matching screenshot 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (customSetupEnabled) NeonTurquoise.copy(alpha = 0.5f) else CyberNavyBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onCustomSetupClick)
                    .testTag("custom_setup_config_row"),
                colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonTurquoise.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = NeonTurquoise,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Custom Setup Enabled: ",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = selectedPayloadType.label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonTurquoise
                            )
                        }
                        Text(
                            text = "Tap to create your own setup & SNI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Large Glowing START / STOP Button
            CyberConnectButton(
                status = vpnState.status,
                onClick = onConnectToggle
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Time Left & Add hours row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time Left",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Time Left :",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = timeLeftFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddHoursClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonTurquoise,
                            contentColor = CyberBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp).testTag("add_hours_button")
                    ) {
                        Text(
                            text = "Add 4hrs",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = onUpgradeVipClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp).testTag("upgrade_vip_shortcut_button")
                    ) {
                        Text(
                            text = "VIP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telegram / WhatsApp Community Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onTelegramClick)
                .testTag("join_community_card"),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Telegram",
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Join Us On Telegram & WhatsApp",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Version & Developer Signature Footer
        Text(
            text = "MUTIGANDA YVAN VPN • Owner: Yvan Mutiganda",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = "Config Version: 1.3.35",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
