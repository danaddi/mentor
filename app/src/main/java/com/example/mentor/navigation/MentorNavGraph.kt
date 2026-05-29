package com.example.mentor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.presentation.auth.AuthViewModel
import com.example.mentor.presentation.auth.LoginScreen
import com.example.mentor.presentation.auth.RegisterScreen
import com.example.mentor.presentation.chat.ChatScreen
import com.example.mentor.presentation.chathistory.ChatHistoryScreen
import com.example.mentor.presentation.emotion.EmotionScreen
import com.example.mentor.presentation.gratitude.GratitudeScreen
import com.example.mentor.presentation.notes.NotesScreen
import com.example.mentor.presentation.profile.ProfileScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Chat : Screen("chat")
    object ChatHistory : Screen("chat_history")
    object Notes : Screen("notes")
    object Emotion : Screen("emotion")
    object Gratitude : Screen("gratitude")
    object Profile : Screen("profile")
    object Home : Screen("home")
}

@Composable
fun MentorNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val startDestination = viewModel.getStartDestination()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToChatHistory = {
                    navController.navigate(Screen.ChatHistory.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.Notes.route)
                },
                onNavigateToEmotion = {
                    navController.navigate(Screen.Emotion.route)
                },
                onNavigateToGratitude = {
                    navController.navigate(Screen.Gratitude.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen()
        }

        composable(Screen.ChatHistory.route) {
            ChatHistoryScreen(
                onSessionClick = { sessionId ->
                    // Navigate to chat with session ID
                    navController.navigate(Screen.Chat.route)
                }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen()
        }

        composable(Screen.Emotion.route) {
            EmotionScreen()
        }

        composable(Screen.Gratitude.route) {
            GratitudeScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : androidx.lifecycle.ViewModel() {
    @androidx.compose.runtime.Composable
    fun getStartDestination(): String {
        val token by tokenDataStore.token.collectAsState(initial = null)
        return if (token != null) Screen.Home.route else Screen.Login.route
    }
}

@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToChatHistory: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToEmotion: () -> Unit,
    onNavigateToGratitude: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToChat,
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = null) },
                    label = { Text("Чат") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToChatHistory,
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("История") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToNotes,
                    icon = { Icon(Icons.Default.Note, contentDescription = null) },
                    label = { Text("Заметки") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToEmotion,
                    icon = { Icon(Icons.Default.EmojiEmotions, contentDescription = null) },
                    label = { Text("Эмоции") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToGratitude,
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Благодарность") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Добро пожаловать в MentorMind!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
