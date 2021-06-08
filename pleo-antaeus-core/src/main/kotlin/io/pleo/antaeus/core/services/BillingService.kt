package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
    private val logger = KotlinLogging.logger {}

    fun chargeInvoice(id: Int): Boolean {
        var status = false
        transaction {
            try {
                val invoice = invoiceService.fetch(id) //Fetch and check to avoid double charge
                val customer = customerService.fetch(invoice.customerId)
                // In future, if currency doesn't match we could also send it to another queue for
                // foreign currency payment processing or set invoice to INVALID status
                if (invoice.status == InvoiceStatus.PROCESSING && customer.currency == invoice.amount.currency) {
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
