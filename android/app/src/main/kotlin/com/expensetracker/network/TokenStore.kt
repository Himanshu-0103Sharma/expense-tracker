package com.expensetracker.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenStore(context: Context) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "expense_tracker_secure_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun saveUser(id: Int, email: String, name: String) {
        prefs.edit()
            .putInt("user_id", id)
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
    }

    fun getUser(): UserInfo? {
        val id = prefs.getInt("user_id", -1)
        val email = prefs.getString("user_email", null)
        val name = prefs.getString("user_name", null)
        if (id == -1 || email == null || name == null) return null
        return UserInfo(id, email, name)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
