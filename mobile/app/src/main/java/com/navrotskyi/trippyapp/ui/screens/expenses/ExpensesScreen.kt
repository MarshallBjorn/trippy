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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.models.ExpensesSummaryDto
import com.navrotskyi.trippyapp.models.SettlementDto
import com.navrotskyi.trippyapp.ui.viewmodels.ExpensesState
import com.navrotskyi.trippyapp.ui.viewmodels.ExpensesViewModel

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Twoje Finanse",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (val state = uiState) {
            is ExpensesState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExpensesState.Success -> {
                ExpensesContent(state.summary, viewModel)
            }
            is ExpensesState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ExpensesContent(summary: ExpensesSummaryDto, viewModel: ExpensesViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalanceCard(
                    title = "Muszą Ci oddać",
                    amount = "+${summary.userBalance} ${summary.currency}",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                BalanceCard(
                    title = "Musisz oddać",
                    amount = "-150.00 ${summary.currency}", // Example static for demo if not in summary
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Wydatki w tym miesiącu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            TotalExpensesCard(summary)
        }

        item {
            Text(
                text = "Oczekujące spłaty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(summary.pendingSettlements) { settlement ->
            SettlementItem(settlement, onSettleClick = { viewModel.settle(settlement.id) })
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 12.sp, color = contentColor.copy(alpha = 0.7f))
            Text(text = amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun TotalExpensesCard(summary: ExpensesSummaryDto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${summary.totalSpent} ${summary.currency}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Łączne wydatki",
                fontSize = 14.sp,
                color = Color.Gray,
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
    val progress = if (total > 0) (amount / total).toFloat() else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = category, modifier = Modifier.width(100.dp), fontSize = 14.sp)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .padding(horizontal = 8.dp),
            color = Color(0xFF5C6BC0),
            trackColor = Color(0xFFF1F3F4),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(text = "$amount $currency", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettlementItem(settlement: SettlementDto, onSettleClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                color = Color(0xFFF1F3F4),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = settlement.fromUserName.take(2).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (settlement.isIncoming) "Otrzymujesz od ${settlement.fromUserName}" else "Oddajesz ${settlement.toUserName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(text = settlement.tripName, fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (settlement.isIncoming) "+" else "-"}${settlement.amount} ${settlement.currency}",
                    color = if (settlement.isIncoming) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (!settlement.isIncoming) {
                    Button(
                        onClick = onSettleClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
                    ) {
                        Text("Spłać", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
