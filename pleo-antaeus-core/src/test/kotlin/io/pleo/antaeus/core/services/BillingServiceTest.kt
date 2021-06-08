package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {
    val slot = slot<Transaction.() -> Any>()
    private val invoiceService = mockk<InvoiceService>()
    private val customerService = mockk<CustomerService>()
    private val paymentProvider = mockk<PaymentProvider>()
    private var pendingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(BigDecimal(1000), Currency.EUR),
        status = InvoiceStatus.PENDING
    )
    private var processingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(BigDecimal(1000), Currency.EUR),
        status = InvoiceStatus.PROCESSING
    )
    private val customer = Customer(
        id = 1,
        currency = Currency.USD
    )

    private val billingService = BillingService(
        paymentProvider = paymentProvider,
        invoiceService = invoiceService,
        customerService = customerService
    )

    @Nested
    inner class PaymentNotAttempted() {

        @Test
        fun `payment is not attempted if invoice is not in processing state`() {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every { transaction(any<Database>(), capture(slot)) } answers {
                slot.invoke(mockk())
            }
            every { invoiceService.fetch(any()) } returns pendingInvoice
            every { customerService.fetch(any()) } returns customer

            verify(exactly = 0) { paymentProvider.charge(any()) }
            assertEquals(false, billingService.chargeInvoice(1))
        }

        @Test
        fun `payment is not attempted if currencies does not match`() {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every { transaction(any<Database>(), capture(slot)) } answers {
                slot.invoke(mockk())
            }
            every { invoiceService.fetch(any()) } returns processingInvoice
            every { customerService.fetch(any()) } returns customer

            verify(exactly = 0) { paymentProvider.charge(any()) }
            assertEquals(false, billingService.chargeInvoice(1))
        }
    }
}
