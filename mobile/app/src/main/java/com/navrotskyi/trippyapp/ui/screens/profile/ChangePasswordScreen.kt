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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navrotskyi.trippyapp.ui.components.TrippyButton

@Composable
fun ChangePasswordScreen(
    onSaveClick: (String, String, (String?) -> Unit) -> Unit,
    onBackClick: () -> Unit,
    viewModel: com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val errors by viewModel.changePasswordErrors.collectAsState()
    val isLoading = uiState is com.navrotskyi.trippyapp.models.ProfileUiState.Loading
    val backendError = (uiState as? com.navrotskyi.trippyapp.models.ProfileUiState.Error)?.message

    DisposableEffect(Unit) {
        onDispose { viewModel.clearChangePasswordErrors() }
    }

    fun submit() {
        if (isSaving) return
        if (!viewModel.validateChangePasswordForm(oldPassword, newPassword, confirmPassword)) return

        isSaving = true
        onSaveClick(oldPassword, newPassword) { backendErr ->
            isSaving = false
            if (backendErr == null) onBackClick()
        }
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
                            text = "Zmiana hasła",
                            fontSize = 20.sp,
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
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.animation.AnimatedVisibility(visible = backendError != null) {
                        Text(
                            text = backendError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    TrippyButton(
                        text = "Zapisz nowe hasło",
                        onClick = { submit() },
                        enabled = !isLoading && !isSaving
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
        ) {
            Spacer(Modifier.height(40.dp))
            PasswordInputField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = "Obecne hasło",
                errorText = errors.oldPasswordError
            )
            Spacer(Modifier.height(24.dp))
            PasswordInputField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nowe hasło",
                errorText = errors.newPasswordError
            )
            Spacer(Modifier.height(24.dp))
            PasswordInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Powtórz nowe hasło",
                errorText = errors.confirmPasswordError
            )
        }
    }
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
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
            isError = errorText != null,
            supportingText = {
                if (errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}