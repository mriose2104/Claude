package com.misgastos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.misgastos.app.data.local.relation.ExpenseFull
import com.misgastos.app.domain.util.CurrencyFormatter
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

@Composable
fun ExpenseRow(
    expense: ExpenseFull,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showDate: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(runCatching { Color(android.graphics.Color.parseColor(expense.categoryColor)) }.getOrDefault(MaterialTheme.colorScheme.primary).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(expense.categoryIcon, style = MaterialTheme.typography.titleMedium)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.establishmentName, style = MaterialTheme.typography.titleSmall)
            val dateLabel = if (showDate) {
                val dayName = expense.expense.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                "${dayName.replaceFirstChar { it.uppercase() }} ${expense.expense.date.dayOfMonth} · "
            } else ""
            Text(
                "$dateLabel${expense.categoryName} · ${expense.paymentMethodName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!expense.expense.note.isNullOrBlank()) {
                Text(
                    expense.expense.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(CurrencyFormatter.format(expense.expense.amount), style = MaterialTheme.typography.titleSmall)
            Text(
                expense.expense.time.format(timeFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
