package com.example.esell

data class Item(
    val title: String,
    val price: String,
    val description: String,
    val category: String
)
val allCategories = listOf(
    "Все",
    "Электроника",
    "Книги",
    "Транспорт",
    "Дом",
    "Одежда",
    "Другое"
)