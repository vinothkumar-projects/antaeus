package io.pleo.antaeus.core.tasks

import io.mockk.mockk
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class InvoiceProcessorTaskTest {
    private val dal = mockk<AntaeusDal>()
    private val customerOne = dal.createCustomer(Currency.EUR)!!
    private val customerTwo = dal.createCustomer(Currency.DKK)!!
    private val invoiceOne =
        dal.createInvoice(Money(BigDecimal(1000), Currency.EUR), customerOne, InvoiceStatus.PENDING)
    private val invoiceTwo = dal.createInvoice(Money(BigDecimal(555), Currency.DKK), customerTwo, InvoiceStatus.PENDING)
    private val invoiceThree =
        dal.createInvoice(Money(BigDecimal(245), Currency.USD), customerTwo, InvoiceStatus.PENDING)
    private val invoiceFour = dal.createInvoice(Money(BigDecimal(100), Currency.EUR), customerOne, InvoiceStatus.PAID)

    @Nested
    inner class CustomerAccountCharged() {

        @Test
        fun `All valid pending invoices are charged successfully`() {
        }
    }

    @Nested
    inner class CurrencyMismatch() {

        @Test
        fun `Invoice not charged if currency is not matching`() {
        }
    }

    @Nested
    inner class FailedPayment() {
    }
}
