package com.example.esell.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String? = null,
    val about_me: String? = null
)