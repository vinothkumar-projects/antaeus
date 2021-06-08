package io.pleo.antaeus.core.services

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*
import java.util.concurrent.Future

class KafkaService(broker: String) {
    private val producer = createProducer(broker)
    private val consumer = createConsumer(broker)

    private fun createProducer(broker: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = broker
        props["acks"] = "all"
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    private fun createConsumer(broker: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = broker
        props["key.deserializer"] = StringSerializer::class.java
        props["value.deserializer"] = StringSerializer::class.java
        return KafkaConsumer<String, String>(props)
    }

    fun sendToTopic(topic: String, key: String, value: String): Future<RecordMetadata>? {
        return producer.send(ProducerRecord(topic, key, value))
    }

    fun subscribeToTopics(topics: String) {
        consumer.subscribe(listOf(topics))
    }

    fun consumeFromTopic(topic: String): ConsumerRecords<String, String>? {
        return consumer.poll(Duration.ofMillis(5000))
    }
}