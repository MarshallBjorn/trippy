package com.navrotskyi.trippyapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.components.TrippyTextField
import com.navrotskyi.trippyapp.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val errors by viewModel.registerErrors.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearRegisterErrors() }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Dołącz do Trippy",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            TrippyTextField(
                value = name,
                onValueChange = { name = it },
                label = "Imię lub Nick",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                errorText = errors.nameError
            )
            Spacer(Modifier.height(16.dp))

            TrippyTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                errorText = errors.emailError
            )
            Spacer(Modifier.height(16.dp))

            TrippyTextField(
                value = password,
                onValueChange = { password = it },
                label = "Hasło",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                errorText = errors.passwordError
            )
            Spacer(Modifier.height(16.dp))

            TrippyTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Powtórz hasło",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                errorText = errors.confirmPasswordError
            )
            Spacer(Modifier.height(32.dp))

            TrippyButton(
                text = "Zarejestruj się",
                onClick = {
                    if (viewModel.validateRegisterForm(name, email, password, confirmPassword)) {
                        onRegisterClick(name, email, password, confirmPassword)
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onBackToLoginClick) {
                Text(
                    text = "Masz już konto? Zaloguj się",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}