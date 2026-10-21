package com.example.esell.network

import com.example.esell.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("items")
    suspend fun getItems(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("category_id") categoryId: Int? = null
    ): PaginatedResponse<ItemResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("user/me")
    suspend fun getMe(): UserResponse
}