plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
}

group = "com.expensetracker"
version = "0.0.1"

application {
    mainClass.set("com.expensetracker.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.postgresql:postgresql:42.7.1")

    // Password hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // Firebase Admin SDK (for push notifications)
    implementation("com.google.firebase:firebase-admin:9.2.0") {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}
