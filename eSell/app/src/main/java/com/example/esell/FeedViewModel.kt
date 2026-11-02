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

    fun deleteItem(itemId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isDeleting = true
            try {
                RetrofitClient.apiService.deleteItem(itemId)
                loadItems()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            } finally {
                isDeleting = false
            }
        }
    }
}