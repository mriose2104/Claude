package com.misgastos.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val BUDGET_CHANNEL_ID = "budget_alerts"
    const val SUMMARY_CHANNEL_ID = "period_summaries"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                BUDGET_CHANNEL_ID,
                "Alertas de presupuesto",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos cuando un gasto o presupuesto llega a su límite" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                SUMMARY_CHANNEL_ID,
                "Resúmenes periódicos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Resumen semanal y mensual de gastos" }
        )
    }
}
