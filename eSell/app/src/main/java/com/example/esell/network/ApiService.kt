package com.example.esell.network

import com.example.esell.dto.*
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response

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

    @PATCH("user/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): UserResponse

    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): UploadResponse

    @POST("items")
    suspend fun createItem(@Body request: CreateItemRequest): ItemResponse

    @DELETE("items/{item_id}")
    suspend fun deleteItem(@Path("item_id") itemId: Int): Response<Unit>

    @GET("items/{item_id}/messages")
    suspend fun getMessages(
        @Path("item_id") itemId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): PaginatedMessagesResponse

    @POST("items/{item_id}/messages")
    suspend fun sendMessage(
        @Path("item_id") itemId: Int,
        @Body request: CreateMessageRequest
    ): MessageResponse
}