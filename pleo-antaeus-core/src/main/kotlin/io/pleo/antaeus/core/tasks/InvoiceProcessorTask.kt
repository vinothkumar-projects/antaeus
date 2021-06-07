package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import java.util.*

class InvoiceProcessorTask(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService
) : TimerTask() {
    override fun run() {
        val pendingInvoices: List<Invoice> = invoiceService.fetchAllByStatus(InvoiceStatus.PENDING)
        // Process one by one (db lock?)
        pendingInvoices.forEach {
            // handle exceptions
            var status = billingService.processInvoice(it)
            if (status) {
                println("invoice charged successfully")
                // change status to paid
            } else {
                println("invoice is not charged successfully")
                // retry queue
            }
        }
    }
}
