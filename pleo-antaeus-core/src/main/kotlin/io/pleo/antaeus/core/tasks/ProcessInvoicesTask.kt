package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
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
            transaction {
                val invoice = invoiceService.fetch(it.key().toInt())
                if (invoice.status == InvoiceStatus.PROCESSING) {
                    val status = billingService.processInvoice(invoice)
                    if (status) {
                        invoiceService.changeStatus(invoice.id, InvoiceStatus.PAID)
                        println("Customer is charged successfully for invoice ${invoice.id}")
                    } else {
                        //retry queue
                    }
                } else {
                    println("invoice status not matching ${invoice.status}")
                }
            }
        }
        println("over over over ")
    }
}
