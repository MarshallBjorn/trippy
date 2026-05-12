package com.navrotskyi.trippyapp.ui.screens.journeys

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.models.TripParticipantDto
import com.navrotskyi.trippyapp.ui.components.TrippyErrorDialog
import com.navrotskyi.trippyapp.ui.components.TrippyOutlinedButton
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripNodeState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: String,
    isOwner: Boolean,
    viewModel: TripViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onInviteClick: (String) -> Unit,
    onAddNodeClick: (String) -> Unit,
    onNodeClick: (String) -> Unit,
    onGroupBalanceClick: (String) -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val trip = trips.find { it.id == tripId }

    val participants by viewModel.participants.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val currentUser by profileViewModel.user.collectAsState()

    val createNodeState by viewModel.createNodeState.collectAsState()
    val context = LocalContext.current
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tripId) {
        viewModel.loadParticipants(tripId)
        viewModel.loadNodes(tripId)
    }

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripNodeState.Success) {
            viewModel.resetCreateNodeState()
        } else if (createNodeState is CreateTripNodeState.Error) {
            errorDialogMessage = (createNodeState as CreateTripNodeState.Error).message
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wydarzenia", "Wydatki", "Uczestnicy")

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nie znaleziono wycieczki", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(text = trip.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            // POPRAWKA: Formatowanie głównych dat wycieczki
                            val formattedStart = formatTripDate(trip.startDate)
                            val formattedEnd = formatTripDate(trip.endDate)
                            val dateDisplay = if (formattedStart.isNotEmpty()) "$formattedStart - $formattedEnd" else "Brak dat"

                            Text(
                                text = dateDisplay,
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
                0, 1 -> {
                    val canCreateNode = participants.any {
                        it.userName == currentUser?.name &&
                                it.isAccepted &&
                                it.tripRole != "VIEWER"
                    }
                    if (canCreateNode) {
                        if (isOwner) {
                            FloatingActionButton(
                                onClick = { onAddNodeClick(tripId) },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Dodaj")
                            }
                        }
                    }
                }
                2 -> {
                    if (isOwner) {
                        FloatingActionButton(
                            onClick = { onInviteClick(tripId) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zaproś")
                        }
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
            TrippyOutlinedButton(
                text = "Zobacz bilans grupy",
                onClick = { onGroupBalanceClick(tripId)},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
                0 -> EventsTab(tripId, nodes, participants, currentUser?.name ?: "", { viewModel.deleteTripNode(tripId, it) }, onNodeClick)
                1 -> ExpensesTab(expenses, trip.pickedCurrency, { onNodeClick(it.id) }, { viewModel.deleteTripNode(tripId, it.id) })
                2 -> ParticipantsTab(participants)
            }
        }

        errorDialogMessage?.let { message ->
            TrippyErrorDialog(
                message = message,
                onDismiss = {
                    errorDialogMessage = null
                    viewModel.resetCreateNodeState()
                }
            )
        }
    }
}

@Composable
fun EventsTab(tripId: String, nodes: List<TripNodeDto>, participants: List<TripParticipantDto>, currentUserName: String, onDeleteNode: (String) -> Unit, onNodeClick: (String) -> Unit) {
    if (nodes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak wydarzeń. Dodaj coś!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(nodes) { node ->

                NodeCard(
                    node = node,
                    onDeleteClick = { onDeleteNode(node.id) },
                    onClick = { onNodeClick(node.id) }
                )
            }
        }
    }
}

@Composable
fun NodeCard(node: TripNodeDto, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(node.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (node.separate) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                        Text("Rozdzielne", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTimeRange(node.startTime, node.endTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${node.price} PLN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Autor: ${node.reporterName ?: "Nieznany"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Typ: ${node.category ?: "Inne"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                if (node.canDelete) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                    }
                }
                else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Brak uprawnień",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// FORMATER CZASU TRWANIA DLA WYDARZEŃ (NodeCard)
fun formatTimeRange(start: String?, end: String?): String {
    if (start.isNullOrBlank() || end.isNullOrBlank()) return "Brak przypisanego czasu"
    return try {
        if (start.length >= 16 && end.length >= 16) {
            val sTime = start.substring(11, 16)
            val sDate = "${start.substring(8, 10)}.${start.substring(5, 7)}"
            val eTime = end.substring(11, 16)
            val eDate = "${end.substring(8, 10)}.${end.substring(5, 7)}"
            if (sDate == eDate) "$sDate, $sTime - $eTime" else "$sDate $sTime - $eDate $eTime"
        } else {
            "${start.replace("T", " ")} - ${end.replace("T", " ")}"
        }
    } catch (e: Exception) {
        "${start ?: ""} - ${end ?: ""}".replace("T", " ")
    }
}

// FORMATER DATY DLA GŁÓWNEJ WYCIECZKI (W PASKU TOP APP BAR)
fun formatTripDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        // Wyciąga samo YYYY-MM-DD z daty od serwera
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else isoDate
    } catch (e: Exception) {
        isoDate
    }
}

@Composable
fun ExpensesTab(expenses: List<TripNodeDto>, currency: String, onEdit: (TripNodeDto) -> Unit, onDelete: (TripNodeDto) -> Unit) {
    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak wydatków. Dodaj pierwszy wydatek!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val total = expenses.sumOf { it.price }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Suma wydatków", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(String.format(Locale.getDefault(), "%.2f %s", total, currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            items(expenses) { expense ->
                ExpenseCard(expense = expense, currency = currency, onEdit = { onEdit(expense) }, onDelete = { onDelete(expense) })
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: TripNodeDto, currency: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category ?: "Inne", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.getDefault(), "%.2f %s", expense.price, currency), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                Row {
                    if (expense.canEdit) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    }
                    else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Brak uprawnień",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    if (expense.canDelete) {
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
}

@Composable
fun ParticipantsTab(participants: List<TripParticipantDto>) {
    if (participants.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak uczestników. Kliknij + aby kogoś zaprosić!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(participants) { participant -> ParticipantCard(participant) }
        }
    }
}

@Composable
fun ParticipantCard(participant: TripParticipantDto) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(participant.userName ?: "Nieznany użytkownik", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (participant.isAccepted) "Dołączył(a) - ${participant.tripRole}" else "Oczekuje na akceptację", style = MaterialTheme.typography.bodySmall, color = if (participant.isAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}