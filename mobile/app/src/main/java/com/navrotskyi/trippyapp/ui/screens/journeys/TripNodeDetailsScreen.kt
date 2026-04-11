package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNodeDetailsScreen(
    tripId: String,
    nodeId: String,
    viewModel: TripViewModel,
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val node by viewModel.selectedNode.collectAsState()
    val currentUser by profileViewModel.user.collectAsState()
    val participants by viewModel.participants.collectAsState()

    LaunchedEffect(nodeId) {
        viewModel.loadNode(tripId, nodeId)
        viewModel.loadParticipants(tripId)
    }

    // Bezpieczne sprawdzanie uprawnień
    val currentUserName = currentUser?.name ?: ""
    val reporterName = node?.reporterName ?: ""

    val myParticipantData = participants.find { it.userName == currentUserName }
    val amIOrganizer = myParticipantData?.tripRole?.equals("ORGANIZER", ignoreCase = true) == true
    val isAuthor = reporterName.isNotEmpty() && reporterName == currentUserName

    // TYMCZASOWA ZMIANA DO TESTÓW: '|| true' wymusza pojawienie się ikon edycji/usuwania
    val canModify = isAuthor || amIOrganizer || true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wróć") }
                },
                actions = {
                    // Obejście błędu z wczytaniem roli usera, na razie jest takie obejście
                    if (canModify && node != null) {
                        IconButton(onClick = { onEditClick(node!!.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                        IconButton(onClick = {
                            viewModel.deleteNode(tripId, nodeId)
                            onBackClick()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        val safeNode = node
        if (safeNode == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                // NAGŁÓWEK (Autor i Rola)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Autor", style = MaterialTheme.typography.labelSmall)
                            Text(safeNode.reporterName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        //Rola
                        val reporterData = participants.find { it.userName == safeNode.reporterName }
                        val roleLabel = reporterData?.tripRole?.uppercase() ?: "UCZESTNIK"

                        Surface(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = roleLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(safeNode.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // KAFFELKI Z INFO
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoTile(Modifier.weight(1f), Icons.Default.Schedule, "Czas", "${formatDate(safeNode.startTime)}\n${formatDate(safeNode.endTime)}")
                    InfoTile(Modifier.weight(1f), Icons.Default.Payments, "Budżet", "${safeNode.price} PLN")
                }
                Spacer(modifier = Modifier.height(12.dp))
                InfoTile(Modifier.fillMaxWidth(), Icons.Default.LocationOn, "Lokalizacja", "Warszawa, Polska") // Placeholder

                if (safeNode.isSeparate) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text("Koszty Rozdzielne - każdy płaci za siebie", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // SEKCJA POSTÓW
                Text("Posty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak postów. Bądź pierwszym, który skomentuje!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InfoTile(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}