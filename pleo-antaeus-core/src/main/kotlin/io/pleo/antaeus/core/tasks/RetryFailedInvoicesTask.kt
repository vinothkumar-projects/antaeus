package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import java.util.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class RetryFailedInvoicesTask(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
    private val kafkaService: KafkaService
) : TimerTask() {

    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Running ${javaClass.simpleName}" }

        val records = kafkaService.consumeFromRetryFailedInvoicesTopic()
        records?.iterator()?.forEach {
            if (isOlderThanAday(it.timestamp())) {
                when (billingService.chargeInvoice(it.key().toInt())) {
                    true -> changeStatusToPaid(it.key().toInt())
                    else -> sendDeadEvent(invoiceService.fetch(it.key().toInt()))
                }
            }
        }
    }

    private fun isOlderThanAday(timestamp: Long): Boolean {
        val eventAge = System.currentTimeMillis() - timestamp
        return (eventAge > TimeUnit.HOURS.toMillis(24))
    }

    private fun changeStatusToPaid(id: Int) {
        invoiceService.changeStatus(id, InvoiceStatus.PAID)
        logger.info { "Payment succeeded for Invoice ${id}" }
    }

    private fun sendDeadEvent(invoice: Invoice) {
        kafkaService.sendToDeadInvoicesTopic(invoice.id.toString(), invoice.amount.toString())
        logger.error { "Payment failed for Invoice ${invoice.id}. Sending to dead letter queue." }
    }
}
