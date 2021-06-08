## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Summary of work

1. A [scheduler](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/services/TaskSchedulerService.kt) is created, that will trigger a task on first of every month.
2. This [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/SchedulePendingInvoicesTask.kt) will send the invoices in pending state for processing to a Kafka topic `process-invoices`. Also it will change the invoice state to `PROCESSING`.
3. Another [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/ProcessInvoicesTask.kt) is scheduled to run every 5 seconds to consume and process the invoices present in the Kafka topic `process-invoices`.
4. If payment is successful then invoice status is changed to `PAID`.
5. Incase of failed payments, it is added to a new topic `retry-failed-invoices`.
6. Another [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/RetryFailedInvoicesTask.kt) is scheduled to run every 5 seconds to consume events from `retry-failed-invoices` and if the last payment attempt is more than 24 hours, then it will retry the payment.
7. If the payment fails again, then a new event is sent to `dlq-invoices` topic.
8. We can introduce any number of retry strategies by introducing new topics in the future incase of payment failures. 
9. Dead letter queue(DLQ) can be either used to send notifications to customers for adding new payment method OR can also be used to manually verify the invoices and errors.
10. Kafka is used because it offers real time processing and reliability of events. At a later point of time, we could also use the events log for audit purposes or do analytics to understand payment failures. We can also try out different retry strategies based on this analysis.

## Architecture

![architecture](https://github.com/vinothkumar-projects/antaeus/blob/master/docs/architecture.png)


## Instructions to run

Requirements:
- \>= Java 11 environment
- Kakfa cluster

### Kafka

- Use [this](https://github.com/vinothkumar-projects/antaeus/blob/master/scripts/docker-compose.yml) docker-compose file to create a kafka cluster locally.
- Use [this](https://docs.docker.com/compose/install/) link to install docker-compose.

Then run the below commands,
```
cd scripts
```
```
docker-compose up -d
```

This starts the kafka cluster and the bootstrap server is available in port 29092.

Following topics need to be created in this cluster,

* `process-invoices`
* `retry-failed-invoices`
* `dlq-invoices`

This [link](https://kafka.apache.org/quickstart) provides scripts to create topics on the cluster.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### Main Libraries and dependencies
* [Kafka](https://kafka.apache.org) - For storing and retreiving events
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine
