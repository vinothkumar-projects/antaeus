package io.pleo.antaeus.core.tasks

import java.util.*

class InvoiceProcessorTask : TimerTask() {
    override fun run() {
        // Fetch all pending invoices
        // Process one by one (db lock?)
        // Charge customer
        // Move invoice state to paid
        println("invoice processor task")
    }
}
