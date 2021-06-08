## Summary of challenge

1. A [scheduler](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/services/TaskSchedulerService.kt) is created, that will trigger a task on first of every month.
2. This [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/SchedulePendingInvoicesTask.kt) will send the invoices in pending state for processing to a Kafka topic `process-invoices`. Also it will change the invoice state to `PROCESSING`.
3. Another [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/ProcessInvoicesTask.kt) is scheduled to run every 5 seconds to consume and process the invoices present in the Kafka topic `process-invoices`.
4. If payment is successful then invoice status is changed to `PAID`.
5. Incase of failed payments, it is added to a new topic `retry-failed-invoices`.
6. Another [task](https://github.com/vinothkumar-projects/antaeus/blob/master/pleo-antaeus-core/src/main/kotlin/io/pleo/antaeus/core/tasks/RetryFailedInvoicesTask.kt) is scheduled to run every 5 seconds to consume events from `retry-failed-invoices` and if the last payment attempt is more than 24 hours, then it will retry the payment.
7. If the payment fails again, then a new event is sent to `dlq-invoices` topic.
8. We can introduce any number of retry strategies by introducing new topics in the future incase of payment failures. 
9. Dead letter queue(DLQ) can be either used to send notifications to customers for adding new payment method OR can also be used to manually verify the invoices and errors.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

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

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
