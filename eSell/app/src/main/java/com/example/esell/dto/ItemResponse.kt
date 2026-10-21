package com.example.esell.dto

data class ItemResponse(
    val id: Int,
    val title: String,
    val description: String,
    val price: Int,
    val deal_type: String,
    val photo_url: String?,
    val category: CategoryResponse?,
    val author: AuthorResponse?
)