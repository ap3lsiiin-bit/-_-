package com.example.esell

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esell.network.RetrofitClient
import com.example.esell.ui.theme.ESellTheme
import kotlinx.coroutines.launch

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

        RetrofitClient.init(applicationContext)

        setContent {
            ESellTheme {
                AppRoot()
            }
        }
    }
}

enum class Screen { FEED, LOGIN, REGISTER, PROFILE, SETTINGS, CREATE_LISTING }

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { ProfilePreferences(context) }
    val tokenStorage = remember { TokenStorage(context) }

    val profileViewModel: ProfileViewModel = viewModel()
    val feedViewModel: FeedViewModel = viewModel()

    var onboardingDone by remember { mutableStateOf(prefs.isOnboardingDone()) }

    var isSessionChecking by remember { mutableStateOf(true) }
    var loggedIn by remember { mutableStateOf(false) }

    var screen by remember { mutableStateOf(Screen.FEED) }

    LaunchedEffect(Unit) {
        if (tokenStorage.hasAccessToken()) {
            try {
                val user = RetrofitClient.apiService.getMe()

                prefs.saveProfile(
                    user.username,
                    user.email ?: prefs.getUserEmail(),
                    user.about_me ?: ""
                )
                profileViewModel.save(
                    name = user.username,
                    email = user.email ?: "",
                    bio = user.about_me ?: "",
                    notifications = profileViewModel.notificationsEnabled
                )
                loggedIn = true
            } catch (e: Exception) {
                tokenStorage.clearTokens()
                loggedIn = false
            }
        } else {
            loggedIn = prefs.isLoggedIn()
        }
        isSessionChecking = false
    }

    if (!onboardingDone) {
        OnboardingScreen(
            onFinish = {
                prefs.setOnboardingDone(true)
                onboardingDone = true
            }
        )
        return
    }

    if (isSessionChecking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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
                    Toast.makeText(context, "Добро пожаловать, $name!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Аккаунт создан!", Toast.LENGTH_SHORT).show()
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
                    tokenStorage.clearTokens()
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
                    tokenStorage.clearTokens()
                    prefs.setOnboardingDone(false)
                    prefs.setLoggedIn(false)
                    loggedIn = false
                    onboardingDone = false
                    screen = Screen.FEED
                },
                onBack = { screen = Screen.PROFILE }
            )
        }

        Screen.CREATE_LISTING -> {
            CreateAdsScreen(
                onBack = { screen = Screen.FEED },
                onCreate = { title, price, type, description, imageUri ->
                    Toast.makeText(
                        context,
                        "Создано: $title ($type)",
                        Toast.LENGTH_SHORT
                    ).show()
                    screen = Screen.FEED
                }
            )
        }

        Screen.FEED -> {
            FeedScreen(
                uiState = feedViewModel.uiState,
                onRetry = { feedViewModel.loadItems() },
                onItemClick = { item ->
                    Toast.makeText(context, "Открыть: ${item.title}", Toast.LENGTH_SHORT).show()
                },
                onAddClick = {
                    if (!loggedIn) {
                        screen = Screen.LOGIN
                    } else {
                        screen = Screen.CREATE_LISTING
                    }
                },
                onProfileClick = {
                    screen = if (loggedIn) Screen.PROFILE else Screen.LOGIN
                }
            )
        }
    }
}