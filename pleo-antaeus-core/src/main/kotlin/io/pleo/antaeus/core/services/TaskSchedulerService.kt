package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.tasks.InvoiceProcessorTask
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


    private fun getDelayUntilFirstDayOfMonth(): Long {
        return 0
        // This function returns the number of milliseconds until the next first day of month
    }
}
