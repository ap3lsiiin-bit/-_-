package com.example.esell

import android.content.Context
import android.content.SharedPreferences

class TokenStorage(context: Context) {
    private val prefs: SharedPreferences = context.getApplicationContext()
        .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun hasAccessToken(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrEmpty()
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}