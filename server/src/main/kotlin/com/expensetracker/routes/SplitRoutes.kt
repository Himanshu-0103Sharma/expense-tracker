package com.expensetracker.routes

import com.expensetracker.models.*
import com.expensetracker.services.NotificationService
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

fun Route.splitRoutes() {
    authenticate("auth-jwt") {
        route("/split-expenses") {

            post {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val request = call.receive<CreateSplitExpenseRequest>()
                val now = System.currentTimeMillis()

                val splitId = transaction {
                    val id = SplitExpenses.insert {
                        it[paidById] = userId
                        it[totalAmount] = request.totalAmount
                        it[description] = request.description
                        it[category] = request.category
                        it[splitType] = request.splitType
                        it[date] = request.date ?: now
                        it[createdAt] = now
                        it[imageUrl] = request.imageUrl
                    } get SplitExpenses.id

                    request.participants.forEach { participant ->
                        SplitParticipants.insert {
                            it[splitExpenseId] = id
                            it[SplitParticipants.userId] = participant.userId
                            it[amount] = participant.amount
                            it[settled] = participant.userId == userId
                        }
                    }

                    id
                }

                val payerName = transaction {
                    Users.select { Users.id eq userId }.first()[Users.name]
                }
                val otherUserIds = request.participants
                    .map { it.userId }
                    .filter { it != userId }
                NotificationService.notifyUsers(
                    otherUserIds,
                    "New split expense",
                    "$payerName added ${request.description} — $${String.format("%.2f", request.totalAmount)}"
                )

                call.respond(HttpStatusCode.Created, mapOf("id" to splitId))
            }

            get {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val splits = transaction {
                    val myParticipations = SplitParticipants
                        .select { SplitParticipants.userId eq userId }
                        .map { it[SplitParticipants.splitExpenseId] }

                    SplitExpenses.select { SplitExpenses.id inList myParticipations }
                        .orderBy(SplitExpenses.date, SortOrder.DESC)
                        .map { row ->
                            val splitId = row[SplitExpenses.id]
                            val paidByUser = Users.select { Users.id eq row[SplitExpenses.paidById] }.first()

                            val participants = SplitParticipants
                                .select { SplitParticipants.splitExpenseId eq splitId }
                                .map { p ->
                                    val user = Users.select { Users.id eq p[SplitParticipants.userId] }.first()
                                    ParticipantResponse(
                                        user = UserInfo(
                                            id = user[Users.id],
                                            email = user[Users.email],
                                            name = user[Users.name]
                                        ),
                                        amount = p[SplitParticipants.amount],
                                        settled = p[SplitParticipants.settled]
                                    )
                                }

                            SplitExpenseResponse(
                                id = splitId,
                                paidBy = UserInfo(
                                    id = paidByUser[Users.id],
                                    email = paidByUser[Users.email],
                                    name = paidByUser[Users.name]
                                ),
                                totalAmount = row[SplitExpenses.totalAmount],
                                description = row[SplitExpenses.description],
                                category = row[SplitExpenses.category],
                                splitType = row[SplitExpenses.splitType],
                                date = row[SplitExpenses.date],
                                participants = participants,
                                createdAt = row[SplitExpenses.createdAt],
                                imageUrl = row[SplitExpenses.imageUrl]
                            )
                        }
                }

                call.respond(splits)
            }

            get("/balances") {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val balances = transaction {
                    val netBalances = mutableMapOf<Int, Double>()

                    val mySplitIds = SplitParticipants
                        .select { SplitParticipants.userId eq userId }
                        .map { it[SplitParticipants.splitExpenseId] }

                    (SplitParticipants innerJoin SplitExpenses)
                        .select { SplitParticipants.splitExpenseId inList mySplitIds }
                        .forEach { row ->
                            val participantId = row[SplitParticipants.userId]
                            val amount = row[SplitParticipants.amount]
                            val settled = row[SplitParticipants.settled]
                            val paidById = row[SplitExpenses.paidById]

                            if (settled) return@forEach

                            if (paidById == userId && participantId != userId) {
                                netBalances[participantId] = (netBalances[participantId] ?: 0.0) + amount
                            } else if (participantId == userId && paidById != userId) {
                                netBalances[paidById] = (netBalances[paidById] ?: 0.0) - amount
                            }
                        }

                    val me = Users.select { Users.id eq userId }.first()
                    val meInfo = UserInfo(id = me[Users.id], email = me[Users.email], name = me[Users.name])

                    netBalances.filter { it.value != 0.0 }.map { (otherUserId, netAmount) ->
                        val otherUser = Users.select { Users.id eq otherUserId }.first()
                        val userInfo = UserInfo(
                            id = otherUser[Users.id],
                            email = otherUser[Users.email],
                            name = otherUser[Users.name]
                        )

                        if (netAmount > 0) {
                            BalanceResponse(from = userInfo, to = meInfo, amount = netAmount)
                        } else {
                            BalanceResponse(from = meInfo, to = userInfo, amount = -netAmount)
                        }
                    }
                }

                call.respond(balances)
            }

            put("/{id}") {
                val splitId = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val request = call.receive<CreateSplitExpenseRequest>()

                transaction {
                    SplitExpenses.update({ SplitExpenses.id eq splitId }) {
                        it[paidById] = userId
                        it[totalAmount] = request.totalAmount
                        it[description] = request.description
                        it[category] = request.category
                        it[splitType] = request.splitType
                        if (request.date != null) it[date] = request.date
                        it[imageUrl] = request.imageUrl
                    }

                    SplitParticipants.deleteWhere { SplitParticipants.splitExpenseId eq splitId }

                    request.participants.forEach { participant ->
                        SplitParticipants.insert {
                            it[splitExpenseId] = splitId
                            it[SplitParticipants.userId] = participant.userId
                            it[amount] = participant.amount
                            it[settled] = participant.userId == userId
                        }
                    }
                }

                call.respond(HttpStatusCode.OK, "Updated")
            }

            put("/{id}/settle/{participantUserId}") {
                val expenseId = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid expense ID")
                val participantUserId = call.parameters["participantUserId"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid user ID")

                val updated = transaction {
                    SplitParticipants.update({
                        (SplitParticipants.splitExpenseId eq expenseId) and
                                (SplitParticipants.userId eq participantUserId)
                    }) {
                        it[settled] = true
                    }
                }

                if (updated > 0) call.respond(HttpStatusCode.OK, "Settled")
                else call.respond(HttpStatusCode.NotFound, "Not found")
            }
        }

        get("/users") {
            val userId = call.principal<JWTPrincipal>()!!
                .payload.getClaim("userId").asInt()

            val users = transaction {
                Users.selectAll()
                    .filter { it[Users.id] != userId }
                    .map { row ->
                        UserInfo(
                            id = row[Users.id],
                            email = row[Users.email],
                            name = row[Users.name]
                        )
                    }
            }

            call.respond(users)
        }
    }
}
