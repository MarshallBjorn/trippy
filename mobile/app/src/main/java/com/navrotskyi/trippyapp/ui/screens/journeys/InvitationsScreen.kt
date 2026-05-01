package com.navrotskyi.trippyapp.ui.screens.journeys
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

import com.navrotskyi.trippyapp.ui.viewmodels.InvitationsViewModel

    @Composable
    fun InvitationsScreen(viewModel: InvitationsViewModel = viewModel()) {

        val invitations by viewModel.invitations.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadInvitations()
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Zaproszenia: ${invitations.size}")

            invitations.forEach {
                Text(it.tripName)
            }
        }
    }
