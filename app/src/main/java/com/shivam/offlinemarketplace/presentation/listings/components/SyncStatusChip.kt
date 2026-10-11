package com.shivam.offlinemarketplace.presentation.listings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivam.offlinemarketplace.R
import com.shivam.offlinemarketplace.domain.model.SyncStatus

@Composable
fun SyncStatusChip(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        SyncStatus.PENDING -> Triple(
            Color(0xFFFFF3CD), // Amber / light yellow
            Color(0xFF856404), // Dark amber
            stringResource(R.string.status_pending)
        )
        SyncStatus.SYNCED -> Triple(
            Color(0xFFD4EDDA), // Light green
            Color(0xFF155724), // Dark green
            stringResource(R.string.status_synced)
        )
        SyncStatus.FAILED -> Triple(
            Color(0xFFF8D7DA), // Light red
            Color(0xFF721C24), // Dark red
            stringResource(R.string.status_failed)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true, name = "Sync Status Chips Preview")
@Composable
fun SyncStatusChipPreview() {
    MaterialTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            SyncStatusChip(status = SyncStatus.PENDING)
            Spacer(modifier = Modifier.width(8.dp))
            SyncStatusChip(status = SyncStatus.SYNCED)
            Spacer(modifier = Modifier.width(8.dp))
            SyncStatusChip(status = SyncStatus.FAILED)
        }
    }
}
