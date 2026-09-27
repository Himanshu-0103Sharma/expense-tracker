package com.expensetracker.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.expensetracker.network.ApiService
import com.expensetracker.network.UserInfo
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val apiService: ApiService) {
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val savedUser = apiService.getSavedUser()
        if (savedUser != null && apiService.token != null) {
            authState = AuthState.Success(savedUser)
            registerDeviceToken()
        }
    }

    private fun registerDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            scope.launch {
                try {
                    apiService.registerDevice(fcmToken)
                    Log.d("FCM", "Device registered: ${fcmToken.take(20)}...")
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to register device: ${e.message}")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        scope.launch {
            authState = AuthState.Loading
            try {
                val response = apiService.login(email, password)
                authState = AuthState.Success(response.user)
                registerDeviceToken()
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, name: String, password: String) {
        scope.launch {
            authState = AuthState.Loading
            try {
                val response = apiService.register(email, name, password)
                authState = AuthState.Success(response.user)
                registerDeviceToken()
            } catch (e: Exception) {
                authState = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        apiService.logout()
        authState = AuthState.Idle
    }
}
