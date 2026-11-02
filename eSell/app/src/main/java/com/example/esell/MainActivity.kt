package com.example.esell

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esell.network.RetrofitClient
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

        RetrofitClient.init(applicationContext)

        setContent {
            ESellTheme {
                AppRoot()
            }
        }
    }
}

enum class Screen { FEED, LOGIN, REGISTER, PROFILE, SETTINGS, CREATE_LISTING, ITEM_DETAIL, MY_LISTINGS, CHAT
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { ProfilePreferences(context) }
    val tokenStorage = remember { TokenStorage(context) }

    val profileViewModel: ProfileViewModel = viewModel()
    val feedViewModel: FeedViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    var onboardingDone by remember { mutableStateOf(prefs.isOnboardingDone()) }

    var isSessionChecking by remember { mutableStateOf(true) }
    var loggedIn by remember { mutableStateOf(false) }

    var screen by remember { mutableStateOf(Screen.FEED) }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    LaunchedEffect(Unit) {
        if (tokenStorage.hasAccessToken()) {
            try {
                val user = RetrofitClient.apiService.getMe()

                prefs.saveProfile(
                    user.username,
                    user.email ?: prefs.getUserEmail(),
                    user.about_me ?: ""
                )
                profileViewModel.updateLocalState(
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
                onLoginSuccess = { name, email, bio ->
                    prefs.saveProfile(name, email, bio)
                    prefs.setLoggedIn(true)
                    profileViewModel.updateLocalState(
                        name = name,
                        email = email,
                        bio = bio,
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
                onRegisterSuccess = { name, email, bio ->
                    prefs.saveProfile(name, email, bio)
                    prefs.setLoggedIn(true)
                    profileViewModel.updateLocalState(
                        name = name,
                        email = email,
                        bio = bio,
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
                onMyListings = { screen = Screen.MY_LISTINGS },
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

        Screen.MY_LISTINGS -> {
            val allItems = (feedViewModel.uiState as? FeedUiState.Success)?.items ?: emptyList()
            val myItems = allItems.filter { it.owner == profileViewModel.username }

            MyListingsScreen(
                myItems = myItems,
                onBack = { screen = Screen.PROFILE },
                onItemClick = { item ->
                    selectedItem = item
                    screen = Screen.ITEM_DETAIL
                }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                viewModel = profileViewModel,
                onSaved = { screen = Screen.PROFILE },
                onClearProfile = {
                    tokenStorage.clearTokens()
                    prefs.setLoggedIn(false)
                    profileViewModel.clearProfile()
                    loggedIn = false
                    screen = Screen.FEED
                },
                onBack = { screen = Screen.PROFILE }
            )
        }

        Screen.CREATE_LISTING -> {
            CreateAdsScreen(
                onBack = { screen = Screen.FEED },
                onCreateSuccess = {
                    feedViewModel.loadItems()
                    screen = Screen.FEED
                }
            )
        }

        Screen.ITEM_DETAIL -> {
            val item = selectedItem
            if (item != null) {
                val isOwner = loggedIn && item.owner == profileViewModel.username

                ItemDetailScreen(
                    item = item,
                    isOwner = isOwner,
                    isDeleting = feedViewModel.isDeleting,
                    onBack = { screen = Screen.FEED },
                    onDeleteConfirm = {
                        feedViewModel.deleteItem(item.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Объявление удалено", Toast.LENGTH_SHORT).show()
                                screen = Screen.FEED
                            } else {
                                Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onChatClick = {
                        chatViewModel.loadMessages(item.id)
                        screen = Screen.CHAT
                    }
                )
            } else {
                screen = Screen.FEED
            }
        }

        Screen.CHAT -> {
            val item = selectedItem
            if (item != null) {
                ChatScreen(
                    itemTitle = item.title,
                    currentUsername = profileViewModel.username,
                    messages = chatViewModel.messages,
                    isLoading = chatViewModel.isLoading,
                    isSending = chatViewModel.isSending,
                    errorMessage = chatViewModel.errorMessage,
                    onSendMessage = { text ->
                        chatViewModel.sendMessage(item.id, text) {
                        }
                    },
                    onBack = { screen = Screen.ITEM_DETAIL }
                )
            } else {
                screen = Screen.FEED
            }
        }

        Screen.FEED -> {
            FeedScreen(
                uiState = feedViewModel.uiState,
                onRetry = { feedViewModel.loadItems() },
                onItemClick = { item ->
                    selectedItem = item
                    screen = Screen.ITEM_DETAIL
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