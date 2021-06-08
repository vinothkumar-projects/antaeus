package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SchedulePendingInvoicesTask(
    private val invoiceService: InvoiceService,
    private val kafkaService: KafkaService
) : TimerTask() {

    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Running ${javaClass.simpleName}" }

        val pendingInvoices: List<Invoice> = invoiceService.fetchAllByStatus(InvoiceStatus.PENDING)
        pendingInvoices.forEach {

            val status: Future<RecordMetadata> =
                kafkaService.sendToProcessInvoicesTopic(it.id.toString(), it.amount.toString())!!

            try {
                val recordMetaData: RecordMetadata = status.get(10, TimeUnit.SECONDS)
                logger.info { "Invoice ${it.id} sent to topic ${recordMetaData.topic()}" }
                invoiceService.changeStatus(it.id, InvoiceStatus.PROCESSING)
            } catch (e: Exception) {
                logger.error(e) { "Failed to schedule invoice" }
            }
        }
    }
}
