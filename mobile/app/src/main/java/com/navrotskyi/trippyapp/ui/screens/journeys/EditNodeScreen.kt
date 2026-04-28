package com.navrotskyi.trippyapp.ui.screens.journeys

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.components.TrippyErrorDialog
import com.navrotskyi.trippyapp.ui.components.TrippyLabeledField
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripNodeState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNodeScreen(
    tripId: String,
    nodeId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    val node by viewModel.selectedNode.collectAsState()
    val createNodeState by viewModel.createNodeState.collectAsState()
    val context = LocalContext.current

    // Inicjalizacja stanów - używamy 'separate' zgodnie z modelem TripNodeDto
    var name by remember(node) { mutableStateOf(node?.name ?: "") }
    var category by remember(node) { mutableStateOf(node?.category ?: "Inne") }
    var startTime by remember(node) { mutableStateOf(node?.startTime?.let { convertIsoToDisplay(it) } ?: "") }
    var endTime by remember(node) { mutableStateOf(node?.endTime?.let { convertIsoToDisplay(it) } ?: "") }
    var price by remember(node) { mutableStateOf(node?.price?.toString() ?: "") }
    var note by remember(node) { mutableStateOf(node?.note ?: "") }
    var separate by remember(node) { mutableStateOf(node?.separate ?: false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripNodeState.Success) {
            Toast.makeText(context, "Zmiany zostały zapisane!", Toast.LENGTH_SHORT).show()
            viewModel.resetCreateNodeState()
            onBackClick() // Powrót po udanej edycji
        } else if (createNodeState is CreateTripNodeState.Error) {
            errorDialogMessage = (createNodeState as CreateTripNodeState.Error).message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj element", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                Box(Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = if (createNodeState is CreateTripNodeState.Loading) "Zapisywanie..." else "Zapisz zmiany",
                        onClick = {
                            if (name.isBlank() || startTime.isBlank() || endTime.isBlank()) {
                                Toast.makeText(context, "Wypełnij wymagane pola!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Wywołujemy funkcję z poprawnymi nazwami zmiennych
                                viewModel.updateTripNode(
                                    tripId = tripId,
                                    nodeId = nodeId,
                                    name = name,
                                    startTime = startTime,
                                    endTime = endTime,
                                    price = price,
                                    note = note,
                                    separate = separate,
                                    category = category
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Bardziej upakowane pola
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrippyLabeledField("Tytuł", name, "np. Lot, Hotel", { name = it })

            TrippyLabeledField("Typ / Lokalizacja", category, "np. Transport, Restauracja", { category = it })

            TrippyLabeledField("Start", startTime, "DD.MM.YYYY HH:MM", { startTime = it })

            TrippyLabeledField("Koniec", endTime, "DD.MM.YYYY HH:MM", { endTime = it })

            TrippyLabeledField(
                "Koszt", price, "np. 150.00", { price = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TrippyLabeledField("Notatka", note, "Opcjonalnie...", { note = it })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Checkbox(checked = separate, onCheckedChange = { separate = it })
                Text("Koszty rozdzielne", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        errorDialogMessage?.let { message ->
            TrippyErrorDialog(
                message = message,
                onDismiss = {
                    errorDialogMessage = null
                    viewModel.resetCreateTripNodeState()
                }
            )
        }
    }
}

fun convertIsoToDisplay(isoDate: String): String {
    return try {
        // Backend: 2026-05-14T10:30:00 -> UI: 14.05.2026 10:30
        val dateTime = isoDate.substring(0, 16)
        val datePart = dateTime.substring(0, 10)
        val timePart = dateTime.substring(11, 16)
        val parts = datePart.split("-")
        "${parts[2]}.${parts[1]}.${parts[0]} $timePart"
    } catch (e: Exception) {
        isoDate
    }
}