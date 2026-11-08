package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VpnSettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var killSwitch by remember { mutableStateOf(false) }
    var selectedDns by remember { mutableStateOf("Cloudflare (1.1.1.1)") }
    var mtuValue by remember { mutableFloatStateOf(1500f) }
    var bypassUdp by remember { mutableStateOf(true) }
    var ipv6Support by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "TUNNEL CONFIGURATION",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(12.dp))

        // DNS Provider
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = NeonTurquoise)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DNS Provider", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Cloudflare (1.1.1.1)",
                    "Google DNS (8.8.8.8)",
                    "AdGuard DNS (Anti-Ads)",
                    "Custom DNS"
                ).forEach { dns ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedDns = dns }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dns,
                            color = if (dns == selectedDns) NeonCyan else TextSecondary,
                            fontWeight = if (dns == selectedDns) FontWeight.Bold else FontWeight.Normal
                        )
                        if (dns == selectedDns) {
                            Text("ACTIVE", color = NeonTurquoise, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Security Features
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kill Switch", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Block all traffic if VPN connection drops", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = killSwitch,
                        onCheckedChange = { killSwitch = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonTurquoise
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fast UDP Hysteria Bypass", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Optimizes gaming & streaming through firewalls", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = bypassUdp,
                        onCheckedChange = { bypassUdp = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonTurquoise
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MTU Configuration
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, CyberNavyBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Maximum Transmission Unit (MTU)", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${mtuValue.toInt()}", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
                Text("Standard 1500 for LTE; use 1420 if experiencing packet loss", fontSize = 11.sp, color = TextMuted)
                Slider(
                    value = mtuValue,
                    onValueChange = { mtuValue = it },
                    valueRange = 1280f..1500f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonTurquoise
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                Toast.makeText(context, "VPN Settings Saved Successfully", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonTurquoise, contentColor = CyberBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_vpn_settings_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("SAVE TUNNEL SETTINGS", fontWeight = FontWeight.Bold)
        }
    }
}
