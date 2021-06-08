package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    private val logger = KotlinLogging.logger {}

    fun chargeInvoice(id: Int): Boolean {
        var status = false
        transaction {
            try {
                val invoice = invoiceService.fetch(id) //Fetch and check to avoid double charge
                if (invoice.status == InvoiceStatus.PROCESSING) {
                    status = paymentProvider.charge(invoice)
                }
            } catch (e: Exception) {
                logger.error(e) { "Payment failed for invoice ${id}" }
            }
        }

        logger.info { "Payment status of invoice ${id} is: ${status}" }
        return status
    }
}
