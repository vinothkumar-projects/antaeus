package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.tasks.ProcessInvoicesTask
import io.pleo.antaeus.core.tasks.SchedulePendingInvoicesTask
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TaskSchedulerService(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
    private val kafkaService: KafkaService
) {

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun scheduleTasks() {
        schedulePendingInvoices()
        processInvoices()
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

    private fun schedulePendingInvoices() {
        scheduler.scheduleAtFixedRate(
            SchedulePendingInvoicesTask(
                invoiceService = invoiceService,
                kafkaService = kafkaService
            ),
            5000,
            getDelayUntilFirstDayOfMonth(),
            TimeUnit.MILLISECONDS
        )
    }

    private fun processInvoices() {
        scheduler.scheduleWithFixedDelay(
            ProcessInvoicesTask(
                invoiceService = invoiceService,
                billingService = billingService,
                kafkaService = kafkaService
            ),
            10000,
            5000,
            TimeUnit.MILLISECONDS
        )
    }
}
