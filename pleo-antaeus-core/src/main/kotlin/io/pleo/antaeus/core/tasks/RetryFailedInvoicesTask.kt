package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import java.util.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class RetryFailedInvoicesTask(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
    private val kafkaService: KafkaService
) : TimerTask() {

    override fun run() {
        val records = kafkaService.consumeFromRetryFailedInvoicesTopic()
        records?.iterator()?.forEach {
            if (isOlderThanAday(it.timestamp())) {
                when (billingService.chargeInvoice(it.key().toInt())) {
                    true -> invoiceService.changeStatus(it.key().toInt(), InvoiceStatus.PAID)
                    else -> sendDeadEvent(invoiceService.fetch(it.key().toInt()))
                }
            }
        }
    }

    private fun isOlderThanAday(timestamp: Long): Boolean {
        val eventAge = System.currentTimeMillis() - timestamp
        return (eventAge > TimeUnit.HOURS.toMillis(24))
    }

    private fun sendDeadEvent(invoice: Invoice) {
        kafkaService.sendToDeadInvoicesTopic(invoice.id.toString(), invoice.amount.toString())
    }
}
