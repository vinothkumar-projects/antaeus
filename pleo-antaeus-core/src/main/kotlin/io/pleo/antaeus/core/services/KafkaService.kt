package io.pleo.antaeus.core.services

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*
import java.util.concurrent.Future

class KafkaService(
    private val broker: String,
    private val processInvoiceTopic: String
) {
    private val producer = createProducer()
    private val processInvoicesTopicConsumer = createConsumer()

    init {
        subscribeToTopics()
    }

    private fun createProducer(): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = broker
        props["acks"] = "all"
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    private fun createConsumer(): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = broker
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        props["group.id"] = "antaeus-consumer"
        return KafkaConsumer<String, String>(props)
    }

    private fun subscribeToTopics() {
        processInvoicesTopicConsumer.subscribe(listOf(processInvoiceTopic))
    }

    private fun sendToTopic(topic: String, key: String, value: String): Future<RecordMetadata>? {
        return producer.send(ProducerRecord(topic, key, value))
    }

    private fun consumeEventsFromTopic(consumer: Consumer<String, String>): ConsumerRecords<String, String>? {
        return consumer.poll(Duration.ofMillis(5000))
    }

    fun sendToProcessInvoicesTopic(key: String, value: String): Future<RecordMetadata>? {
        return sendToTopic(processInvoiceTopic, key, value)
    }

    fun consumeFromProcessInvoicesTopic(): ConsumerRecords<String, String>? {
        return consumeEventsFromTopic(processInvoicesTopicConsumer)
    }
}