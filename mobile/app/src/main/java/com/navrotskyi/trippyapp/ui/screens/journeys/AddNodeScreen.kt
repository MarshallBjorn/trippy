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
fun AddNodeScreen(
    tripId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isSeparate by remember { mutableStateOf(false) }

    val createNodeState by viewModel.createNodeState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripState.Success) {
            Toast.makeText(context, "Dodano wydarzenie do planu!", Toast.LENGTH_SHORT).show()
            viewModel.resetCreateNodeState()
            onBackClick()
        } else if (createNodeState is CreateTripState.Error) {
            Toast.makeText(context, (createNodeState as CreateTripState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetCreateNodeState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj wydarzenie", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = if (createNodeState is CreateTripState.Loading) "Zapisywanie..." else "Zapisz w planie",
                        onClick = {
                            if (name.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                                viewModel.createTripNode(tripId, name, startTime, endTime, price, note, isSeparate)
                            } else {
                                Toast.makeText(context, "Wypełnij wymagane pola (Nazwa, Daty)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrippyLabeledField(label = "Tytuł", value = name, placeholder = "np. Lot, Hotel, Zwiedzanie", onValueChange = { name = it })
            TrippyLabeledField(label = "Start (DD.MM.YYYY HH:mm)", value = startTime, placeholder = "np. 14.05.2026 10:00", onValueChange = { startTime = it })
            TrippyLabeledField(label = "Koniec (DD.MM.YYYY HH:mm)", value = endTime, placeholder = "np. 14.05.2026 12:30", onValueChange = { endTime = it })
            TrippyLabeledField(
                label = "Szacowany Koszt",
                value = price,
                placeholder = "np. 150.50",
                onValueChange = { price = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TrippyLabeledField(label = "Notatka / Opis", value = note, placeholder = "Dodatkowe informacje...", onValueChange = { note = it })

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSeparate, onCheckedChange = { isSeparate = it })
                Text(text = "Koszty rozdzielne (Każdy płaci za siebie)", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}