package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.VpnServerEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ServerSelectionDialog(
    servers: List<VpnServerEntity>,
    selectedServerId: String?,
    isUserVip: Boolean = false,
    onServerSelected: (VpnServerEntity) -> Unit,
    onCustomServerClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberNavyDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, NeonTurquoise.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT SERVER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = NeonCyan
                    )
                    Text(
                        text = "${servers.size} Authorized Nodes",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Auto Select Server
                    item {
                        AutoSelectServerCard(
                            isSelected = selectedServerId == "auto",
                            onClick = {
                                val best = com.example.vpn.AutoServerSelector.selectBestServer(servers, isUserVip)
                                if (best != null) {
                                    onServerSelected(best.copy(name = "Auto Select Server (${best.name})"))
                                }
                                onDismiss()
                            }
                        )
                    }

                    // 2. Server List from Database
                    items(servers) { server ->
                        val isSelected = server.id == selectedServerId
                        ServerItemRow(
                            server = server,
                            isSelected = isSelected,
                            onClick = {
                                onServerSelected(server)
                                onDismiss()
                            }
                        )
                    }

                    // 3. My Custom Server Item
                    item {
                        CustomServerEntryCard(
                            onClick = {
                                onCustomServerClicked()
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AutoSelectServerCard(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isSelected) NeonTurquoise else CyberNavyBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("server_item_auto"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberNavyElevated else CyberNavyCard
        )
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
                    .background(NeonCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto Select Server",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Random Server | Lowest Latency",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            ProtocolBadge(text = "Auto", color = NeonTurquoise)

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) NeonGreen else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ServerItemRow(
    server: VpnServerEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isSelected) NeonTurquoise else CyberNavyBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("server_item_${server.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberNavyElevated else CyberNavyCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag or country emoji
            Text(
                text = server.flagEmoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (server.isVipOnly) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyberPurple,
                            modifier = Modifier
                                .background(CyberPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = "${server.description} • ${server.pingMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            ProtocolBadge(
                text = server.protocol,
                color = when (server.protocol) {
                    "OVPN" -> NeonCyan
                    "WIREGUARD" -> NeonTurquoise
                    "V2RAY" -> CyberPurple
                    else -> ElectricBlue
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) NeonGreen else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CustomServerEntryCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("server_item_custom"),
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
                    .background(CyberPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyberPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MY CUSTOM SERVER",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Click to configure custom host/SSH/proxy",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            ProtocolBadge(text = "CUSTOM", color = CyberPurple)
        }
    }
}

@Composable
fun ProtocolBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
            ),
            color = color
        )
    }
}
