package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.UserAccountEntity
import com.example.ui.components.formatSpeed
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AboutScreen(userAccount: UserAccountEntity?) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(CyberNavyDark)
                .border(2.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_vpn_logo),
                contentDescription = null,
                modifier = Modifier.size(66.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "MUTIGANDA YVAN VPN",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Owner & Developer: Yvan Mutiganda",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = NeonTurquoise
        )

        Text(
            text = "Version 1.3.35 (Production Build)",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Device & User Account Specs Card
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEVICE TELEMETRY & ACCOUNT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                AboutDetailRow(label = "Hardware ID (HWID)", value = userAccount?.hwid ?: "HWID-849102")
                AboutDetailRow(label = "Membership Level", value = userAccount?.vipTier ?: "Free Tier")
                AboutDetailRow(label = "Client Name", value = userAccount?.username ?: "Client")
                AboutDetailRow(label = "Total Downloaded", value = formatSpeed(userAccount?.totalDownloadBytes ?: 0L))
                AboutDetailRow(label = "Total Uploaded", value = formatSpeed(userAccount?.totalUploadBytes ?: 0L))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Official Contacts Card
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OFFICIAL CONTACTS & PAYMENT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonTurquoise
                )
                Spacer(modifier = Modifier.height(10.dp))

                AboutDetailRow(label = "Developer Name", value = "Yvan Mutiganda")
                AboutDetailRow(label = "MTN MoMo Number", value = "0780949484")
                AboutDetailRow(label = "MoMo Payment Code", value = "2238763")
                AboutDetailRow(label = "WhatsApp Support", value = "+250 787 699 523")
                AboutDetailRow(label = "Optimized Carrier", value = "MTN Rwanda [RW]")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security & Protocols Architecture Card
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SUPPORTED PROTOCOLS & ENGINE",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyberPurple
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• OpenVPN (UDP/TCP TUN)\n" +
                            "• WireGuard (Kernel Fast Cryptography)\n" +
                            "• Secure Shell (SSH Direct, SSL, WS Proxy)\n" +
                            "• V2Ray / Xray (VMess, VLESS, Trojan)\n" +
                            "• DNSTT / SlowDNS (Restricted ISP Bypass)\n" +
                            "• UDP Hysteria (Congestion-Control Tunnel)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "© 2026 MUTIGANDA YVAN VPN. All Rights Reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun AboutDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
    }
}
