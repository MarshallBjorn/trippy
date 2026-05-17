package com.navrotskyi.trippyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TripCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(modifier = Modifier.height(24.dp).width(180.dp))
                ShimmerBox(modifier = Modifier.height(20.dp).width(80.dp), cornerRadius = 12)
            }
            Spacer(Modifier.height(12.dp))
            ShimmerBox(modifier = Modifier.height(14.dp).width(220.dp))
            Spacer(Modifier.height(12.dp))
            ShimmerBox(modifier = Modifier.height(16.dp).width(140.dp))
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(modifier = Modifier.height(14.dp).width(160.dp))
                ShimmerBox(modifier = Modifier.size(20.dp), cornerRadius = 10)
            }
        }
    }
}


@Composable
fun TripNodeSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            ShimmerBox(modifier = Modifier.height(18.dp).width(200.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.height(14.dp).width(120.dp))
            Spacer(Modifier.height(6.dp))
            ShimmerBox(modifier = Modifier.height(14.dp).width(80.dp))
        }
    }
}