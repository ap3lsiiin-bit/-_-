package com.example.esell

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esell.dto.UpdateUserRequest
import com.example.esell.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ProfilePreferences(application)

    var username by mutableStateOf(prefs.getUserName())
        private set
    var email by mutableStateOf(prefs.getUserEmail())
        private set
    var aboutMe by mutableStateOf(prefs.getUserBio())
        private set
    var notificationsEnabled by mutableStateOf(prefs.isNotificationsEnabled())
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateLocalState(name: String, email: String, bio: String, notifications: Boolean) {
        this.username = name
        this.email = email
        this.aboutMe = bio
        this.notificationsEnabled = notifications
        prefs.saveProfile(name, email, bio)
        prefs.setNotificationsEnabled(notifications)
    }

    fun validate(name: String, email: String, bio: String): String? {
        val trimmedName = name.trim()
        if (trimmedName.length < 3 || trimmedName.length > 32) {
            return "Имя должно быть от 3 до 32 символов"
        }
        if (!trimmedName.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return "Имя может содержать только латиницу, цифры и _"
        }
        if (!email.trim().contains("@")) {
            return "Email должен содержать символ @"
        }
        if (bio.length > 500) {
            return "Описание не должно превышать 500 символов"
        }
        return null
    }

    fun saveProfileToServer(
        name: String,
        bio: String,
        notifications: Boolean,
        onSuccess: () -> Unit
    ) {
        val error = validate(name, email, bio)
        if (error != null) {
            errorMessage = error
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.updateMe(
                    UpdateUserRequest(username = name.trim(), about_me = bio.trim())
                )

                this@ProfileViewModel.username = response.username
                this@ProfileViewModel.aboutMe = response.about_me ?: ""
                this@ProfileViewModel.notificationsEnabled = notifications

                prefs.saveProfile(response.username, email, response.about_me ?: "")
                prefs.setNotificationsEnabled(notifications)

                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Ошибка сохранения: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearProfile() {
        username = ""
        email = ""
        aboutMe = ""
        notificationsEnabled = true
        prefs.clearProfile()
    }
}