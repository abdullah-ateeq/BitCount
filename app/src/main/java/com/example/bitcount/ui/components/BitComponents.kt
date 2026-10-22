package com.example.bitcount.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitcount.model.BinaryGrouping

@Composable
fun BitCellView(
    bitPosition: Int,
    bitValue: Char,
    showPosition: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isSet = bitValue == '1'
    val containerColor = if (isSet) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSet) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSet) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val cellDescription = "Bit $bitPosition, value $bitValue."

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .semantics { contentDescription = cellDescription }
    ) {
        if (showPosition) {
            Text(
                text = "$bitPosition",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .background(containerColor)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = bitValue.toString(),
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = if (isSet) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun BitRowView(
    magnitudeBinary: String,
    analyzedBitLength: Int,
    showPositions: Boolean,
    grouping: BinaryGrouping,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Bits are displayed from MSB down to LSB (left to right)
    // The bit position at index i (0-based from left of magnitudeBinary) is:
    // bitPosition = analyzedBitLength - 1 - i
    val groupSize = if (grouping == BinaryGrouping.OFF) 0 else grouping.groupSize

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in magnitudeBinary.indices) {
            val bitValue = magnitudeBinary[i]
            val bitPosition = analyzedBitLength - 1 - i

            BitCellView(
                bitPosition = bitPosition,
                bitValue = bitValue,
                showPosition = showPositions
            )

            // Grouping visual spacer (from right / LSB alignment)
            if (groupSize > 0 && i < magnitudeBinary.length - 1) {
                val remainingFromRight = magnitudeBinary.length - 1 - i
                if (remainingFromRight % groupSize == 0) {
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            } else if (i < magnitudeBinary.length - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
