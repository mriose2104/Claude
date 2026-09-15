package com.misgastos.app.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.misgastos.app.MisGastosApplication
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.DateRangeUtils
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val WEEKLY_WORK_NAME = "weekly_summary"
private const val MONTHLY_WORK_NAME = "monthly_summary"

class WeeklySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as MisGastosApplication).container
        val settings = container.budgetRepository.observeAlertSettings().first()
        if (!settings.notificationsEnabled || !settings.weeklySummaryEnabled) return Result.success()
        val stats = container.expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisWeek)).first()
        container.alertNotifier.notifyWeeklySummary(stats.total)
        return Result.success()
    }
}

class MonthlySummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as MisGastosApplication).container
        val settings = container.budgetRepository.observeAlertSettings().first()
        if (!settings.notificationsEnabled || !settings.monthlySummaryEnabled) return Result.success()
        val stats = container.expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisMonth)).first()
        container.alertNotifier.notifyMonthlySummary(stats.total)
        return Result.success()
    }
}

/** Schedules or cancels the periodic summary notifications based on user preference. */
object SummaryScheduler {

    fun applyWeekly(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(WEEKLY_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        } else {
            workManager.cancelUniqueWork(WEEKLY_WORK_NAME)
        }
    }

    fun applyMonthly(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            // WorkManager has no native monthly interval; 30 days approximates a monthly cadence.
            val request = PeriodicWorkRequestBuilder<MonthlySummaryWorker>(30, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(MONTHLY_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        } else {
            workManager.cancelUniqueWork(MONTHLY_WORK_NAME)
        }
    }
}
