package com.navrotskyi.trippyapp.ui.screens.journeys

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.ui.components.TrippyButton
import com.navrotskyi.trippyapp.ui.components.TrippyTextField
import com.navrotskyi.trippyapp.ui.viewmodels.InviteState
import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import com.navrotskyi.trippyapp.models.TripParticipantDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteParticipantScreen(
    tripId: String,
    viewModel: TripViewModel,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val inviteState by viewModel.inviteState.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(inviteState) {
        when (inviteState) {
            is InviteState.Success -> {
                Toast.makeText(context, "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show()
                email = ""
                viewModel.resetInviteState()

            }
            is InviteState.Error -> {
                val errorMessage = (inviteState as InviteState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetInviteState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zaproś uczestnika") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wpisz adres e-mail osoby, którą chcesz zaprosić do podróży.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TrippyTextField(
                value = email,
                onValueChange = { email = it },
                label = "Adres e-mail",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Obsługa ładowania na przycisku
            if (inviteState is InviteState.Loading) {
                CircularProgressIndicator()
            } else {
                TrippyButton(
                    text = "Wyślij zaproszenie",
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.inviteParticipant(tripId, email)
                        } else {
                            Toast.makeText(context, "Podaj adres e-mail", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Obecni uczestnicy:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))


            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                participants.forEach { participant ->
                    ParticipantCard(participant = participant)
                }
            }
        }
    }
}