package com.navrotskyi.trippyapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.theme.SuccessGreen
import coil.compose.AsyncImage
import com.navrotskyi.trippyapp.ui.viewmodels.GroupBalanceState
import com.navrotskyi.trippyapp.ui.viewmodels.GroupBalanceViewModel
import androidx.compose.runtime.getValue

data class ParticipantBalance(val name: String, val balance: Double, val initials: String, val avatarUrl: String? = null, val isMe: Boolean = false)
enum class SettlementType { I_OWE_THEM, THEY_OWE_ME }
data class Settlement(val otherPersonName: String, val amount: Double, val type: SettlementType)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupBalanceScreen(
    tripId: String,
    viewModel: GroupBalanceViewModel,
    onBackClick: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bilans Grupy",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        when (val state = uiState) {
            is GroupBalanceState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is GroupBalanceState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Błąd: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is GroupBalanceState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TotalBalanceCard(balance = state.totalBalance)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Stan uczestników",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ParticipantsRow(participants = state.participants)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Twoje rozliczenia",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    state.settlements.forEach { settlement ->
                        SettlementCard(settlement)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TotalBalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Twój całkowity bilans:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val color = if (balance < 0) MaterialTheme.colorScheme.error else if (balance > 0) SuccessGreen else MaterialTheme.colorScheme.onSurface
            val sign = if (balance > 0) "+" else ""
            Text(
                text = "$sign${String.format("%.2f", balance)} PLN",
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ParticipantsRow(participants: List<ParticipantBalance>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(participants) { participant ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (participant.avatarUrl != null) {
                        AsyncImage(
                            model = participant.avatarUrl,
                            contentDescription = "Awatar użytkownika ${participant.name}",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = participant.initials,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(participant.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                val color = if (participant.balance < 0) MaterialTheme.colorScheme.error else if (participant.balance > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                val sign = if (participant.balance > 0) "+" else ""
                Text(
                    text = "$sign${String.format("%.2f", participant.balance)}",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SettlementCard(settlement: Settlement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                }
                Spacer(modifier = Modifier.height(12.dp))

                val titleText = if (settlement.type == SettlementType.I_OWE_THEM) "Oddajesz ${settlement.otherPersonName}:" else "${settlement.otherPersonName} oddaje Ci:"
                val amountColor = if (settlement.type == SettlementType.I_OWE_THEM) MaterialTheme.colorScheme.error else SuccessGreen

                Text(titleText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${String.format("%.2f", settlement.amount)} PLN", color = amountColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (settlement.type == SettlementType.I_OWE_THEM) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Spłać", color = MaterialTheme.colorScheme.onPrimary)
                }
            } else {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Przypomnij", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
