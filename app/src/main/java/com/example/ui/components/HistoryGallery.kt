package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeneratedImage
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun HistoryGallery(
    history: List<GeneratedImage>,
    onSelectImage: (GeneratedImage) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RECENT CREATIONS (${history.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(history, key = { it.id }) { item ->
                HistoryThumbnailItem(
                    image = item,
                    onClick = { onSelectImage(item) }
                )
            }
        }
    }
}

@Composable
private fun HistoryThumbnailItem(
    image: GeneratedImage,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCardElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("history_item_${image.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (image.bitmap != null) {
            Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = image.prompt,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (image.fallbackDrawableRes != null) {
            Image(
                painter = painterResource(id = image.fallbackDrawableRes),
                contentDescription = image.prompt,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Style Pill in bottom
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = image.style.title,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = image.style.color,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
