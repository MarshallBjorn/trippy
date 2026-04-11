package com.navrotskyi.trippyapp.ui.screens.journeys

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.models.TripParticipantDto
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripNodeState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit,
    onInviteClick: (String) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val trip = trips.find { it.id == tripId }
    val participants by viewModel.participants.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val createNodeState by viewModel.createTripNodeState.collectAsState()
    val context = LocalContext.current

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<TripNodeDto?>(null) }

    LaunchedEffect(tripId) {
        viewModel.loadParticipants(tripId)
        viewModel.loadExpenses(tripId)
    }

    LaunchedEffect(createNodeState) {
        when (createNodeState) {
            is CreateTripNodeState.Success -> {
                Toast.makeText(context, "Operacja udana!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateTripNodeState()
                showAddExpenseDialog = false
                expenseToEdit = null
            }
            is CreateTripNodeState.Error -> {
                Toast.makeText(context, (createNodeState as CreateTripNodeState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateTripNodeState()
            }
            else -> {}
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    val tabs = listOf("Wydarzenia", "Wydatki", "Uczestnicy")

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nie znaleziono wycieczki", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    if (showAddExpenseDialog || expenseToEdit != null) {
        AddTripNodeDialog(
            tripId = tripId,
            viewModel = viewModel,
            expenseToEdit = expenseToEdit,
            onDismiss = {
                showAddExpenseDialog = false
                expenseToEdit = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(text = trip.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                text = "${trip.startDate} - ${trip.endDate}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            when (selectedTabIndex) {
                1 -> {
                    FloatingActionButton(
                        onClick = { showAddExpenseDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj wydatek")
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { onInviteClick(tripId) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zaproś uczestnika")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> EventsTabPlaceholder()
                1 -> ExpensesTab(
                    expenses = expenses,
                    currency = trip.pickedCurrency,
                    onEdit = { expenseToEdit = it },
                    onDelete = { viewModel.deleteTripNode(tripId, it.id) }
                )
                2 -> ParticipantsTab(participants = participants)
            }
        }
    }
}

@Composable
fun ExpensesTab(
    expenses: List<TripNodeDto>,
    currency: String,
    onEdit: (TripNodeDto) -> Unit,
    onDelete: (TripNodeDto) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Brak wydatków. Dodaj pierwszy wydatek!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val total = expenses.sumOf { it.price }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Suma wydatków",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f %s", total, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            items(expenses) { expense ->
                ExpenseCard(
                    expense = expense,
                    currency = currency,
                    onEdit = { onEdit(expense) },
                    onDelete = { onDelete(expense) }
                )
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: TripNodeDto,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondary, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = expense.category ?: "Inne",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f %s", expense.price, currency),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edytuj",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantsTab(participants: List<TripParticipantDto>) {
    if (participants.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Brak uczestników. Kliknij + aby kogoś zaprosić!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(participants) { participant ->
                ParticipantCard(participant)
            }
        }
    }
}

@Composable
fun ParticipantCard(participant: TripParticipantDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.userName ?: "Nieznany użytkownik",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (participant.isAccepted) "Dołączył(a) - ${participant.tripRole}" else "Oczekuje na akceptację",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (participant.isAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EventsTabPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Lista wydarzeń (wkrótce)", color = MaterialTheme.colorScheme.onBackground)
    }
}
