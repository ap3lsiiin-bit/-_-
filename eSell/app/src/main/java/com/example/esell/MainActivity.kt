package com.example.esell

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esell.ui.theme.ESellTheme

const val PREFS_NAME = "esell_prefs"
const val KEY_ONBOARDING_DONE = "onboarding_done"
const val KEY_USER_NAME = "user_name"
const val KEY_USER_EMAIL = "user_email"
const val KEY_USER_BIO = "user_bio"
const val KEY_LOGGED_IN = "logged_in"
const val KEY_NOTIFICATIONS = "notifications_enabled"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ESellTheme {
                AppRoot()
            }
        }
    }
}

enum class Screen { FEED, LOGIN, REGISTER, PROFILE, SETTINGS }

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { ProfilePreferences(context) }

    val profileViewModel: ProfileViewModel = viewModel()

    var onboardingDone by remember { mutableStateOf(prefs.isOnboardingDone()) }
    var loggedIn by remember { mutableStateOf(prefs.isLoggedIn()) }
    var screen by remember { mutableStateOf(Screen.FEED) }

    if (!onboardingDone) {
        OnboardingScreen(
            onFinish = {
                prefs.setOnboardingDone(true)
                onboardingDone = true
            }
        )
        return
    }

    when (screen) {
        Screen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { name, email ->
                    prefs.saveProfile(name, email, prefs.getUserBio())
                    prefs.setLoggedIn(true)
                    profileViewModel.save(
                        name = name,
                        email = email,
                        bio = prefs.getUserBio(),
                        notifications = profileViewModel.notificationsEnabled
                    )
                    loggedIn = true
                    screen = Screen.FEED
                },
                onGoToRegister = { screen = Screen.REGISTER },
                onBack = { screen = Screen.FEED }
            )
        }

        Screen.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = { name, email ->
                    prefs.saveProfile(name, email, "")
                    prefs.setLoggedIn(true)
                    profileViewModel.save(
                        name = name,
                        email = email,
                        bio = "",
                        notifications = profileViewModel.notificationsEnabled
                    )
                    loggedIn = true
                    screen = Screen.FEED
                },
                onBack = { screen = Screen.LOGIN }
            )
        }

        Screen.PROFILE -> {
            ProfileScreen(
                userName = profileViewModel.username.ifBlank { "Пользователь" },
                userEmail = profileViewModel.email.ifBlank { "email@example.com" },
                userBio = profileViewModel.aboutMe,
                onBack = { screen = Screen.FEED },
                onMyListings = {  },
                onSettings = { screen = Screen.SETTINGS },
                onLogout = {
                    prefs.setLoggedIn(false)
                    profileViewModel.clearProfile()
                    loggedIn = false
                    screen = Screen.FEED
                }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                viewModel = profileViewModel,
                onSaved = { screen = Screen.PROFILE },
                onClearProfile = {
                    prefs.setOnboardingDone(false)
                    prefs.setLoggedIn(false)
                    loggedIn = false
                    onboardingDone = false
                    screen = Screen.FEED
                },
                onBack = { screen = Screen.PROFILE }
            )
        }

        Screen.FEED -> {
            FeedScreen(
                allItems = demoItems,
                onItemClick = { item ->
                    Toast.makeText(context, "Открыть: ${item.title}", Toast.LENGTH_SHORT).show()
                },
                onAddClick = {
                    if (!loggedIn) {
                        screen = Screen.LOGIN
                    } else {
                        Toast.makeText(context, "Подача объявления — скоро", Toast.LENGTH_SHORT).show()
                    }
                },
                onProfileClick = {
                    screen = if (loggedIn) Screen.PROFILE else Screen.LOGIN
                }
            )
        }
    }
}