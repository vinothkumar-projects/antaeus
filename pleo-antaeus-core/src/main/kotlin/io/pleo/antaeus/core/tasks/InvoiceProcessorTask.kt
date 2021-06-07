package io.pleo.antaeus.core.tasks

import java.util.*

class InvoiceProcessorTask : TimerTask() {
    override fun run() {
        println("invoice processor task")
    }
}
