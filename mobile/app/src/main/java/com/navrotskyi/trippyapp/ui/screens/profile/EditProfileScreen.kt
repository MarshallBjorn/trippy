package com.navrotskyi.trippyapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun EditProfileScreen(
    currentName: String,
    onSaveClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    val darkBlue = Color(0xFF142E50)
    val buttonBlue = Color(0xFF4C6A9C)
    val avatarBg = Color(0xFFE8ECEF)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Wstecz",
                                modifier = Modifier.size(20.dp),
                                tint = darkBlue
                            )
                        }

                        Text(
                            text = "Edycja Profilu",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkBlue,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = "Zapisz zmiany",
                        onClick = { onSaveClick(name) }
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier.size(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(avatarBg, shape = CircleShape)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 38.sp,
                        color = Color(0xFF5A6A7D),
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(buttonBlue, shape = CircleShape)
                        .clickable { /* Tu będzie logika wyboru zdjęcia */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zmień zdjęcie",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Twoja nazwa / Nick",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = buttonBlue,
                        unfocusedBorderColor = Color(0xFFC4C4C4)
                    )
                )

                Text(
                    text = "Ta nazwa będzie widoczna dla Twoich znajomych w rozliczeniach.",
                    fontSize = 16.sp,
                    color = Color(0xFF6C757D),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}