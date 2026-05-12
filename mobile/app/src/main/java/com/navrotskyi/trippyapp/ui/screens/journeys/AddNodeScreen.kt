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
import com.navrotskyi.trippyapp.ui.components.DateTimePickerField
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.components.TrippyErrorDialog
import com.navrotskyi.trippyapp.ui.components.TrippyLabeledField
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripNodeState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeScreen(
    tripId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var separate by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    val createNodeState by viewModel.createNodeState.collectAsState()
    val errors by viewModel.nodeFormErrors.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { viewModel.clearNodeFormErrors() }
    }

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripNodeState.Success) {
            Toast.makeText(context, "Dodano pomyślnie!", Toast.LENGTH_SHORT).show()
            viewModel.resetCreateNodeState()
            onBackClick()
        } else if (createNodeState is CreateTripNodeState.Error) {
            errorDialogMessage = (createNodeState as CreateTripNodeState.Error).message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj element", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = if (createNodeState is CreateTripNodeState.Loading) "Zapisywanie..." else "Zapisz",
                        onClick = {
                            if (viewModel.validateTripNodeForm(name, startTime, endTime, price)) {
                                viewModel.createTripNode(
                                    tripId = tripId,
                                    name = name,
                                    startTime = startTime,
                                    endTime = endTime,
                                    price = price,
                                    note = note,
                                    separate = separate,
                                    category = if (category.isBlank()) "Inne" else category
                                )
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrippyLabeledField(
                label = "Tytuł",
                value = name,
                placeholder = "np. Lot, Hotel, Obiad",
                onValueChange = { name = it },
                errorText = errors.nameError
            )

            TrippyLabeledField(
                label = "Typ / Lokalizacja",
                value = category,
                placeholder = "np. Transport, Restauracja",
                onValueChange = { category = it }
            )

            DateTimePickerField(
                label = "Start",
                value = startTime,
                onValueChange = { startTime = it },
                errorText = errors.startTimeError
            )

            DateTimePickerField(
                label = "Koniec",
                value = endTime,
                onValueChange = { endTime = it },
                errorText = errors.endTimeError
            )

            TrippyLabeledField(
                label = "Koszt",
                value = price,
                placeholder = "np. 150.00",
                onValueChange = { if (!it.startsWith("-")) price = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorText = errors.priceError
            )

            TrippyLabeledField(
                label = "Notatka",
                value = note,
                placeholder = "Opcjonalnie dodatkowe informacje",
                onValueChange = { note = it }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Checkbox(checked = separate, onCheckedChange = { separate = it })
                Text(text = "Koszty rozdzielne", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))
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