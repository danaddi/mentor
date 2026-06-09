package com.example.mentor.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mentor.R
import com.example.mentor.data.local.TokenDataStore
import com.example.mentor.presentation.auth.LoginScreen
import com.example.mentor.presentation.auth.RegisterScreen
import com.example.mentor.presentation.chat.ChatScreen
import com.example.mentor.presentation.chathistory.ChatHistoryScreen
import com.example.mentor.presentation.notes.NotesScreen
import com.example.mentor.presentation.profile.ProfileScreen
import com.example.mentor.presentation.tracker.EmotionGratitudeScreen

// Route constants
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"  // parent route for bottom-nav scaffold
    const val CHAT = "chat"
    const val NOTES = "notes"
    const val TRACKER = "tracker"
    const val PROFILE = "profile"
    const val CHAT_HISTORY = "chat_history"
}

@Composable
fun MentorNavGraph(tokenDataStore: TokenDataStore) {
    // Read saved token to determine start destination
    val token by tokenDataStore.token.collectAsState(initial = null)
    
    // Wait for token check
    var startDestination by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(token) {
        startDestination = if (token != null) Routes.MAIN else Routes.LOGIN
    }
    
    if (startDestination == null) return  // show nothing while checking token
    
    val rootNavController = rememberNavController()
    
    NavHost(
        navController = rootNavController,
        startDestination = startDestination!!
    ) {
        // Auth screens — NO bottom bar
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    rootNavController.navigate(Routes.REGISTER)
                }
            )
        }
        
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    rootNavController.popBackStack()
                }
            )
        }
        
        // Main scaffold with persistent bottom bar
        composable(Routes.MAIN) { backStackEntry ->
            val selectedSessionId = backStackEntry.savedStateHandle?.get<String>("selected_session_id")
            // Remove it so it doesn't persist across future navigations
            if (selectedSessionId != null) {
                backStackEntry.savedStateHandle?.remove<String>("selected_session_id")
            }
            MainScreen(
                rootNavController = rootNavController,
                initialSessionId = selectedSessionId
            )
        }
        
        // Chat history screen - separate route outside main scaffold
        composable(Routes.CHAT_HISTORY) {
            ChatHistoryScreen(
                onSessionClick = { sessionId ->
                    rootNavController.previousBackStackEntry?.savedStateHandle?.set("selected_session_id", sessionId)
                    rootNavController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    rootNavController: androidx.navigation.NavController,
    initialSessionId: String? = null
) {
    val bottomNavController = rememberNavController()
    // Remember the session ID so it survives recomposition within this screen
    var pendingSessionId by remember { mutableStateOf(initialSessionId) }
    
    // Update when new initialSessionId arrives (e.g., from history)
    LaunchedEffect(initialSessionId) {
        if (initialSessionId != null) {
            pendingSessionId = initialSessionId
        }
    }
    
    // Bottom nav items
    val bottomNavItems = listOf(
        BottomNavItem(Routes.CHAT, R.drawable.home, "Чат"),
        BottomNavItem(Routes.NOTES, R.drawable.list, "Заметки"),
        BottomNavItem(Routes.TRACKER, R.drawable.stars, "Трекер"),
        BottomNavItem(Routes.PROFILE, R.drawable.account, "Профиль"),
    )
    
    Scaffold(
        bottomBar = {
            // Custom bottom bar — always visible
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        if (index > 0) Spacer(modifier = Modifier.width(48.dp))
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        IconButton(onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp).alpha(if (selected) 1f else 0.5f),
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.CHAT,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CHAT) {
                val sessionToLoad = pendingSessionId
                ChatScreen(
                    onNavigateToHistory = {
                        rootNavController.navigate(Routes.CHAT_HISTORY)
                    },
                    sessionId = sessionToLoad
                )
                // Clear pending after passing to ChatScreen
                LaunchedEffect(sessionToLoad) {
                    if (sessionToLoad != null) {
                        pendingSessionId = null
                    }
                }
            }
            composable(Routes.NOTES) { NotesScreen() }
            composable(Routes.TRACKER) {
                EmotionGratitudeScreen()
            }
            composable(Routes.PROFILE) { 
                ProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

data class BottomNavItem(val route: String, val iconRes: Int, val label: String)
