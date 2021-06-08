package io.pleo.antaeus.core.tasks

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.KafkaService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Future

class SchedulePendingInvoicesTaskTest {
    private val invoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(BigDecimal(1000), Currency.EUR),
        status = InvoiceStatus.PENDING
    )
    private val slot = slot<InvoiceStatus>()
    private val invoiceService = mockk<InvoiceService>()
    private val kafkaService = mockk<KafkaService>()
    private val future = mockk<Future<RecordMetadata>>()
    private val recordMetaData = mockk<RecordMetadata>()

    private val schedulePendingInvoicesTask = SchedulePendingInvoicesTask(
        invoiceService = invoiceService,
        kafkaService = kafkaService
    )

    @Nested
    inner class InvoicesScheduled() {

        @Test
        fun `All valid pending invoices are scheduled for processing`() {
            every { invoiceService.fetchAllByStatus(any()) } returns listOf(invoice)
            every { invoiceService.changeStatus(any(), capture(slot)) } returns invoice
            every { kafkaService.sendToProcessInvoicesTopic(any(), any()) } returns future
            every { future.get(any(), any()) } returns recordMetaData
            every { recordMetaData.topic() } returns "random"

            schedulePendingInvoicesTask.run()
            assertEquals(InvoiceStatus.PROCESSING, slot.captured)
        }
    }
}
