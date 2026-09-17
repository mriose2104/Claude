package com.misgastos.app.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.misgastos.app.domain.util.CurrencyFormatter

/** Posts local notifications for budget/spending alerts. All checks are evaluated right after an
 *  expense is saved, or from the periodic summary worker — no server round-trip needed. */
class AlertNotifier(private val context: Context) {

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notify(id: Int, channelId: String, title: String, message: String) {
        if (!canNotify()) return
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    fun notifyDailyLimitExceeded(spentToday: Double, limit: Double) = notify(
        id = 1001,
        channelId = NotificationChannels.BUDGET_CHANNEL_ID,
        title = "🔴 Límite diario superado",
        message = "Hoy llevas ${CurrencyFormatter.format(spentToday)} de un límite de ${CurrencyFormatter.format(limit)}."
    )

    fun notifyCategoryBudgetWarning(categoryName: String, percentUsed: Int) = notify(
        id = 1002 + categoryName.hashCode(),
        channelId = NotificationChannels.BUDGET_CHANNEL_ID,
        title = "🟡 Presupuesto de $categoryName al $percentUsed%",
        message = "Estás cerca de alcanzar el presupuesto asignado a $categoryName este mes."
    )

    fun notifyCategoryBudgetExceeded(categoryName: String) = notify(
        id = 1003 + categoryName.hashCode(),
        channelId = NotificationChannels.BUDGET_CHANNEL_ID,
        title = "🔴 Presupuesto de $categoryName excedido",
        message = "Ya superaste el presupuesto mensual asignado a $categoryName."
    )

    fun notifyMonthlyBudgetWarning(percentUsed: Int) = notify(
        id = 1004,
        channelId = NotificationChannels.BUDGET_CHANNEL_ID,
        title = "🟡 Presupuesto mensual al $percentUsed%",
        message = "Ya usaste el $percentUsed% de tu presupuesto mensual."
    )

    fun notifyMonthlyBudgetExceeded() = notify(
        id = 1005,
        channelId = NotificationChannels.BUDGET_CHANNEL_ID,
        title = "🔴 Presupuesto mensual excedido",
        message = "Superaste el presupuesto que definiste para este mes."
    )

    fun notifyWeeklySummary(total: Double) = notify(
        id = 2001,
        channelId = NotificationChannels.SUMMARY_CHANNEL_ID,
        title = "Resumen semanal",
        message = "Gastaste ${CurrencyFormatter.format(total)} esta semana."
    )

    fun notifyMonthlySummary(total: Double) = notify(
        id = 2002,
        channelId = NotificationChannels.SUMMARY_CHANNEL_ID,
        title = "Resumen mensual",
        message = "Gastaste ${CurrencyFormatter.format(total)} este mes."
    )
}
