package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioOption
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AspectRatioSelector(
    selectedRatio: AspectRatioOption,
    onRatioSelected: (AspectRatioOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AspectRatio,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ASPECT RATIO",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatioOption.values().forEach { option ->
                val isSelected = selectedRatio == option
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) NeonCyan else StudioBorder,
                    label = "ratioBorder"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) NeonCyan.copy(alpha = 0.15f) else StudioCardElevated,
                    label = "ratioBg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onRatioSelected(option) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                        .testTag("ratio_${option.label.replace(':', '_')}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        RatioIconPreview(option = option, isSelected = isSelected)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonCyan else TextPrimary
                        )
                        Text(
                            text = option.iconDescription,
                            fontSize = 9.sp,
                            color = if (isSelected) TextPrimary.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatioIconPreview(option: AspectRatioOption, isSelected: Boolean) {
    val color = if (isSelected) NeonCyan else TextMuted
    val (w, h) = when (option) {
        AspectRatioOption.SQUARE -> Pair(16.dp, 16.dp)
        AspectRatioOption.PORTRAIT_9_16 -> Pair(11.dp, 18.dp)
        AspectRatioOption.LANDSCAPE_16_9 -> Pair(18.dp, 11.dp)
        AspectRatioOption.PORTRAIT_3_4 -> Pair(13.dp, 17.dp)
    }

    Box(
        modifier = Modifier
            .size(width = w, height = h)
            .border(1.5.dp, color, RoundedCornerShape(2.dp))
    )
}
