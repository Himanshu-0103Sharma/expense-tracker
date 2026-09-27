package com.expensetracker.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- SHARED DATA SHAPES ---

@Serializable
data class RegisterRequest(val email: String, val name: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: UserInfo)

@Serializable
data class UserInfo(val id: Int, val email: String, val name: String)

// --- PERSONAL EXPENSES ---

@Serializable
data class CreateExpenseRequest(val amount: Double, val description: String, val category: String, val date: Long? = null, val imageUrl: String? = null)

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

// --- SPLIT EXPENSES ---

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
data class ParticipantShare(val userId: Int, val amount: Double)

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

// --- API SERVICE ---

class ApiService(private val tokenStore: TokenStore) {
    private val baseUrl = com.expensetracker.BuildConfig.API_BASE_URL

    var token: String? = tokenStore.getToken()
        private set

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        expectSuccess = false
    }

    // --- AUTH ---

    suspend fun register(email: String, name: String, password: String): AuthResponse {
        val response = client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, name, password))
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        val authResponse = response.body<AuthResponse>()
        token = authResponse.token
        tokenStore.saveToken(authResponse.token)
        tokenStore.saveUser(authResponse.user.id, authResponse.user.email, authResponse.user.name)
        return authResponse
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val response = client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        val authResponse = response.body<AuthResponse>()
        token = authResponse.token
        tokenStore.saveToken(authResponse.token)
        tokenStore.saveUser(authResponse.user.id, authResponse.user.email, authResponse.user.name)
        return authResponse
    }

    fun logout() {
        token = null
        tokenStore.clear()
    }

    fun getSavedUser(): UserInfo? = tokenStore.getUser()

    // --- PERSONAL EXPENSES ---

    suspend fun getPersonalExpenses(): List<ExpenseResponse> {
        val response = client.get("$baseUrl/personal-expenses") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return response.body()
    }

    suspend fun addPersonalExpense(amount: Double, description: String, category: String, imageUrl: String? = null): ExpenseResponse {
        val response = client.post("$baseUrl/personal-expenses") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(CreateExpenseRequest(amount, description, category, imageUrl = imageUrl))
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return response.body()
    }

    suspend fun editPersonalExpense(id: Int, amount: Double, description: String, category: String, imageUrl: String? = null) {
        val response = client.put("$baseUrl/personal-expenses/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(CreateExpenseRequest(amount, description, category, imageUrl = imageUrl))
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
    }

    suspend fun deletePersonalExpense(id: Int) {
        val response = client.delete("$baseUrl/personal-expenses/$id") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
    }

    // --- SPLIT EXPENSES ---

    suspend fun getSplitExpenses(): List<SplitExpenseResponse> {
        val response = client.get("$baseUrl/split-expenses") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return response.body()
    }

    suspend fun createSplitExpense(request: CreateSplitExpenseRequest): SplitExpenseResponse? {
        val response = client.post("$baseUrl/split-expenses") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return try { response.body() } catch (_: Exception) { null }
    }

    suspend fun editSplitExpense(id: Int, request: CreateSplitExpenseRequest) {
        val response = client.put("$baseUrl/split-expenses/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
    }

    suspend fun getBalances(): List<BalanceResponse> {
        val response = client.get("$baseUrl/split-expenses/balances") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return response.body()
    }

    suspend fun settleUp(splitExpenseId: Int, userId: Int) {
        val response = client.put("$baseUrl/split-expenses/$splitExpenseId/settle/$userId") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
    }

    // --- USERS ---

    suspend fun getAllUsers(): List<UserInfo> {
        val response = client.get("$baseUrl/users") {
            header("Authorization", "Bearer $token")
        }
        if (!response.status.isSuccess()) throw Exception(response.bodyAsText())
        return response.body()
    }

    suspend fun registerDevice(fcmToken: String) {
        client.post("$baseUrl/register-device") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(mapOf("token" to fcmToken))
        }
    }
}
