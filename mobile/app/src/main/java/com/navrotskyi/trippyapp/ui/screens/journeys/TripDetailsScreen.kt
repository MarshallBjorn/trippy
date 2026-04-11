package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.models.TripParticipantDto
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: String,
    viewModel: TripViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit,
    onInviteClick: (String) -> Unit,
    onAddNodeClick: (String) -> Unit,
    onNodeClick: (String) -> Unit // <--- DODANO
) {
    val trips by viewModel.trips.collectAsState()
    val trip = trips.find { it.id == tripId }
    val participants by viewModel.participants.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val currentUser by profileViewModel.user.collectAsState()

    LaunchedEffect(tripId) {
        viewModel.loadParticipants(tripId)
        viewModel.loadNodes(tripId)
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
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 2) onInviteClick(tripId)
                    else if (selectedTabIndex == 0) onAddNodeClick(tripId)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
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
                0 -> EventsTab(
                    tripId = tripId,
                    nodes = nodes,
                    participants = participants,
                    currentUserName = currentUser?.name ?: "",
                    onDeleteNode = { nodeId -> viewModel.deleteNode(tripId, nodeId) },
                    onNodeClick = onNodeClick // <--- PRZEKAZANO
                )
                1 -> ExpensesTabPlaceholder()
                2 -> ParticipantsTab(participants = participants)
            }
        }
    }
}

@Composable
fun EventsTab(
    tripId: String,
    nodes: List<TripNodeDto>,
    participants: List<TripParticipantDto>,
    currentUserName: String,
    onDeleteNode: (String) -> Unit,
    onNodeClick: (String) -> Unit
) {
    val myParticipantData = participants.find { it.userName == currentUserName }
    val amIOrganizer = myParticipantData?.tripRole?.equals("ORGANIZER", ignoreCase = true) == true

    if (nodes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak wydarzeń w planie. Dodaj coś!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(nodes) { node ->
                val isAuthor = node.reporterName == currentUserName
                val canEditOrDelete = isAuthor || amIOrganizer

                NodeCard(
                    node = node,
                    canEditOrDelete = canEditOrDelete,
                    onDeleteClick = { onDeleteNode(node.id) },
                    onClick = { onNodeClick(node.id) }
                )
            }
        }
    }
}

@Composable
fun NodeCard(
    node: TripNodeDto,
    canEditOrDelete: Boolean,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (node.isSeparate) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Koszty Rozdzielne",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${formatDate(node.startTime)} - ${formatDate(node.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${node.price} PLN",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!node.note.isNullOrBlank()) {
                Text(
                    text = node.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Autor: ${node.reporterName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Posty (0) - Miejsce na dyskusję",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (canEditOrDelete) {
                    Row {
                        IconButton(onClick = { /* Edycja */ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
//Formater dla dat
fun formatDate(isoDate: String): String {
    return try {
        isoDate.substring(0, 16).replace("T", " ")
    } catch (e: Exception) {
        isoDate
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
fun ExpensesTabPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Wydatki i rozliczenia (wkrótce)", color = MaterialTheme.colorScheme.onBackground)
    }
}