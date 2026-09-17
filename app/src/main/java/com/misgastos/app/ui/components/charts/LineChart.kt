package com.misgastos.app.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class LinePoint(val label: String, val value: Double)

@Composable
fun LineChart(
    data: List<LinePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    height: androidx.compose.ui.unit.Dp = 160.dp
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0.0).let { if (it <= 0) 1.0 else it }
    val minValue = (data.minOfOrNull { it.value } ?: 0.0).coerceAtMost(0.0)
    val range = (maxValue - minValue).let { if (it == 0.0) 1.0 else it }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            if (data.size < 2) return@Canvas
            val stepX = size.width / (data.size - 1)
            val points = data.mapIndexed { index, point ->
                val normalized = ((point.value - minValue) / range).toFloat()
                Offset(x = index * stepX, y = size.height - (normalized * size.height))
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            points.forEach { offset ->
                drawCircle(color = lineColor, radius = 7f, center = offset)
                drawCircle(color = Color.White, radius = 3f, center = offset)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { point ->
                Text(
                    point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
