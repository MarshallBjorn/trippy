package com.navrotskyi.trippyapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    isSyncing: Boolean = false,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val visible = isOffline || isSyncing || pendingCount > 0

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val bgColor = when {
            isOffline -> Color(0xFFB00020) // czerwony
            isSyncing -> Color(0xFF1976D2) // niebieski
            else -> Color(0xFFF57C00)      // pomarańczowy
        }
        val message = when {
            isOffline && pendingCount > 0 ->
                "Tryb offline · $pendingCount oczekujących zmian"
            isOffline ->
                "Tryb offline · wyświetlane są dane z pamięci podręcznej"
            isSyncing ->
                "Synchronizacja..."
            pendingCount > 0 ->
                "$pendingCount oczekujących zmian"
            else -> ""
        }
        val icon = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudOff

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 8.dp))
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}