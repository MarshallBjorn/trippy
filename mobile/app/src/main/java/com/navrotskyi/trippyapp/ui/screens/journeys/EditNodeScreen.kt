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
import com.navrotskyi.trippyapp.ui.components.TrippyLabeledField
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripState
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

    var name by remember(node) { mutableStateOf<String>(node?.name ?: "") }
    var startTime by remember(node) { mutableStateOf<String>(node?.startTime?.let { convertIsoToDisplay(it) } ?: "") }
    var endTime by remember(node) { mutableStateOf<String>(node?.endTime?.let { convertIsoToDisplay(it) } ?: "") }
    var price by remember(node) { mutableStateOf<String>(node?.price?.toString() ?: "") }
    var note by remember(node) { mutableStateOf<String>(node?.note ?: "") }
    var isSeparate by remember(node) { mutableStateOf<Boolean>(node?.isSeparate ?: false) }

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripState.Success) {
            Toast.makeText(context, "Zaktualizowano wydarzenie!", Toast.LENGTH_SHORT).show()
            viewModel.resetCreateNodeState()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj wydarzenie", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                Box(Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = if (createNodeState is CreateTripState.Loading) "Zapisywanie..." else "Zapisz zmiany",
                        onClick = {
                            viewModel.updateTripNode(tripId, nodeId, name, startTime, endTime, price, note, isSeparate)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrippyLabeledField("Tytuł", name, "np. Lot, Hotel", { name = it })
            TrippyLabeledField("Start (DD.MM.YYYY HH:mm)", startTime, "np. 14.05.2026 10:00", { startTime = it })
            TrippyLabeledField("Koniec (DD.MM.YYYY HH:mm)", endTime, "np. 14.05.2026 12:30", { endTime = it })
            TrippyLabeledField("Koszt", price, "np. 150.00", { price = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            TrippyLabeledField("Notatka", note, "Dodatkowe informacje...", { note = it })

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(isSeparate, { isSeparate = it })
                Text("Koszty rozdzielne")
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Funkcja konwertująca format ISO na format wyświetlania
fun convertIsoToDisplay(isoDate: String): String {
    return try {
        val datePart = isoDate.substring(0, 10)
        val timePart = isoDate.substring(11, 16)
        val parts = datePart.split("-")
        "${parts[2]}.${parts[1]}.${parts[0]} $timePart"
    } catch (e: Exception) {
        isoDate
    }
}