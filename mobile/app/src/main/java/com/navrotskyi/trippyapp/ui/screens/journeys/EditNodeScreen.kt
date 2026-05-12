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
fun EditNodeScreen(
    tripId: String,
    nodeId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    val node by viewModel.selectedNode.collectAsState()
    val createNodeState by viewModel.createNodeState.collectAsState()
    val errors by viewModel.nodeFormErrors.collectAsState()
    val context = LocalContext.current

    var name by remember(node) { mutableStateOf(node?.name ?: "") }
    var category by remember(node) { mutableStateOf(node?.category ?: "Inne") }
    var startTime by remember(node) { mutableStateOf(node?.startTime?.let { convertIsoToDisplay(it) } ?: "") }
    var endTime by remember(node) { mutableStateOf(node?.endTime?.let { convertIsoToDisplay(it) } ?: "") }
    var price by remember(node) { mutableStateOf(node?.price?.toString() ?: "") }
    var note by remember(node) { mutableStateOf(node?.note ?: "") }
    var separate by remember(node) { mutableStateOf(node?.separate ?: false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearNodeFormErrors() }
    }

    LaunchedEffect(createNodeState) {
        if (createNodeState is CreateTripNodeState.Success) {
            Toast.makeText(context, "Zmiany zostały zapisane!", Toast.LENGTH_SHORT).show()
            viewModel.resetCreateNodeState()
            onBackClick()
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
                            if (viewModel.validateTripNodeForm(name, startTime, endTime, price)) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrippyLabeledField(
                label = "Tytuł",
                value = name,
                placeholder = "np. Lot, Hotel",
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
                placeholder = "Opcjonalnie...",
                onValueChange = { note = it }
            )

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
        val dateTime = isoDate.substring(0, 16)
        val datePart = dateTime.substring(0, 10)
        val timePart = dateTime.substring(11, 16)
        val parts = datePart.split("-")
        "${parts[2]}.${parts[1]}.${parts[0]} $timePart"
    } catch (e: Exception) {
        isoDate
    }
}