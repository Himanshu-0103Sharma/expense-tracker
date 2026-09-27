package com.expensetracker

import com.expensetracker.models.*
import com.expensetracker.plugins.configureSecurity
import com.expensetracker.plugins.configureSerialization
import com.expensetracker.routes.configureRouting
import com.expensetracker.services.NotificationService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    val databaseUrl = System.getenv("DATABASE_URL")

    if (databaseUrl != null) {
        Database.connect(
            url = databaseUrl,
            driver = "org.postgresql.Driver"
        )
    } else {
        Database.connect("jdbc:sqlite:./expense-tracker.db", driver = "org.sqlite.JDBC")
    }

    transaction {
        SchemaUtils.create(Users, PersonalExpenses, SplitExpenses, SplitParticipants, DeviceTokens)
    }

    NotificationService.initialize()

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port) {
        configureSerialization()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}
