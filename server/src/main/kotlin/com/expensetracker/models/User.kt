package com.expensetracker.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

// --- DATABASE TABLE ---
// This defines the "users" table in SQLite
// Think of it like a spreadsheet with these columns:
object Users : Table() {
    val id = integer("id").autoIncrement()       // auto-generated: 1, 2, 3...
    val email = varchar("email", 255).uniqueIndex()  // must be unique — no duplicate accounts
    val passwordHash = varchar("password_hash", 255) // BCrypt hashed — never store raw passwords
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}

// --- REQUEST/RESPONSE SHAPES ---
// What the client sends TO the server:

@Serializable
data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// What the server sends BACK to the client:

@Serializable
data class AuthResponse(
    val token: String,   // JWT token — client stores this
    val user: UserInfo   // basic user info to display in the app
)

@Serializable
data class UserInfo(
    val id: Int,
    val email: String,
    val name: String
)
