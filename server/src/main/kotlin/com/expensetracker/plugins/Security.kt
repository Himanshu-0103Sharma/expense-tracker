package com.expensetracker.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*

// --- JWT CONFIG ---
// These values are used to create and verify tokens
object JwtConfig {
    val SECRET = System.getenv("JWT_SECRET") ?: "dev-secret-local-only"
    const val ISSUER = "expense-tracker"
    const val AUDIENCE = "expense-tracker-app"
    const val EXPIRY_MS = 86_400_000L  // 24 hours in milliseconds

    // Creates a token for a given user ID
    fun generateToken(userId: Int): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)  // embed user ID inside the token
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRY_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }
}

// Installs JWT verification — any route marked "authenticate" will require a valid token
fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.SECRET))
                    .withIssuer(JwtConfig.ISSUER)
                    .withAudience(JwtConfig.AUDIENCE)
                    .build()
            )
            validate { credential ->
                // If token is valid and has a userId, allow the request
                if (credential.payload.getClaim("userId").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null  // reject — invalid token
                }
            }
            challenge { _, _ ->
                // What to send back when token is missing/invalid/expired
                call.respond(HttpStatusCode.Unauthorized, "Token is invalid or expired")
            }
        }
    }
}
