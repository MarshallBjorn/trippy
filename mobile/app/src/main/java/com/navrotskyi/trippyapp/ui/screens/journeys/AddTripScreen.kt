package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel

@Composable
fun AddTripScreen(
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    // Stany formularza
    var tripName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }


    val formErrors by viewModel.addTripErrors.collectAsState()


    DisposableEffect(Unit) {
        onDispose { viewModel.clearAddTripErrors() }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Wstecz",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Nowa Podróż",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = "Utwórz wycieczkę",
                        onClick = {
                            val isValid = viewModel.validateAddTripForm(
                                name = tripName,
                                startDate = startDate,
                                endDate = endDate,
                                budget = budget
                            )

                            if (isValid) {
                                //
                                println("Wszystko się zgadza! Formularz jest gotowy do wysłania.")
                            }
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AddTripInputField(
                value = tripName,
                onValueChange = { tripName = it },
                label = "Nazwa wycieczki",
                placeholder = "np. Tatry 2026",
                errorText = formErrors.nameError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AddTripInputField(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Data rozpoczęcia",
                placeholder = "DD.MM.RRRR",
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                errorText = formErrors.startDateError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AddTripInputField(
                value = endDate,
                onValueChange = { endDate = it },
                label = "Data zakończenia",
                placeholder = "DD.MM.RRRR",
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                errorText = formErrors.endDateError
            )

            Spacer(modifier = Modifier.height(24.dp))

            AddTripInputField(
                value = budget,
                onValueChange = { budget = it },
                label = "Całkowity budżet",
                placeholder = "np. 2000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorText = formErrors.budgetError
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AddTripInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorText: String? = null
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
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions,


            isError = errorText != null,


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