package com.example.mentor.presentation.tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mentor.R
import com.example.mentor.domain.model.Emotion
import com.example.mentor.domain.model.EmotionType
import com.example.mentor.domain.model.GratitudeEntry
import com.example.mentor.presentation.emotion.EmotionUiState
import com.example.mentor.presentation.emotion.EmotionViewModel
import com.example.mentor.presentation.gratitude.GratitudeUiState
import com.example.mentor.presentation.gratitude.GratitudeViewModel
import com.example.mentor.ui.theme.EmotionAnger
import com.example.mentor.ui.theme.EmotionDisgust
import com.example.mentor.ui.theme.EmotionFear
import com.example.mentor.ui.theme.EmotionJoy
import com.example.mentor.ui.theme.EmotionNeutral
import com.example.mentor.ui.theme.EmotionSadness
import com.example.mentor.ui.theme.EmotionSurprise
import com.example.mentor.ui.theme.MentorPrimary
import com.example.mentor.ui.theme.MentorTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val filteredEmotions by emotionViewModel.filteredEmotions.collectAsState()
    val startDate by emotionViewModel.startDate.collectAsState()
    val endDate by emotionViewModel.endDate.collectAsState()
    val allGratitudes by gratitudeViewModel.gratitudes.collectAsState()

    // Refresh emotions when screen is focused
    LaunchedEffect(Unit) {
        emotionViewModel.loadEmotions()
        emotionViewModel.setDateRange(startDate, endDate)
    }

    EmotionGratitudeScreenContent(
        emotions = filteredEmotions,
        gratitudeEntries = allGratitudes,
        startDate = startDate,
        endDate = endDate,
        showGratitudeDialog = showGratitudeDialog,
        showEmotionDialog = showEmotionDialog,
        onAddEmotion = { showEmotionDialog = true },
        onAddGratitude = { showGratitudeDialog = true },
        onDismissEmotionDialog = { showEmotionDialog = false },
        onConfirmEmotion = { emotionType, intensity ->
            emotionViewModel.saveEmotion(emotionType, intensity)
            showEmotionDialog = false
        },
        onDismissGratitudeDialog = { showGratitudeDialog = false },
        onConfirmGratitude = { content ->
            gratitudeViewModel.saveGratitude(content)
            showGratitudeDialog = false
        },
        onDateRangeSelected = { start, end ->
            emotionViewModel.setDateRange(start, end)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionGratitudeScreenContent(
    emotions: List<Emotion>,
    gratitudeEntries: List<GratitudeEntry>,
    startDate: LocalDate = LocalDate.now(),
    endDate: LocalDate = LocalDate.now(),
    showEmotionDialog: Boolean = false,
    showGratitudeDialog: Boolean = false,
    onAddEmotion: () -> Unit = {},
    onAddGratitude: () -> Unit = {},
    onDismissEmotionDialog: () -> Unit = {},
    onDismissGratitudeDialog: () -> Unit = {},
    onConfirmEmotion: (EmotionType, Int) -> Unit = { _, _ -> },
    onConfirmGratitude: (String) -> Unit = {},
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_color_1),
            contentDescription = "Emotion gratitude screen background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Трекер")
                            IconButton(onClick = onAddEmotion) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Добавить эмоцию",
                                    tint = MentorPrimary
                                )
                            }
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
                    .padding(16.dp)
            ) {
                DonutChartSection(
                    emotions = emotions
                        .groupBy { emotionTypeFromApiName(it.emotionType) }
                        .mapValues { (_, list) -> list.size }
                        .toList(),
                    startDate = startDate,
                    endDate = endDate,
                    onDateRangeClick = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                GratitudeSection(
                    gratitudes = gratitudeEntries,
                    onAddGratitude = onAddGratitude
                )
            }
        }
    }

    if (showEmotionDialog) {
        AddEmotionDialog(
            onDismiss = onDismissEmotionDialog,
            onConfirm = onConfirmEmotion
        )
    }

    if (showGratitudeDialog) {
        AddGratitudeDialog(
            onDismiss = onDismissGratitudeDialog,
            onConfirm = onConfirmGratitude
        )
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            startDate = startDate,
            endDate = endDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                onDateRangeSelected(start, end)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun DonutChartSection(
    emotions: List<Pair<EmotionType, Int>>,
    startDate: LocalDate,
    endDate: LocalDate,
    onDateRangeClick: () -> Unit
) {
    val safeEmotions = emotions.ifEmpty { listOf(EmotionType.NEUTRAL to 0) }
    val totalCount = emotions.sumOf { it.second }
    val chartTotal = totalCount.coerceAtLeast(1)
    val dateRangeText = formatDateRange(startDate, endDate)
    val emotionColors = mapOf(
        EmotionType.JOY to EmotionJoy,
        EmotionType.SADNESS to EmotionSadness,
        EmotionType.ANGER to EmotionAnger,
        EmotionType.FEAR to EmotionFear,
        EmotionType.SURPRISE to EmotionSurprise,
        EmotionType.DISGUST to EmotionDisgust,
        EmotionType.NEUTRAL to EmotionNeutral
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 40f
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f

                safeEmotions.forEach { (emotion, count) ->
                    val sweepAngle = (count.toFloat() / chartTotal) * 360f
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateRangeText,
                    fontSize = 14.sp,
                    color = MentorPrimary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onDateRangeClick)
                )
                Text(
                    text = totalCount.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            safeEmotions.take(3).forEach { (emotion, count) ->
                LegendItem(
                    color = emotionColors[emotion] ?: Color.Gray,
                    label = emotion.displayName,
                    percentage = calculatePercentage(count, totalCount)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            safeEmotions.drop(3).take(3).forEach { (emotion, count) ->
                LegendItem(
                    color = emotionColors[emotion] ?: Color.Gray,
                    label = emotion.displayName,
                    percentage = calculatePercentage(count, totalCount)
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, percentage: Int) {
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
            text = "$label ($percentage%)",
            fontSize = 12.sp,
            color = Color(0xFF8C8C8C)
        )
    }
}

@Composable
fun GratitudeSection(
    gratitudes: List<GratitudeEntry>,
    onAddGratitude: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                    Icons.Default.Add,
                    contentDescription = "Добавить благодарность",
                    tint = MentorPrimary
                )
            }
        }

        if (gratitudes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clip(RoundedCornerShape(30.dp)),
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
fun GratitudeCard(gratitude: GratitudeEntry) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        shape = RoundedCornerShape(30.dp),
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
            shape = RoundedCornerShape(30.dp)
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
                                text = emotion.dialogDisplayName(),
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
                                    .border(
                                        1.dp,
                                        if (selectedEmotion == emotion) Color.White else Color(0xFF8C8C8C),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

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
            shape = RoundedCornerShape(30.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    startDate: LocalDate,
    endDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        initialSelectedEndDateMillis = endDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedStart = dateRangePickerState.selectedStartDateMillis?.toLocalDate(zoneId)
                    val selectedEnd = dateRangePickerState.selectedEndDateMillis?.toLocalDate(zoneId)
                    if (selectedStart != null && selectedEnd != null) {
                        onConfirm(selectedStart, selectedEnd)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null &&
                    dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Выберите период",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}

private fun formatDateRange(startDate: LocalDate, endDate: LocalDate): String {
    if (startDate == LocalDate.now() && endDate == LocalDate.now()) {
        return "Сегодня"
    }

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return if (startDate == endDate) {
        startDate.format(formatter)
    } else {
        "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
}

private fun calculatePercentage(count: Int, total: Int): Int {
    return if (total == 0) 0 else (count.toFloat() / total * 100).toInt()
}

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}

private fun EmotionType.dialogDisplayName(): String {
    return when (this) {
        EmotionType.JOY -> "Радость"
        EmotionType.SADNESS -> "Грусть"
        EmotionType.ANGER -> "Злость"
        EmotionType.FEAR -> "Страх"
        EmotionType.SURPRISE -> "Удивление"
        EmotionType.DISGUST -> "Отвращение"
        EmotionType.NEUTRAL -> "Спокойствие"
    }
}

private fun emotionTypeFromApiName(apiName: String): EmotionType {
    return when (apiName.lowercase()) {
        EmotionType.JOY.apiName -> EmotionType.JOY
        EmotionType.SADNESS.apiName -> EmotionType.SADNESS
        EmotionType.ANGER.apiName -> EmotionType.ANGER
        EmotionType.FEAR.apiName -> EmotionType.FEAR
        EmotionType.SURPRISE.apiName -> EmotionType.SURPRISE
        EmotionType.DISGUST.apiName -> EmotionType.DISGUST
        EmotionType.NEUTRAL.apiName -> EmotionType.NEUTRAL
        else -> EmotionType.NEUTRAL
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Emotion Gratitude Screen")
@Composable
private fun EmotionGratitudeScreenPreview() {
    MentorTheme {
        EmotionGratitudeScreenContent(
            emotions = listOf(
                Emotion("1", "user1", "joy", 8, "2024-01-15T10:00:00Z"),
                Emotion("2", "user1", "sadness", 4, "2024-01-15T11:00:00Z"),
                Emotion("3", "user1", "neutral", 5, "2024-01-15T12:00:00Z")
            ),
            gratitudeEntries = listOf(
                GratitudeEntry("1", "user1", "Благодарен за поддержку друзей", "2024-01-15T10:00:00Z"),
                GratitudeEntry("2", "user1", "Рад новому дню и возможностям", "2024-01-16T09:00:00Z")
            ),
            showEmotionDialog = false,
            showGratitudeDialog = false,
            onAddEmotion = {},
            onAddGratitude = {},
            onDismissEmotionDialog = {},
            onDismissGratitudeDialog = {},
            onConfirmEmotion = { _, _ -> },
            onConfirmGratitude = {},
            onDateRangeSelected = { _, _ -> }
        )
    }
}
