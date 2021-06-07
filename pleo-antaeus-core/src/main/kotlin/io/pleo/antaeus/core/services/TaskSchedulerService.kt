package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.tasks.InvoiceProcessorTask
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TaskSchedulerService {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun scheduleTasks(): Unit {
        scheduler.scheduleAtFixedRate(
            InvoiceProcessorTask(),
            5000,
            getDelayUntilFirstDayOfMonth(),
            TimeUnit.MILLISECONDS
        )
    }

    // This function returns the number of milliseconds until the next first day of month
    private fun getDelayUntilFirstDayOfMonth(): Long {
        var firstDayOfMonth = Calendar.getInstance()

        if (firstDayOfMonth.get(Calendar.DAY_OF_MONTH) != 1) {
            firstDayOfMonth.add(Calendar.MONTH, 1)
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        }

        return (firstDayOfMonth.timeInMillis - Calendar.getInstance().timeInMillis)
    }
}
