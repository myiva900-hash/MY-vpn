package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserAccountEntity
import com.example.data.model.VipPackage
import com.example.data.model.VipPackages
import com.example.ui.components.CopyableInfoRow
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
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
fun VipSubscriptionScreen(
    userAccount: UserAccountEntity?,
    onViewHistory: () -> Unit,
    onSubmitPayment: (String, Long, String, VipPackage, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var selectedPackage by remember { mutableStateOf(VipPackages.ALL.first()) }
    var txIdInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf(userAccount?.phoneNumber ?: "0780949484") }
    var notesInput by remember { mutableStateOf("") }

    val isVipActive = (userAccount?.vipExpiresAt ?: 0L) > System.currentTimeMillis()
    val expiryDateStr = if (isVipActive) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(userAccount!!.vipExpiresAt))
    } else {
        "Expired / Free Trial"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // VIP Header Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isVipActive) CyberPurple else CyberNavyBorder,
                    RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isVipActive) "VIP MEMBERSHIP ACTIVE" else "FREE MEMBERSHIP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isVipActive) CyberPurple else NeonTurquoise
                        )
                        Text(
                            text = "Expires: $expiryDateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isVipActive) NeonCyan else TextMuted
                        )
                    }
                    Button(
                        onClick = onViewHistory,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberNavyElevated,
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("vip_payment_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIP Packages Selection
        Text(
            text = "SELECT VIP PACKAGE",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = NeonCyan,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VipPackages.ALL.forEach { pkg ->
                val isSelected = pkg.id == selectedPackage.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            if (isSelected) CyberPurple else CyberNavyBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedPackage = pkg }
                        .testTag("vip_package_${pkg.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CyberNavyElevated else CyberNavyCard
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                if (pkg.isPopular) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MOST POPULAR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = CyberPink,
                                        modifier = Modifier
                                            .background(CyberPink.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${pkg.priceRwf} RWF",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonTurquoise
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        pkg.features.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonTurquoise,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Official MTN Mobile Money Instructions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberAmberBorder(), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFCC00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MoMo", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MTN Mobile Money Payment",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFCC00)
                        )
                        Text(
                            text = "Pay via MoMo Code or Transfer, then submit below",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CopyableInfoRow(
                    label = "ACCOUNT NAME",
                    value = "Yvan Mutiganda",
                    onCopied = { Toast.makeText(context, "Account Name Copied!", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CopyableInfoRow(
                    label = "MTN PHONE NUMBER",
                    value = "0780949484",
                    onCopied = { Toast.makeText(context, "Phone Number Copied!", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CopyableInfoRow(
                    label = "PAYMENT CODE / MERCHANT",
                    value = "2238763",
                    onCopied = { Toast.makeText(context, "Payment Code Copied!", Toast.LENGTH_SHORT).show() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        // Quick USSD dialer for MTN MoMo
                        try {
                            val ussd = "*182*8*1*2238763*${selectedPackage.priceRwf}%23"
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$ussd"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Dial *182# on your MTN SIM to pay", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("momo_ussd_dial_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFCC00),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DIAL MOMO CODE (*182*8*1*2238763#)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submission Form
        Text(
            text = "SUBMIT PAYMENT FOR VERIFICATION",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = NeonCyan,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = txIdInput,
                    onValueChange = { txIdInput = it },
                    label = { Text("MTN Transaction ID (TxID)", color = TextSecondary) },
                    placeholder = { Text("e.g. 19827364829", color = TextMuted) },
                    singleLine = true,
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("payment_txid_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number Used", color = TextSecondary) },
                    singleLine = true,
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("payment_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Additional Note / Sender Name (Optional)", color = TextSecondary) },
                    singleLine = true,
                    colors = cyberTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmitPayment(
                            txIdInput,
                            selectedPackage.priceRwf,
                            phoneInput,
                            selectedPackage,
                            notesInput
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_vip_payment_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SUBMIT FOR ADMIN APPROVAL",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun cyberTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = CyberNavyBorder,
    focusedContainerColor = CyberNavyDark,
    unfocusedContainerColor = CyberNavyDark
)

@Composable
fun CyberAmberBorder(): Color = Color(0xFFFFCC00).copy(alpha = 0.5f)
