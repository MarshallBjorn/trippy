package com.navrotskyi.trippyapp.ui.screens.expenses

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.data.download.BalancePdfDownloader
import com.navrotskyi.trippyapp.data.download.DownloadResult
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.ui.viewmodels.BalanceSummary
import com.navrotskyi.trippyapp.ui.viewmodels.ExpenseHistoryState
import com.navrotskyi.trippyapp.ui.viewmodels.ExpenseHistoryViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.MySettlementInfo
import com.navrotskyi.trippyapp.ui.viewmodels.ReporterFilter
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    tripId: String,
    currentUserId: String,
    viewModel: ExpenseHistoryViewModel,
    onBackClick: () -> Unit,
    onBalanceDetailsClick: (String) -> Unit
) {
    LaunchedEffect(tripId, currentUserId) {
        if (currentUserId.isNotEmpty()) viewModel.load(tripId, currentUserId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showReporterSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val startDownload: () -> Unit = {
        when (val result = BalancePdfDownloader.download(context, tripId)) {
            is DownloadResult.Started ->
                Toast.makeText(context, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
            is DownloadResult.NoAuth ->
                Toast.makeText(context, "Brak autoryzacji — zaloguj się ponownie", Toast.LENGTH_LONG).show()
            is DownloadResult.NoBaseUrl ->
                Toast.makeText(context, "Brak konfiguracji adresu API", Toast.LENGTH_LONG).show()
            is DownloadResult.Error ->
                Toast.makeText(context, "Błąd pobierania: ${result.message}", Toast.LENGTH_LONG).show()
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> startDownload() }

    val onDownloadClick: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startDownload()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historia wydatków",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Pobierz raport PDF"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ExpenseHistoryState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ExpenseHistoryState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ExpenseHistoryState.Success -> {
                    state.balanceSummary?.let { summary ->
                        BalanceSummaryCard(
                            summary = summary,
                            onClick = { onBalanceDetailsClick(tripId) }
                        )
                    }
                    FiltersBar(
                        availableCategories = state.availableCategories,
                        selectedCategories = state.selectedCategories,
                        selectedReportersCount = state.selectedReporters.size,
                        hasReporters = state.availableReporters.isNotEmpty(),
                        onCategoryToggle = viewModel::toggleCategory,
                        onPeopleClick = { showReporterSheet = true }
                    )

                    StatusBar(
                        visibleCount = state.filteredExpenses.size,
                        totalCount = state.allExpenses.size,
                        hasActiveFilters = state.selectedCategories.isNotEmpty() ||
                                state.selectedReporters.isNotEmpty(),
                        onClear = viewModel::clearFilters
                    )

                    if (state.filteredExpenses.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (state.allExpenses.isEmpty())
                                    "Brak wydatków w tej wycieczce"
                                else "Brak wydatków pasujących do filtrów",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(state.filteredExpenses, key = { it.id }) { expense ->
                                ExpenseRow(expense)
                            }
                        }
                    }

                    if (showReporterSheet) {
                        ReporterFilterSheet(
                            reporters = state.availableReporters,
                            selected = state.selectedReporters,
                            onToggle = viewModel::toggleReporter,
                            onDismiss = { showReporterSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersBar(
    availableCategories: List<String>,
    selectedCategories: Set<String>,
    selectedReportersCount: Int,
    hasReporters: Boolean,
    onCategoryToggle: (String) -> Unit,
    onPeopleClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (availableCategories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableCategories) { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { onCategoryToggle(category) },
                        label = { Text(category) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (hasReporters) {
            AssistChip(
                onClick = onPeopleClick,
                label = {
                    Text(
                        if (selectedReportersCount == 0) "Filtruj osoby"
                        else "Osoby ($selectedReportersCount)"
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun StatusBar(
    visibleCount: Int,
    totalCount: Int,
    hasActiveFilters: Boolean,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$visibleCount z $totalCount wydatków",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasActiveFilters) {
            TextButton(onClick = onClear) {
                Text("Wyczyść filtry")
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: TripNodeDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(expense.category ?: "Inne")
                    expense.reporterName?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                expense.startTime?.takeIf { it.length >= 10 }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it.substring(0, 10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "%.2f".format(expense.price),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun BalanceSummaryCard(
    summary: BalanceSummary,
    onClick: () -> Unit
) {
    val isPositive = summary.myNetBalance >= 0
    val accent = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val sign = if (isPositive) "+" else ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Twój bilans",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$sign${"%.2f".format(summary.myNetBalance)} ${summary.currency}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.totalSettlements} przelewów",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Zobacz szczegóły",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (summary.mySettlements.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                summary.mySettlements.take(3).forEach { s ->
                    MySettlementRow(s, summary.currency)
                }
            }
        }
    }
}

@Composable
private fun MySettlementRow(s: MySettlementInfo, currency: String) {
    val accent = if (s.isIncoming) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val prefix = if (s.isIncoming) "+" else "-"
    val label = if (s.isIncoming) "Otrzymujesz od ${s.otherName}" else "Oddajesz ${s.otherName}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$prefix${"%.2f".format(s.amount)} $currency",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporterFilterSheet(
    reporters: List<ReporterFilter>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filtruj po osobie",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            reporters.forEach { reporter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = reporter.id in selected,
                        onCheckedChange = { onToggle(reporter.id) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = reporter.name)
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gotowe")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
