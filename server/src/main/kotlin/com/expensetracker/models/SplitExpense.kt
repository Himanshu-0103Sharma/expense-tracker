package com.expensetracker.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object SplitExpenses : Table() {
    val id = integer("id").autoIncrement()
    val paidById = integer("paid_by_id").references(Users.id)
    val totalAmount = double("total_amount")
    val description = varchar("description", 500)
    val category = varchar("category", 100)
    val splitType = varchar("split_type", 20)
    val date = long("date")
    val createdAt = long("created_at")
    val imageUrl = varchar("image_url", 1000).nullable()

    override val primaryKey = PrimaryKey(id)
}

object SplitParticipants : Table() {
    val id = integer("id").autoIncrement()
    val splitExpenseId = integer("split_expense_id").references(SplitExpenses.id)
    val userId = integer("user_id").references(Users.id)
    val amount = double("amount")
    val settled = bool("settled").default(false)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class CreateSplitExpenseRequest(
    val totalAmount: Double,
    val description: String,
    val category: String,
    val splitType: String,
    val date: Long? = null,
    val imageUrl: String? = null,
    val participants: List<ParticipantShare>
)

@Serializable
data class ParticipantShare(
    val userId: Int,
    val amount: Double
)

@Serializable
data class SplitExpenseResponse(
    val id: Int,
    val paidBy: UserInfo,
    val totalAmount: Double,
    val description: String,
    val category: String,
    val splitType: String,
    val date: Long,
    val participants: List<ParticipantResponse>,
    val createdAt: Long,
    val imageUrl: String? = null
)

@Serializable
data class ParticipantResponse(
    val user: UserInfo,
    val amount: Double,
    val settled: Boolean
)

@Serializable
data class BalanceResponse(
    val from: UserInfo,
    val to: UserInfo,
    val amount: Double
)
