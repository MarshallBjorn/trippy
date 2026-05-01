package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navrotskyi.trippyapp.ui.viewmodels.InvitationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(
    viewModel: InvitationsViewModel = viewModel()
) {
    val invitations by viewModel.invitations.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Zaproszenia") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            if (invitations.isEmpty()) {
                Text("Brak zaproszeń")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(invitations) { invite ->

                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                invite.tripName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text("Zaprasza: ${invite.inviterName}")

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {

                                Button(
                                    onClick = {
                                        viewModel.accept(invite.tripId)
                                    }
                                ) {
                                    Text("Akceptuj")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        viewModel.reject(invite.tripId)
                                    }
                                ) {
                                    Text("Odrzuć")
                                }
                            }
                        }
                    }
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}