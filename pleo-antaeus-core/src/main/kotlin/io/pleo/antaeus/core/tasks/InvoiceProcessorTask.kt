package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.exceptions.NetworkException
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
        // how to make sure its not charged twice in parallel
        val pendingInvoices: List<Invoice> = invoiceService.fetchAllByStatus(InvoiceStatus.PENDING)
        // Process one by one (db lock?)
        pendingInvoices.forEach {
            try {
                var status = billingService.processInvoice(it)

                if (status) {
                    invoiceService.changeStatus(it.id, InvoiceStatus.PAID)
                    println("invoice charged successfully")
                } else {
                    println("invoice is not charged successfully")
                    // retry queue
                }
            } catch (e: NetworkException) {
                // retry later?
            }
        }
    }
}
