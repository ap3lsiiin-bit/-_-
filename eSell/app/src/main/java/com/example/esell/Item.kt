package com.example.esell

import com.example.esell.dto.ItemResponse

data class Item(
    val id: Int = 0,
    val title: String,
    val price: String,
    val description: String,
    val category: String,
    val owner: String = "Неизвестный автор",
    val imageUrl: String? = null,
    val dealType: String = "sale"
)

fun ItemResponse.toItem(): Item {
    val formattedPrice = if (this.price > 0) {
        String.format("%,d ₽", this.price).replace(',', ' ')
    } else {
        "Бесплатно"
    }

    return Item(
        id = this.id,
        title = this.title,
        price = formattedPrice,
        description = this.description,
        category = this.category?.name ?: "Другое",
        owner = this.author?.username ?: "Неизвестный автор",
        imageUrl = this.photo_url,
        dealType = this.deal_type
    )
}