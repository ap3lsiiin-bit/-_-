package com.example.esell

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

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

    fun validate(name: String, email: String, bio: String): String? {
        val trimmedName = name.trim()
        if (trimmedName.length < 3 || trimmedName.length > 32) {
            return "Имя должно быть от 3 до 32 символов"
        }
        if (!trimmedName.matches(Regex("^[a-zA-Z]+$"))) {
            return "Имя может содержать только латинские буквы"
        }
        if (!email.trim().contains("@")) {
            return "Email должен содержать символ @"
        }
        if (bio.length > 500) {
            return "Описание не должно превышать 500 символов"
        }
        return null
    }


    fun save(name: String, email: String, bio: String, notifications: Boolean): String? {
        val error = validate(name, email, bio)
        if (error != null) return error

        this.username = name.trim()
        this.email = email.trim()
        this.aboutMe = bio.trim()
        this.notificationsEnabled = notifications

        prefs.saveProfile(this.username, this.email, this.aboutMe)
        prefs.setNotificationsEnabled(notifications)
        return null
    }

    fun clearProfile() {
        username = ""
        email = ""
        aboutMe = ""
        notificationsEnabled = true
        prefs.clearProfile()
    }
}