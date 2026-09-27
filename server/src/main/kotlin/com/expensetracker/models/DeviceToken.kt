package com.expensetracker.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object DeviceTokens : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val token = varchar("token", 500)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class RegisterDeviceRequest(
    val token: String
)
