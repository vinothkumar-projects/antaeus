package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskSchedulerServiceTest {

    private val dal = mockk<AntaeusDal>()
    private val invoiceService = InvoiceService(dal = dal)
    private val billingService = BillingService(paymentProvider = getPaymentProvider())
    private val kafkaService = mockk<KafkaService>()
    private val slot = slot<Long>()
    private val scheduler = mockk<ScheduledExecutorService>()
    private val timezone = TimeZone.getTimeZone("UTC")
    private val calendarOne = Calendar.getInstance(timezone)
    private val calendarTwo = Calendar.getInstance(timezone)

    private lateinit var taskSchedulerService: TaskSchedulerService

    @BeforeAll
    fun setup() {
        mockkStatic(Executors::class)
        every { Executors.newScheduledThreadPool(any()) } returns scheduler
        taskSchedulerService = TaskSchedulerService(
            invoiceService = invoiceService,
            billingService = billingService,
            kafkaService = kafkaService
        )
    }

    @BeforeEach
    fun scheduleTasks() {
        mockkStatic(Calendar::class)
        every { scheduler.scheduleAtFixedRate(any(), any(), capture(slot), any()) } returns null
        every { scheduler.scheduleWithFixedDelay(any(), any(), any(), any()) } returns null
    }

    @Test
    fun `returns correct delay until first of a month`() {
        // set date to Jan 31st, 1970
        calendarOne.timeInMillis = 0
        calendarTwo.timeInMillis = 0
        calendarOne.add(Calendar.DAY_OF_MONTH, 30)
        calendarTwo.add(Calendar.DAY_OF_MONTH, 30)
        every { Calendar.getInstance() } returnsMany listOf(calendarOne, calendarTwo)

        taskSchedulerService.scheduleTasks()

        // Returns delay as 24 hours
        assertEquals(86400000, slot.captured)
    }

    @Test
    fun `returns delay as zero on first of a month`() {
        // set date to Jan 1st, 1970 (epoch)
        calendarOne.timeInMillis = 0
        calendarTwo.timeInMillis = 0
        every { Calendar.getInstance() } returnsMany listOf(calendarOne, calendarTwo)
        taskSchedulerService.scheduleTasks()

        assertEquals(0, slot.captured)
    }

    fun getPaymentProvider(): PaymentProvider {
        return object : PaymentProvider {
            override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
            }
        }
    }
}
