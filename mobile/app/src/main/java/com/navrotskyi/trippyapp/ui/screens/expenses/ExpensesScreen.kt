package com.navrotskyi.trippyapp.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.data.network.NetworkMonitor
import com.navrotskyi.trippyapp.data.sync.SyncManager
import com.navrotskyi.trippyapp.models.ExpensesSummaryDto
import com.navrotskyi.trippyapp.models.SettlementDto
import com.navrotskyi.trippyapp.ui.components.OfflineBanner
import com.navrotskyi.trippyapp.ui.components.ShimmerBox
import com.navrotskyi.trippyapp.ui.viewmodels.ExpensesState
import com.navrotskyi.trippyapp.ui.viewmodels.ExpensesViewModel

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val isOnline by NetworkMonitor.isOnline.collectAsState(initial = true)
    val isSyncing by SyncManager.isSyncing.collectAsState()
    val pendingCount by SyncManager.pendingCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OfflineBanner(
                isOffline = !isOnline,
                isSyncing = isSyncing,
                pendingCount = pendingCount
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Twoje Finanse",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (val state = uiState) {
                    is ExpensesState.Loading -> {
                        ExpensesShimmer()
                    }
                    is ExpensesState.Success -> {
                        ExpensesContent(state.summary, viewModel)
                    }
                    is ExpensesState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesShimmer() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(modifier = Modifier.weight(1f).height(110.dp), cornerRadius = 16)
            ShimmerBox(modifier = Modifier.weight(1f).height(110.dp), cornerRadius = 16)
        }
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(28.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp), cornerRadius = 16)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp), cornerRadius = 16)
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp), cornerRadius = 16)
    }
}

@Composable
fun ExpensesContent(summary: ExpensesSummaryDto, viewModel: ExpensesViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalanceCard(
                    title = "Muszą Ci oddać",
                    amount = "+${"%.2f".format(summary.userBalance)} ${summary.currency}",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                BalanceCard(
                    title = "Musisz oddać",
                    amount = "-0.00 ${summary.currency}",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Wydatki w tym miesiącu",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TotalExpensesCard(summary)
        }

        if (summary.pendingSettlements.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Oczekujące spłaty",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            items(summary.pendingSettlements) { settlement ->
                SettlementItem(settlement, onSettleClick = { viewModel.settle(settlement.id) })
            }
        }
    }
}

@Composable
fun BalanceCard(
    title: String,
    amount: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@Composable
fun TotalExpensesCard(summary: ExpensesSummaryDto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${"%.2f".format(summary.totalSpent)} ${summary.currency}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "Łączne wydatki",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            summary.categoryBreakdown.forEach { (category, amount) ->
                CategoryProgressRow(
                    category = category,
                    amount = amount,
                    total = summary.totalSpent,
                    currency = summary.currency
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CategoryProgressRow(category: String, amount: Double, total: Double, currency: String) {
    val progressValue = if (total > 0) (amount / total).toFloat() else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${"%.0f".format(amount)} $currency",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettlementItem(settlement: SettlementDto, onSettleClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = settlement.fromUserName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (settlement.isIncoming) "Otrzymujesz od ${settlement.fromUserName}" else "Oddajesz ${settlement.toUserName}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = settlement.tripName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (settlement.isIncoming) "+" else "-"}${"%.2f".format(settlement.amount)} ${settlement.currency}",
                    color = if (settlement.isIncoming) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (!settlement.isIncoming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onSettleClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Spłać", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}