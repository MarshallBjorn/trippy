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
import androidx.compose.ui.graphics.Color
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

    val darkBlue = Color(0xFF142E50)
    val greyBg = Color(0xFFF8F9FA)

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Wstecz", modifier = Modifier.size(20.dp), tint = darkBlue)
                        }
                        Text("Domyślna Waluta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkBlue, modifier = Modifier.padding(start = 8.dp))
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(text = "Potwierdź wybór", onClick = { onSaveClick(selectedCurrency) })
                }
            }
        },
        containerColor = greyBg
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(currencies) { index, currency ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedCurrency = currency.code }.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currency.code, fontWeight = FontWeight.Bold, color = darkBlue, fontSize = 16.sp)
                                Text(currency.name, color = Color.Gray, fontSize = 16.sp)
                            }
                            RadioButton(
                                selected = selectedCurrency == currency.code,
                                onClick = { selectedCurrency = currency.code },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4C6A9C), unselectedColor = Color(0xFFE0E0E0))
                            )
                        }
                        if (index < currencies.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0), thickness = 1.dp)
                        }
                    }
                }
            }

            Text("Ta waluta będzie domyślnie ustawiana dla nowych wycieczek.", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(vertical = 20.dp, horizontal = 8.dp))
        }
    }
}