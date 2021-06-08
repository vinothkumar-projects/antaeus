package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import java.util.*
import org.jetbrains.exposed.sql.transactions.transaction

class ProcessInvoicesTask(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService,
    private val kafkaService: KafkaService
) : TimerTask() {

    override fun run() {
        val records = kafkaService.consumeFromProcessInvoicesTopic()
        records?.iterator()?.forEach {
            when (billingService.chargeInvoice(it.key().toInt())) {
                true -> invoiceService.changeStatus(it.key().toInt(), InvoiceStatus.PAID)
                else -> sendRetryEvent(invoiceService.fetch(it.key().toInt()))
            }
        }
    }

    private fun sendRetryEvent(invoice: Invoice) {
        kafkaService.sendToRetryFailedInvoicesTopic(invoice.id.toString(), invoice.amount.toString())
    }
}
