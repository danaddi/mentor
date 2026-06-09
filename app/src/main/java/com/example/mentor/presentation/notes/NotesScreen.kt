package com.example.mentor.presentation.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mentor.R
import com.example.mentor.domain.model.Note
import com.example.mentor.ui.theme.MentorBackground
import com.example.mentor.ui.theme.MentorOnPrimary
import com.example.mentor.ui.theme.MentorPrimary
import com.example.mentor.ui.theme.MentorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel()
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_color_1),
            contentDescription = "Notes screen background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            // Custom TopAppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заметки",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MentorPrimary
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Поиск по заметкам") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(30.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MentorPrimary,
                    unfocusedIndicatorColor = Color(0xFF8C8C8C)
                )
            )

            when (val state = uiState) {
                is NotesUiState.Initial,
                is NotesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is NotesUiState.Success -> {
                    val filteredNotes = if (searchQuery.isBlank()) {
                        state.notes
                    } else {
                        state.notes.filter {
                            it.content.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "Ничего не найдено" else "Заметок пока нет",
                                fontSize = 16.sp,
                                color = Color(0xFF8C8C8C)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredNotes) { note ->
                                NoteItem(
                                    note = note,
                                    onClick = { viewModel.selectNote(note) }
                                )
                            }
                        }
                    }
                }

                is NotesUiState.Error -> {
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
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddNoteDialog = true },
            containerColor = MentorOnPrimary,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .clip(CircleShape)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Добавить заметку",
                tint = MentorPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showAddNoteDialog || selectedNote != null) {
        AddNoteDialog(
            note = selectedNote,
            onDismiss = {
                showAddNoteDialog = false
                viewModel.clearSelectedNote()
            },
            onConfirm = { content ->
                viewModel.createNote(content)
                showAddNoteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenContent(
    notes: List<Note>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreateNote: () -> Unit,
    showCreateDialog: Boolean = false,
    onConfirmCreate: (String) -> Unit = {},
    onDismissDialog: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заметки") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MentorPrimary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить заметку",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск по заметкам") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = MentorPrimary,
                    unfocusedIndicatorColor = Color(0xFF8C8C8C)
                )
            )

            val filteredNotes = if (searchQuery.isBlank()) {
                notes
            } else {
                notes.filter {
                    it.content.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Ничего не найдено" else "Заметок пока нет",
                        fontSize = 16.sp,
                        color = Color(0xFF8C8C8C)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNotes) { note ->
                        NoteItem(note = note)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AddNoteDialog(
            onDismiss = onDismissDialog,
            onConfirm = onConfirmCreate
        )
    }
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF).copy(0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.content,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.createdAt,
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C)
            )
        }
    }
}

@Composable
fun AddNoteDialog(
    note: Note? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var noteContent by remember { mutableStateOf(if (note != null) note.content else "") }
    val isViewMode = note != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isViewMode) "Просмотр заметки" else "Новая заметка",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { if (!isViewMode) noteContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Введите текст заметки...") },
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp),
                    readOnly = isViewMode,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = MentorPrimary,
                        unfocusedIndicatorColor = Color(0xFF8C8C8C)
                    )
                )

                if (isViewMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Создано: ${note?.createdAt ?: ""}",
                        fontSize = 12.sp,
                        color = Color(0xFF8C8C8C)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isViewMode) {
                        TextButton(onClick = onDismiss) {
                            Text("Закрыть", color = Color(0xFF8C8C8C))
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена", color = Color(0xFF8C8C8C))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (noteContent.isNotBlank()) {
                                    onConfirm(noteContent)
                                }
                            },
                            enabled = noteContent.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MentorPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes Screen")
@Composable
private fun NotesScreenPreview() {
    MentorTheme {
        NotesScreenContent(
            notes = listOf(
                Note("1", "user1", null, "Важная мысль о медитации и осознанности", "2024-01-15T10:00:00Z"),
                Note("2", "user1", "session1", "Разговор с ИИ помог мне понять свои эмоции", "2024-01-16T14:00:00Z"),
                Note("3", "user1", null, "Сегодня хороший день", "2024-01-17T09:00:00Z")
            ),
            searchQuery = "",
            onSearchChange = {},
            onCreateNote = {},
            showCreateDialog = false,
            onConfirmCreate = {},
            onDismissDialog = {}
        )
    }
}
