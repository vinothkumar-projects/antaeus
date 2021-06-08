package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.jetbrains.exposed.sql.transactions.transaction

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    fun chargeInvoice(id: Int): Boolean {
        return transaction {
            val invoice = invoiceService.fetch(id) //Fetch and check to avoid double charge
            if (invoice.status == InvoiceStatus.PROCESSING) {
                paymentProvider.charge(invoice)
            } else {
                false
            }
        }
    }
}
