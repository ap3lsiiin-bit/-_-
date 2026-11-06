package com.example.esell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esell.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface FeedUiState {
    object Loading : FeedUiState
    data class Success(val items: List<Item>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel : ViewModel() {
    var uiState by mutableStateOf<FeedUiState>(FeedUiState.Loading)
        private set

    var isDeleting by mutableStateOf(false)
        private set

    init {
        loadItems()
    }

    fun silentRefresh() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getItems(page = 1, pageSize = 20)
                val mappedItems = response.items.map { it.toItem() }
                uiState = FeedUiState.Success(mappedItems)
            } catch (e: Exception) { }
        }
    }

    fun loadItems() {
        uiState = FeedUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getItems(page = 1, pageSize = 20)
                val mappedItems = response.items.map { it.toItem() }
                uiState = FeedUiState.Success(mappedItems)
            } catch (e: IOException) {
                uiState = FeedUiState.Error("Ошибка сети. Проверьте интернет.")
            } catch (e: Exception) {
                uiState = FeedUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    fun deleteItem(itemId: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            isDeleting = true
            try {
                val response = RetrofitClient.apiService.deleteItem(itemId)

                if (response.isSuccessful) {
                    loadItems()
                    onResult(null)
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Сначала войдите в аккаунт"
                        403 -> "У вас нет прав на удаление этого объявления"
                        404 -> "Объявление уже удалено или не найдено"
                        else -> "Не удалось удалить объявление (код ${response.code()})"
                    }
                    onResult(errorMsg)
                }
            } catch (e: Exception) {
                onResult("Ошибка сети. Объявление не удалено")
            } finally {
                isDeleting = false
            }
        }
    }
}