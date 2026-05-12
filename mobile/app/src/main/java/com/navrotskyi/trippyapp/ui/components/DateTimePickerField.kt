package com.navrotskyi.trippyapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorText: String? = null,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedYear by remember { mutableStateOf(0) }
    var pickedMonth by remember { mutableStateOf(0) }
    var pickedDay by remember { mutableStateOf(0) }

    val parsed = remember(value) { parseDisplayDateTime(value) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parsed?.let {
            it.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = parsed?.hour ?: 12,
        initialMinute = parsed?.minute ?: 0,
        is24Hour = true
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                enabled = false,
                placeholder = { Text("Wybierz datę i godzinę") },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = errorText != null,
                supportingText = {
                    if (errorText != null) {
                        Text(text = errorText, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        pickedYear = date.year
                        pickedMonth = date.monthValue
                        pickedDay = date.dayOfMonth
                        showDatePicker = false
                        showTimePicker = true
                    } ?: run { showDatePicker = false }
                }) { Text("Dalej") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Wybierz godzinę") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = "%02d.%02d.%04d %02d:%02d".format(
                        pickedDay, pickedMonth, pickedYear,
                        timePickerState.hour, timePickerState.minute
                    )
                    onValueChange(formatted)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            }
        )
    }
}

private fun parseDisplayDateTime(value: String): LocalDateTime? {
    return try {
        if (value.isBlank()) return null
        val (datePart, timePart) = value.trim().split(" ")
        val (d, m, y) = datePart.split(".")
        val (h, min) = timePart.split(":")
        LocalDateTime.of(y.toInt(), m.toInt(), d.toInt(), h.toInt(), min.toInt())
    } catch (e: Exception) {
        null
    }
}