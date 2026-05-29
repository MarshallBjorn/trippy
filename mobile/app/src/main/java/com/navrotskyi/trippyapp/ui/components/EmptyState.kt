package com.navrotskyi.trippyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.navrotskyi.trippyapp.ui.theme.Dimens

/**
 * Spójny widok "pustego stanu" dla list, które nie mają jeszcze żadnych elementów.
 * Składa się z ikony w kółku, tytułu, opisu zachęcającego do akcji oraz opcjonalnego przycisku.
 *
 * @param icon ikona ilustrująca pustą listę
 * @param title krótki, pogrubiony nagłówek
 * @param description tekst zachęcający użytkownika do podjęcia działania
 * @param actionLabel etykieta przycisku akcji (jeśli null – przycisk się nie pojawia)
 * @param onActionClick akcja wywoływana po kliknięciu przycisku
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(Dimens.ScreenPadding)
                    .size(48.dp)
            )
        }

        Spacer(Modifier.height(Dimens.SpaceXl))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(Dimens.SpaceSm))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(Modifier.height(Dimens.SpaceXl))
            TrippyButton(
                text = actionLabel,
                onClick = onActionClick
            )
        }
    }
}
