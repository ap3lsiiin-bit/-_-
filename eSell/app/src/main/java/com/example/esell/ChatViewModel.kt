package com.example.esell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esell.dto.CreateMessageRequest
import com.example.esell.dto.MessageResponse
import com.example.esell.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<MessageResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadMessages(itemId: Int, showLoading: Boolean = true) {
        if (showLoading) isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMessages(itemId)
                messages = response.items
                errorMessage = null
            } catch (e: Exception) {
                if (messages.isEmpty()) {
                    errorMessage = "Ошибка загрузки: ${e.message}"
                }
            } finally {
                if (showLoading) isLoading = false
            }
        }
    }

    fun sendMessage(itemId: Int, text: String, onSuccess: () -> Unit) {
        val trimmedText = text.trim()

        if (trimmedText.isEmpty()) {
            errorMessage = "Введите сообщение"
            return
        }

        if (trimmedText.length > 500) {
            errorMessage = "Не более 500 символов"
            return
        }

        isSending = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val newMessage = RetrofitClient.apiService.sendMessage(
                    itemId,
                    CreateMessageRequest(trimmedText)
                )
                messages = messages + newMessage
                onSuccess()
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    errorMessage = "Сначала войдите в аккаунт"
                } else {
                    errorMessage = "Ошибка сервера: ${e.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка сети: ${e.message}"
            } finally {
                isSending = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}