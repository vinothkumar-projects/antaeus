plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation("org.apache.kafka:kafka-clients:2.0.0")
    implementation(project(":pleo-antaeus-data"))
    api(project(":pleo-antaeus-models"))
}