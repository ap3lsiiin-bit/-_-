package com.example.esell.dto

data class MessageUserResponse(
    val id: String,
    val username: String,
    val about_me: String?
)

data class MessageResponse(
    val id: Int,
    val item_id: Int,
    val user: MessageUserResponse,
    val text: String,
    val created_at: String
)

data class PaginatedMessagesResponse(
    val items: List<MessageResponse>,
    val meta: MetaResponse
)

data class CreateMessageRequest(
    val text: String
)