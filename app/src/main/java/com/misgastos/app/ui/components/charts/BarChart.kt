package com.misgastos.app.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class BarEntry(val label: String, val value: Double, val highlighted: Boolean = false)

@Composable
fun BarChart(
    data: List<BarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    highlightColor: Color = MaterialTheme.colorScheme.tertiary,
    maxBarHeight: androidx.compose.ui.unit.Dp = 130.dp,
    barWidth: androidx.compose.ui.unit.Dp = 32.dp,
    valueLabel: (Double) -> String = { "$${"%.0f".format(it)}" }
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 0.0

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(data) { entry ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(barWidth + 16.dp)) {
                if (entry.value > 0) {
                    Text(
                        valueLabel(entry.value),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val fraction = if (maxValue > 0) (entry.value / maxValue).toFloat().coerceIn(0.02f, 1f) else 0.02f
                Box(
                    modifier = Modifier
                        .height((maxBarHeight * fraction).coerceAtLeast(4.dp))
                        .width(barWidth)
                        .background(
                            if (entry.highlighted) highlightColor else barColor,
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Text(
                    entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
