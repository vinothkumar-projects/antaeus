package io.pleo.antaeus.core.services

import java.util.Properties

class KafkaService(broker: String) {
    private val props = Properties()

    init {
        props["bootstrap.servers"] = broker
    }
}