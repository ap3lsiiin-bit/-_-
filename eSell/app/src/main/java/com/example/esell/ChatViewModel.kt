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

class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<MessageResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadMessages(itemId: Int) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMessages(itemId)
                messages = response.items
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun sendMessage(itemId: Int, text: String, onSuccess: () -> Unit) {
        if (text.isBlank()) return

        isSending = true
        viewModelScope.launch {
            try {
                val newMessage = RetrofitClient.apiService.sendMessage(
                    itemId,
                    CreateMessageRequest(text.trim())
                )
                messages = messages + newMessage
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Ошибка отправки: ${e.message}"
            } finally {
                isSending = false
            }
        }
    }
}