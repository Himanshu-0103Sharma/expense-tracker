package com.expensetracker.routes

import com.expensetracker.models.*
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

fun Route.expenseRoutes() {
    authenticate("auth-jwt") {
        route("/personal-expenses") {

            post {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val request = call.receive<CreateExpenseRequest>()
                val now = System.currentTimeMillis()

                val expenseId = transaction {
                    PersonalExpenses.insert {
                        it[PersonalExpenses.userId] = userId
                        it[amount] = request.amount
                        it[description] = request.description
                        it[category] = request.category
                        it[date] = request.date ?: now
                        it[createdAt] = now
                        it[imageUrl] = request.imageUrl
                    } get PersonalExpenses.id
                }

                call.respond(HttpStatusCode.Created, ExpenseResponse(
                    id = expenseId,
                    amount = request.amount,
                    description = request.description,
                    category = request.category,
                    date = request.date ?: now,
                    createdAt = now,
                    imageUrl = request.imageUrl
                ))
            }

            get {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()

                val month = call.request.queryParameters["month"]

                val expenses = transaction {
                    val query = PersonalExpenses.select { PersonalExpenses.userId eq userId }
                        .orderBy(PersonalExpenses.date, SortOrder.DESC)

                    query.map { row ->
                        ExpenseResponse(
                            id = row[PersonalExpenses.id],
                            amount = row[PersonalExpenses.amount],
                            description = row[PersonalExpenses.description],
                            category = row[PersonalExpenses.category],
                            date = row[PersonalExpenses.date],
                            createdAt = row[PersonalExpenses.createdAt],
                            imageUrl = row[PersonalExpenses.imageUrl]
                        )
                    }
                }

                call.respond(expenses)
            }

            put("/{id}") {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()
                val expenseId = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val request = call.receive<CreateExpenseRequest>()

                val updated = transaction {
                    PersonalExpenses.update({
                        (PersonalExpenses.id eq expenseId) and (PersonalExpenses.userId eq userId)
                    }) {
                        it[amount] = request.amount
                        it[description] = request.description
                        it[category] = request.category
                        if (request.date != null) it[date] = request.date
                        it[imageUrl] = request.imageUrl
                    }
                }

                if (updated > 0) call.respond(HttpStatusCode.OK, "Updated")
                else call.respond(HttpStatusCode.NotFound, "Not found")
            }

            delete("/{id}") {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asInt()
                val expenseId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val deleted = transaction {
                    PersonalExpenses.deleteWhere {
                        (PersonalExpenses.id eq expenseId) and (PersonalExpenses.userId eq userId)
                    }
                }

                if (deleted > 0) call.respond(HttpStatusCode.OK, "Deleted")
                else call.respond(HttpStatusCode.NotFound, "Not found")
            }
        }
    }
}
