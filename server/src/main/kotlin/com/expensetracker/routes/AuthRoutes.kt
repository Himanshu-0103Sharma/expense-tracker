package com.expensetracker.routes

import com.expensetracker.models.*
import com.expensetracker.plugins.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Application.configureRouting() {
    routing {
        expenseRoutes()
        splitRoutes()
        deviceRoutes()

        route("/auth") {

            // POST /auth/register — create a new account
            post("/register") {
                val request = call.receive<RegisterRequest>()

                // Check if email already taken
                val existing = transaction {
                    Users.select { Users.email eq request.email }.firstOrNull()
                }
                if (existing != null) {
                    call.respond(HttpStatusCode.Conflict, "Email already registered")
                    return@post
                }

                // Hash the password (never store raw)
                val hashed = BCrypt.hashpw(request.password, BCrypt.gensalt())

                // Insert into database
                val userId = transaction {
                    Users.insert {
                        it[email] = request.email
                        it[name] = request.name
                        it[passwordHash] = hashed
                    } get Users.id
                }

                // Generate token and respond
                val token = JwtConfig.generateToken(userId)
                call.respond(HttpStatusCode.Created, AuthResponse(
                    token = token,
                    user = UserInfo(id = userId, email = request.email, name = request.name)
                ))
            }

            // POST /auth/login — get a token with email + password
            post("/login") {
                val request = call.receive<LoginRequest>()

                // Find user by email
                val user = transaction {
                    Users.select { Users.email eq request.email }.firstOrNull()
                }
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                    return@post
                }

                // Verify password against stored hash
                if (!BCrypt.checkpw(request.password, user[Users.passwordHash])) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                    return@post
                }

                // Password correct — generate token
                val token = JwtConfig.generateToken(user[Users.id])
                call.respond(AuthResponse(
                    token = token,
                    user = UserInfo(
                        id = user[Users.id],
                        email = user[Users.email],
                        name = user[Users.name]
                    )
                ))
            }
        }
    }
}
