package com.example.esell.dto

data class PaginatedResponse<T>(
    val items: List<T>,
    val meta: MetaResponse
)