package com.expensetracker.services

import com.expensetracker.models.DeviceTokens
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.io.File

object NotificationService {

    private var initialized = false

    fun initialize() {
        val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")
        val stream = if (credentialsJson != null) {
            ByteArrayInputStream(credentialsJson.toByteArray())
        } else {
            val file = File("firebase-service-account.json")
            if (!file.exists()) {
                println("Firebase credentials not found — push notifications disabled")
                return
            }
            file.inputStream()
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .build()
        FirebaseApp.initializeApp(options)
        initialized = true
    }

    fun notifyUsers(userIds: List<Int>, title: String, body: String) {
        if (!initialized) return
        val tokens = transaction {
            DeviceTokens.select { DeviceTokens.userId inList userIds }
                .map { it[DeviceTokens.token] }
        }

        tokens.forEach { token ->
            try {
                val message = Message.builder()
                    .setToken(token)
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build()
                    )
                    .build()
                FirebaseMessaging.getInstance().send(message)
            } catch (e: Exception) {
                println("Failed to send notification to $token: ${e.message}")
            }
        }
    }
}
