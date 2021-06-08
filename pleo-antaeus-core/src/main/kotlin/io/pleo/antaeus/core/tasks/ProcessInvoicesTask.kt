package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import java.util.*
import org.jetbrains.exposed.sql.transactions.transaction

class ProcessInvoicesTask(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
    private val kafkaService: KafkaService
) : TimerTask() {

    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Running ${javaClass.simpleName}" }

        val records = kafkaService.consumeFromProcessInvoicesTopic()
        records?.iterator()?.forEach {
            when (billingService.chargeInvoice(it.key().toInt())) {
                true -> changeStatusToPaid(it.key().toInt())
                else -> sendRetryEvent(invoiceService.fetch(it.key().toInt()))
            }
        }
    }

    private fun changeStatusToPaid(id: Int) {
        invoiceService.changeStatus(id, InvoiceStatus.PAID)
        logger.info { "Payment succeeded for Invoice ${id}" }
    }

    private fun sendRetryEvent(invoice: Invoice) {
        kafkaService.sendToRetryFailedInvoicesTopic(invoice.id.toString(), invoice.amount.toString())
        logger.error { "Payment failed for Invoice ${invoice.id}. Sending to retry queue." }
    }
}
