package com.expensetracker.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

// Teaches the server: "when you receive JSON, convert it to Kotlin objects"
// and "when you send a response, convert Kotlin objects to JSON"
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true       // makes JSON readable in logs
            isLenient = false        // strict parsing — rejects malformed JSON
            ignoreUnknownKeys = true // if client sends extra fields, don't crash
        })
    }
}
