package com.navrotskyi.trippyapp.ui.screens.journeys

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.components.TrippyErrorDialog
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripNodeState
import com.navrotskyi.trippyapp.ui.viewmodels.CreateTripState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("PLN") }
    var isCurrencyExpanded by remember { mutableStateOf(false) }
    val currencies = listOf("PLN", "USD", "EUR", "GBP")

    val formErrors by viewModel.addTripErrors.collectAsState()
    val createTripState by viewModel.createTripState.collectAsState()
    val context = LocalContext.current

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val zoneId = ZoneId.systemDefault()

    val datePickerState = rememberDatePickerState()
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var errorDialogMessage by remember { mutableStateOf<String?>(null) }


    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAddTripErrors()
            viewModel.resetCreateTripState()
        }
    }

    LaunchedEffect(createTripState) {
        when (createTripState) {
            is CreateTripState.Success -> {
                Toast.makeText(context, "Wycieczka utworzona!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateTripState()
                onBackClick()
            }
            is CreateTripState.Error -> {
                errorDialogMessage = (createTripState as CreateTripState.Error).message
            }
            else -> {}
        }
    }

    if (showStartPicker || showEndPicker) {
        DatePickerDialog(
            onDismissRequest = {
                showStartPicker = false
                showEndPicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                        if (showStartPicker) {
                            startDate = date.format(formatter)
                        } else if (showEndPicker) {
                            endDate = date.format(formatter)
                        }
                    }
                    showStartPicker = false
                    showEndPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStartPicker = false
                    showEndPicker = false
                }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowa Podróż", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz", tint = MaterialTheme.colorScheme.onSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                TrippyButton(
                    text = if (createTripState is CreateTripState.Loading) "Ładowanie..." else "Utwórz wycieczkę",
                    onClick = {
                        if (createTripState !is CreateTripState.Loading) {
                            val isValid = viewModel.validateAddTripForm(tripName, startDate, endDate, budget)
                            if (isValid) {
                                viewModel.createTrip(tripName, startDate, endDate, budget, currency)
                            }
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            AddTripField(
                label = "Nazwa wycieczki",
                value = tripName,
                placeholder = "np. \"Tatry 2026\"",
                onValueChange = { tripName = it },
                errorText = formErrors.nameError
            )

            // Data rozpoczęcia
            AddTripField(
                label = "Data rozpoczęcia",
                value = startDate,
                placeholder = "Kliknij, aby wybrać date",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.clickable { showStartPicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.clickable { showStartPicker = true }) },
                errorText = formErrors.startDateError
            )

            // Data zakończenia
            AddTripField(
                label = "Data zakończenia",
                value = endDate,
                placeholder = "Kliknij, aby wybrać date",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.clickable { showEndPicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.clickable { showEndPicker = true }) },
                errorText = formErrors.endDateError
            )

            AddTripField(
                label = "Całkowity budżet",
                value = budget,
                placeholder = "np. \"2000\"",
                onValueChange = { budget = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorText = formErrors.budgetError
            )

            // Waluta wyjazdu
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Waluta wyjazdu",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = isCurrencyExpanded,
                    onExpandedChange = { isCurrencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isCurrencyExpanded,
                        onDismissRequest = { isCurrencyExpanded = false }
                    ) {
                        currencies.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = { currency = selection; isCurrencyExpanded = false }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        errorDialogMessage?.let { message ->
            TrippyErrorDialog(
                message = message,
                onDismiss = {
                    errorDialogMessage = null
                    viewModel.resetCreateTripState()
                }
            )
        }
    }
}

@Composable
fun AddTripField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorText: String? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            isError = errorText != null,
            readOnly = readOnly,
            supportingText = {
                if (errorText != null) {
                    Text(text = errorText, color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}