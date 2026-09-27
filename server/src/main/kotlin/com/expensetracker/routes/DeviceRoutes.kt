package com.expensetracker.routes

import com.expensetracker.models.DeviceTokens
import com.expensetracker.models.RegisterDeviceRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.deviceRoutes() {
    authenticate("auth-jwt") {
        post("/register-device") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("userId").asInt()

            val request = call.receive<RegisterDeviceRequest>()

            transaction {
                DeviceTokens.deleteWhere {
                    (DeviceTokens.userId eq userId) or (DeviceTokens.token eq request.token)
                }
                DeviceTokens.insert {
                    it[DeviceTokens.userId] = userId
                    it[token] = request.token
                    it[createdAt] = System.currentTimeMillis()
                }
            }

            call.respond(HttpStatusCode.OK, "Device registered")
        }
    }
}
