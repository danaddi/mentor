package com.example.mentor.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mentor.ui.theme.MentorPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Обычный") }
    var scrollToBottom by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.Initial) {
            viewModel.createSession()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.ChatReady) {
            val messages = (uiState as ChatUiState.ChatReady).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MentorMind") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.Menu, contentDescription = "Chat history")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.createSession() }) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ChatUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ChatUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ChatUiState.ChatReady -> {
                    // Messages list
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.messages) { message ->
                                MessageBubble(message = message)
                            }
                        }

                        // Scroll to bottom FAB
                        if (!listState.isScrollInProgress &&
                            listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size < state.messages.size) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        scrollToBottom = true
                                    },
                                    containerColor = MentorPrimary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = "Scroll to bottom",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Handle scroll to bottom
                    LaunchedEffect(scrollToBottom) {
                        if (scrollToBottom) {
                            listState.scrollToItem(state.messages.size - 1)
                            scrollToBottom = false
                        }
                    }

                    // Mode selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMode == "Обычный",
                            onClick = { selectedMode = "Обычный" },
                            label = { Text("Обычный", fontSize = 14.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MentorPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF8C8C8C)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )

                        FilterChip(
                            selected = selectedMode == "Эмоции",
                            onClick = { selectedMode = "Эмоции" },
                            label = { Text("Эмоции", fontSize = 14.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MentorPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF8C8C8C)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    // Input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Введите сообщение...") },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank(),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MentorPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                is ChatUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = Color(0xFFC33636)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.createSession() }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: com.example.mentor.domain.model.Message) {
    val isUser = message.role == "user"
    val backgroundColor = if (isUser) {
        Color(0xFF6D5F57)
    } else {
        Color.White
    }
    val textColor = if (isUser) Color.White else Color(0xFF1A1A1A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp
            )
        }
    }
}
