package com.navrotskyi.trippyapp.ui.screens.journeys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navrotskyi.trippyapp.ui.components.EmptyState
import com.navrotskyi.trippyapp.ui.theme.Dimens
import com.navrotskyi.trippyapp.ui.viewmodels.InvitationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(
    onBackClick: () -> Unit,
    onInvitationAccepted: () -> Unit = {},
    viewModel: InvitationsViewModel = viewModel()
) {
    val invitations by viewModel.invitations.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zaproszenia") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.SpaceLg)
        ) {

            if (invitations.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.MailOutline,
                    title = "Brak zaproszeń",
                    description = "Nie masz żadnych oczekujących zaproszeń. Gdy ktoś zaprosi Cię do podróży, pojawi się ono tutaj.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    items(invitations) { invite ->

                        Card {
                            Column(modifier = Modifier.padding(Dimens.SpaceLg)) {

                                Text(
                                    invite.tripName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text("Zaprasza: ${invite.inviterName}")

                                Spacer(modifier = Modifier.height(Dimens.SpaceMd))

                                Row {

                                    Button(
                                        onClick = {
                                            viewModel.accept(invite.tripId, onAccepted = onInvitationAccepted)
                                        }
                                    ) {
                                        Text("Akceptuj")
                                    }

                                    Spacer(modifier = Modifier.width(Dimens.SpaceSm))

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
            }

            error?.let {
                Spacer(modifier = Modifier.height(Dimens.SpaceLg))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
