package com.expensetracker.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object PersonalExpenses : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val amount = double("amount")
    val description = varchar("description", 500)
    val category = varchar("category", 100)
    val date = long("date")
    val createdAt = long("created_at")
    val imageUrl = varchar("image_url", 1000).nullable()

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class CreateExpenseRequest(
    val amount: Double,
    val description: String,
    val category: String,
    val date: Long? = null,
    val imageUrl: String? = null
)

@Serializable
data class ExpenseResponse(
    val id: Int,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Long,
    val createdAt: Long,
    val imageUrl: String? = null
)
