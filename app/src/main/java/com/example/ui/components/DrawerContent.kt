package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.UserAccountEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonTurquoise
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DrawerContent(
    userAccount: UserAccountEntity?,
    onNavigate: (ScreenDestination) -> Unit,
    onCloseDrawer: () -> Unit,
    onImportConfig: () -> Unit,
    onExportConfig: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(CyberNavyDark)
            .verticalScroll(scrollState)
    ) {
        // App Drawer Header matching screenshot 4
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(NeonTurquoise.copy(alpha = 0.85f), Color(0xFF00695C))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(CyberBlack.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_vpn_logo),
                        contentDescription = "MUTIGANDA YVAN VPN Logo",
                        modifier = Modifier.size(54.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MUTIGANDA YVAN VPN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Owner: Yvan Mutiganda",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = CyberBlack.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .background(CyberBlack.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tier: ${userAccount?.vipTier ?: "FREE"} • HWID: ${(userAccount?.hwid ?: "128492").take(9)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        DrawerItem(
            label = "Home",
            icon = Icons.Default.Home,
            tint = NeonCyan,
            onClick = { onNavigate(ScreenDestination.HOME); onCloseDrawer() }
        )

        DrawerItem(
            label = "Select Server",
            icon = Icons.Default.Dns,
            tint = NeonTurquoise,
            onClick = { onNavigate(ScreenDestination.SERVERS); onCloseDrawer() }
        )

        DrawerItem(
            label = "Custom Server",
            icon = Icons.Default.Tune,
            tint = ElectricBlue,
            onClick = { onNavigate(ScreenDestination.CUSTOM_SETUP); onCloseDrawer() }
        )

        DrawerItem(
            label = "OVPN & Config Manager",
            icon = Icons.Default.Dns,
            tint = NeonTurquoise,
            badge = "AES-256",
            onClick = { onNavigate(ScreenDestination.CONFIG_MANAGER); onCloseDrawer() }
        )

        DrawerItem(
            label = "VIP Subscription",
            icon = Icons.Default.Star,
            tint = CyberPurple,
            badge = "MTN MoMo",
            onClick = { onNavigate(ScreenDestination.VIP_SUBSCRIPTION); onCloseDrawer() }
        )

        DrawerItem(
            label = "Payment History",
            icon = Icons.Default.Receipt,
            tint = NeonCyan,
            onClick = { onNavigate(ScreenDestination.PAYMENT_HISTORY); onCloseDrawer() }
        )

        DrawerItem(
            label = "My Account",
            icon = Icons.Default.Person,
            tint = NeonTurquoise,
            onClick = { onNavigate(ScreenDestination.ABOUT); onCloseDrawer() }
        )

        DrawerItem(
            label = "WhatsApp Support",
            icon = Icons.Default.SupportAgent,
            tint = Color(0xFF25D366),
            badge = "+250 787",
            onClick = { onNavigate(ScreenDestination.WHATSAPP_SUPPORT); onCloseDrawer() }
        )

        HorizontalDivider(
            color = CyberNavyBorder,
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        DrawerItem(
            label = "VPN Settings",
            icon = Icons.Default.Settings,
            tint = TextSecondary,
            onClick = { onNavigate(ScreenDestination.SETTINGS); onCloseDrawer() }
        )

        DrawerItem(
            label = "Connection Logs",
            icon = Icons.Default.Terminal,
            tint = NeonCyan,
            onClick = { onNavigate(ScreenDestination.LOGS); onCloseDrawer() }
        )

        DrawerItem(
            label = "Import Configuration (.myvpn)",
            icon = Icons.Default.CloudDownload,
            tint = ElectricBlue,
            onClick = { onImportConfig(); onCloseDrawer() }
        )

        DrawerItem(
            label = "Export Configuration",
            icon = Icons.Default.CloudUpload,
            tint = CyberPurple,
            onClick = { onExportConfig(); onCloseDrawer() }
        )

        HorizontalDivider(
            color = CyberNavyBorder,
            thickness = 0.8.dp,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        DrawerItem(
            label = "Administrator Portal",
            icon = Icons.Default.AdminPanelSettings,
            tint = CyberPink,
            badge = "Yvan M.",
            onClick = { onNavigate(ScreenDestination.ADMIN_PANEL); onCloseDrawer() }
        )

        DrawerItem(
            label = "About Application",
            icon = Icons.Default.Info,
            tint = TextMuted,
            onClick = { onNavigate(ScreenDestination.ABOUT); onCloseDrawer() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    tint: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("drawer_item_${label.replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = tint,
                modifier = Modifier
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
