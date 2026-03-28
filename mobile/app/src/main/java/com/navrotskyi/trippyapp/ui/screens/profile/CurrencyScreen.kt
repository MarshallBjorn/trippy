package com.navrotskyi.trippyapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.components.TrippyButton

data class CurrencyItem(val code: String, val name: String)

@Composable
fun CurrencyScreen(
    currentCurrency: String,
    currencies: List<CurrencyItem>,
    onSaveClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

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
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Wstecz", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("Domyślna Waluta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(text = "Potwierdź wybór", onClick = { onSaveClick(selectedCurrency) })
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(currencies) { index, currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCurrency = currency.code }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currency.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                                Text(currency.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            }
                            RadioButton(
                                selected = selectedCurrency == currency.code,
                                onClick = { selectedCurrency = currency.code },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        if (index < currencies.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                        }
                    }
                }
            }

            Text("Ta waluta będzie domyślnie ustawiana dla nowych wycieczek.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp, horizontal = 8.dp))
        }
    }
}