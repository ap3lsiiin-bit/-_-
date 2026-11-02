package com.example.esell.dto

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    val url: String
)

data class CreateItemRequest(
    @SerializedName("deal_type") val deal_type: String,
    val title: String,
    val description: String,
    val price: Int,
    @SerializedName("category_id") val category_id: Int,
    @SerializedName("photo_url") val photo_url: String?
)