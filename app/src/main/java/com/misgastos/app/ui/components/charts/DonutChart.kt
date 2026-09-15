package com.misgastos.app.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class DonutEntry(val label: String, val value: Double, val color: Color, val icon: String = "")

@Composable
fun DonutChart(
    data: List<DonutEntry>,
    modifier: Modifier = Modifier,
    diameter: androidx.compose.ui.unit.Dp = 180.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 28.dp,
    centerLabel: String? = null
) {
    val total = data.sumOf { it.value }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(diameter), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(diameter)) {
                if (total <= 0) return@Canvas
                var startAngle = -90f
                val stroke = Stroke(width = strokeWidth.toPx())
                data.forEach { entry ->
                    val sweep = (entry.value / total * 360.0).toFloat()
                    drawArc(
                        color = entry.color,
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtLeast(0.5f),
                        useCenter = false,
                        style = stroke
                    )
                    startAngle += sweep
                }
            }
            if (centerLabel != null) {
                Text(centerLabel, style = MaterialTheme.typography.titleSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.forEach { entry ->
                val percent = if (total > 0) entry.value / total * 100.0 else 0.0
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(entry.color, CircleShape))
                    Text(
                        "${entry.icon} ${entry.label}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text("%.1f%%".format(percent), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
