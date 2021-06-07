package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskSchedulerServiceTest {

    private val slot = slot<Long>()
    private val scheduler = mockk<ScheduledExecutorService>()
    private lateinit var taskSchedulerService: TaskSchedulerService

    @BeforeAll
    fun setup() {
        mockkStatic(Executors::class)
        every { Executors.newScheduledThreadPool(any()) } returns scheduler
        taskSchedulerService = TaskSchedulerService()
    }

    @BeforeEach
    fun scheduleTasks() {
        every { scheduler.scheduleAtFixedRate(any(), any(), capture(slot), any()) } returns null
    }

    @Test
    fun `returns correct delay until first of a month`() {
        taskSchedulerService.scheduleTasks()

        // Mock current time so it returns delay as 24 hours
        assertEquals(86400000, slot.captured)
    }

    @Test
    fun `returns delay as zero on first of a month`() {
        taskSchedulerService.scheduleTasks()

        // Mock current time to 1st day of month
        assertEquals(0, slot.captured)
    }
}
