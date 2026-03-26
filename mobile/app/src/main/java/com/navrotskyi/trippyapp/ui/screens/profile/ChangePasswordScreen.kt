package com.navrotskyi.trippyapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.components.TrippyButton

@Composable
fun ChangePasswordScreen(
    onSaveClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val darkBlue = Color(0xFF142E50)

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
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Wstecz",
                                modifier = Modifier.size(20.dp),
                                tint = darkBlue
                            )
                        }
                        Text(
                            text = "Zmiana hasła",
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
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Box(modifier = Modifier.padding(16.dp)) {
                    TrippyButton(
                        text = "Zapisz nowe hasło",
                        onClick = {
                            onSaveClick(oldPassword, newPassword)
                        }
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            PasswordInputField(value = oldPassword, onValueChange = { oldPassword = it }, label = "Obecne hasło")
            Spacer(modifier = Modifier.height(24.dp))

            PasswordInputField(value = newPassword, onValueChange = { newPassword = it }, label = "Nowe hasło")
            Spacer(modifier = Modifier.height(24.dp))

            PasswordInputField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Powtórz nowe hasło")
        }
    }
}

@Composable
fun PasswordInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    val darkBlue = Color(0xFF142E50)
    val buttonBlue = Color(0xFF4C6A9C)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = darkBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = buttonBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}