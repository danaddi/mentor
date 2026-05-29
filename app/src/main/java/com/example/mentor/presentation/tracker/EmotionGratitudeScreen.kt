package com.example.mentor.presentation.tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mentor.domain.model.EmotionType
import com.example.mentor.presentation.emotion.EmotionViewModel
import com.example.mentor.presentation.gratitude.GratitudeViewModel
import com.example.mentor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionGratitudeScreen(
    emotionViewModel: EmotionViewModel = hiltViewModel(),
    gratitudeViewModel: GratitudeViewModel = hiltViewModel()
) {
    var showEmotionDialog by remember { mutableStateOf(false) }
    var showGratitudeDialog by remember { mutableStateOf(false) }
    val emotionUiState by emotionViewModel.uiState.collectAsState()
    val gratitudeUiState by gratitudeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Трекер") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEmotionDialog = true },
                containerColor = MentorPrimary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить эмоцию",
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
                .padding(16.dp)
        ) {
            // Donut chart section
            DonutChartSection(
                emotions = listOf(
                    EmotionType.JOY to 3,
                    EmotionType.SADNESS to 2,
                    EmotionType.ANGER to 1,
                    EmotionType.FEAR to 1,
                    EmotionType.SURPRISE to 2,
                    EmotionType.NEUTRAL to 4
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gratitude section
            GratitudeSection(
                gratitudes = if (gratitudeUiState is com.example.mentor.presentation.gratitude.GratitudeUiState.Success) {
                    listOf((gratitudeUiState as com.example.mentor.presentation.gratitude.GratitudeUiState.Success).gratitude)
                } else emptyList(),
                onAddGratitude = { showGratitudeDialog = true }
            )
        }
    }

    // Emotion dialog
    if (showEmotionDialog) {
        AddEmotionDialog(
            onDismiss = { showEmotionDialog = false },
            onConfirm = { emotion, intensity ->
                emotionViewModel.saveEmotion(emotion.apiName, intensity)
                showEmotionDialog = false
            }
        )
    }

    // Gratitude dialog
    if (showGratitudeDialog) {
        AddGratitudeDialog(
            onDismiss = { showGratitudeDialog = false },
            onConfirm = { content ->
                gratitudeViewModel.saveGratitude(content)
                showGratitudeDialog = false
            }
        )
    }
}

@Composable
fun DonutChartSection(emotions: List<Pair<EmotionType, Int>>) {
    val total = emotions.sumOf { it.second }
    val emotionColors = mapOf(
        EmotionType.JOY to EmotionJoy,
        EmotionType.SADNESS to EmotionSadness,
        EmotionType.ANGER to EmotionAnger,
        EmotionType.FEAR to EmotionFear,
        EmotionType.SURPRISE to EmotionSurprise,
        EmotionType.NEUTRAL to EmotionNeutral
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Donut chart
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 40f
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f

                emotions.forEach { (emotion, count) ->
                    val sweepAngle = (count.toFloat() / total) * 360f
                    val color = emotionColors[emotion] ?: Color.Gray

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        )
                    )

                    startAngle += sweepAngle
                }
            }

            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Сегодня",
                    fontSize = 14.sp,
                    color = Color(0xFF8C8C8C)
                )
                Text(
                    text = total.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            emotions.take(3).forEach { (emotion, count) ->
                LegendItem(
                    color = emotionColors[emotion] ?: Color.Gray,
                    label = emotion.displayName,
                    count = count
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            emotions.drop(3).take(3).forEach { (emotion, count) ->
                LegendItem(
                    color = emotionColors[emotion] ?: Color.Gray,
                    label = emotion.displayName,
                    count = count
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label ($count)",
            fontSize = 12.sp,
            color = Color(0xFF8C8C8C)
        )
    }
}

@Composable
fun GratitudeSection(
    gratitudes: List<com.example.mentor.domain.model.GratitudeEntry>,
    onAddGratitude: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Мои благодарности",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Surface(
                    shape = CircleShape,
                    color = MentorPrimary
                ) {
                    Text(
                        text = gratitudes.size.toString(),
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onAddGratitude) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "View all",
                    tint = MentorPrimary
                )
            }
        }

        // Horizontal scrolling list
        if (gratitudes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет записей",
                    fontSize = 14.sp,
                    color = Color(0xFF8C8C8C)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gratitudes.take(5)) { gratitude ->
                    GratitudeCard(gratitude = gratitude)
                }
            }
        }
    }
}

@Composable
fun GratitudeCard(gratitude: com.example.mentor.domain.model.GratitudeEntry) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = gratitude.content,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                maxLines = 3
            )
            Text(
                text = gratitude.createdAt,
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C)
            )
        }
    }
}

@Composable
fun AddEmotionDialog(
    onDismiss: () -> Unit,
    onConfirm: (EmotionType, Int) -> Unit
) {
    var selectedEmotion by remember { mutableStateOf<EmotionType?>(null) }
    var intensity by remember { mutableStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Добавить эмоцию",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Emotion selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    EmotionType.entries.forEach { emotion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selectedEmotion == emotion) MentorPrimary else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedEmotion = emotion }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = emotion.displayName,
                                color = if (selectedEmotion == emotion) Color.White else Color(0xFF1A1A1A),
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        if (selectedEmotion == emotion) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                // Intensity slider
                Text(
                    text = "Интенсивность: $intensity",
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Slider(
                    value = intensity.toFloat(),
                    onValueChange = { intensity = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = Color(0xFF8C8C8C))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedEmotion?.let { emotion ->
                                onConfirm(emotion, intensity)
                            }
                        },
                        enabled = selectedEmotion != null,
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

@Composable
fun AddGratitudeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var gratitudeText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Новая благодарность",
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = gratitudeText,
                    onValueChange = { gratitudeText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("За что вы благодарны сегодня?") },
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = MentorPrimary,
                        unfocusedIndicatorColor = Color(0xFF8C8C8C)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = Color(0xFF8C8C8C))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (gratitudeText.isNotBlank()) {
                                onConfirm(gratitudeText)
                            }
                        },
                        enabled = gratitudeText.isNotBlank(),
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
