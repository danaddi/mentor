package com.example.mentor.presentation.emotion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mentor.domain.model.EmotionType
import com.example.mentor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmotionViewModel = hiltViewModel()
) {
    var selectedEmotion by remember { mutableStateOf<EmotionType?>(null) }
    var intensity by remember { mutableStateOf(5) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Трекер эмоций") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is EmotionUiState.Initial -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Как вы себя чувствуете?",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(EmotionType.entries) { emotion ->
                            EmotionCard(
                                emotion = emotion,
                                isSelected = selectedEmotion == emotion,
                                onClick = { selectedEmotion = emotion }
                            )
                        }
                    }

                    if (selectedEmotion != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Интенсивность: $intensity",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = intensity.toFloat(),
                            onValueChange = { intensity = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = {
                                selectedEmotion?.let { emotion ->
                                    viewModel.saveEmotion(emotion.apiName, intensity)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Сохранить")
                        }
                    }
                }
            }
            is EmotionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EmotionUiState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Эмоция сохранена!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Записать еще")
                        }
                    }
                }
            }
            is EmotionUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetState() }) {
                            Text("Попробовать снова")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionCard(
    emotion: EmotionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (emotion) {
        EmotionType.JOY -> EmotionJoy
        EmotionType.SADNESS -> EmotionSadness
        EmotionType.ANGER -> EmotionAnger
        EmotionType.FEAR -> EmotionFear
        EmotionType.SURPRISE -> EmotionSurprise
        EmotionType.DISGUST -> EmotionDisgust
        EmotionType.NEUTRAL -> EmotionNeutral
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emotion.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
