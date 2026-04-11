package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AddTripNodeDialog(
    tripId: String,
    viewModel: TripViewModel,
    expenseToEdit: TripNodeDto? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(expenseToEdit?.name ?: "") }
    var price by remember { mutableStateOf(expenseToEdit?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(expenseToEdit?.category ?: "Inne") }
    var note by remember { mutableStateOf(expenseToEdit?.note ?: "") }
    var isSeparate by remember { mutableStateOf(expenseToEdit?.isSeparate ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expenseToEdit == null) "Dodaj wydatek" else "Edytuj wydatek", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notatka (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isSeparate, onCheckedChange = { isSeparate = it })
                    Text("Osobny wydatek")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = price.toDoubleOrNull() ?: 0.0
                    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    if (expenseToEdit == null) {
                        viewModel.createTripNode(
                            tripId = tripId,
                            name = name,
                            price = priceVal,
                            category = category,
                            start = now,
                            end = now,
                            isSeparate = isSeparate,
                            note = if (note.isBlank()) null else note
                        )
                    } else {
                        viewModel.updateTripNode(
                            tripId = tripId,
                            nodeId = expenseToEdit.id,
                            name = name,
                            price = priceVal,
                            category = category,
                            start = expenseToEdit.start ?: now,
                            end = expenseToEdit.end ?: now,
                            isSeparate = isSeparate,
                            note = if (note.isBlank()) null else note
                        )
                    }
                    onDismiss()
                }
            ) {
                Text(if (expenseToEdit == null) "Dodaj" else "Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
