package io.openorcha.entangle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val Violet = Color(0xFF7C5CFF)
private val Teal = Color(0xFF33D6C3)

sealed interface Screen {
    data object Lessons : Screen
    data class LessonDetail(val id: String) : Screen
    data object Quiz : Screen
    data object CoinFlip : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = ContentStore.load(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Violet, secondary = Teal)) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EntangleApp(content)
                }
            }
        }
    }
}

@Composable
fun EntangleApp(content: Content) {
    var screen by remember { mutableStateOf<Screen>(Screen.Lessons) }
    val goHome = { screen = Screen.Lessons }
    BackHandler(enabled = screen != Screen.Lessons, onBack = goHome)

    when (val s = screen) {
        Screen.Lessons -> LessonsScreen(content, onOpen = { screen = it })
        is Screen.LessonDetail -> {
            val lesson = content.lesson(s.id)
            if (lesson == null) goHome() else LessonDetailScreen(lesson, content, onBack = goHome, onOpen = { screen = it })
        }
        Screen.Quiz -> QuizScreen(content.quiz, onBack = goHome)
        Screen.CoinFlip -> CoinFlipScreen(content.coinFlip, onBack = goHome)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(title: String, onBack: (() -> Unit)?, body: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) { body() }
    }
}

@Composable
fun LessonsScreen(content: Content, onOpen: (Screen) -> Unit) {
    Page(title = content.app.name, onBack = null) {
        Text(content.app.tagline, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content.lessons.forEachIndexed { index, lesson ->
            Card(
                onClick = { onOpen(Screen.LessonDetail(lesson.id)) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Violet, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                        Text(lesson.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onOpen(Screen.Quiz) }) { Text("Take the quiz") }
            OutlinedButton(onClick = { onOpen(Screen.CoinFlip) }) { Text(content.coinFlip.title) }
        }
    }
}

@Composable
fun LessonDetailScreen(lesson: Lesson, content: Content, onBack: () -> Unit, onOpen: (Screen) -> Unit) {
    val index = content.lessons.indexOf(lesson)
    val next = content.lessons.getOrNull(index + 1)
    Page(title = lesson.title, onBack = onBack) {
        Text(lesson.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        lesson.paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
        if (next != null) {
            Button(onClick = { onOpen(Screen.LessonDetail(next.id)) }) { Text("Next: ${next.title}") }
        } else {
            Button(onClick = { onOpen(Screen.Quiz) }) { Text("Take the quiz") }
        }
    }
}

@Composable
fun QuizScreen(questions: List<QuizQuestion>, onBack: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }

    if (index >= questions.size) {
        Page(title = "Quiz complete", onBack = onBack) {
            Text("You scored $score out of ${questions.size}.", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { index = 0; selected = null; score = 0 }) { Text("Try again") }
        }
        return
    }

    val q = questions[index]
    Page(title = "Question ${index + 1} of ${questions.size}", onBack = onBack) {
        Text(q.question, style = MaterialTheme.typography.titleMedium)
        q.choices.forEachIndexed { i, choice ->
            val border = when {
                selected == null -> null
                i == q.answerIndex -> Color(0xFF35C46F)
                i == selected -> Color(0xFFE5535B)
                else -> null
            }
            Card(
                onClick = { if (selected == null) { selected = i; if (i == q.answerIndex) score += 1 } },
                enabled = selected == null,
                modifier = Modifier.fillMaxWidth(),
                border = border?.let { androidx.compose.foundation.BorderStroke(2.dp, it) },
            ) {
                Text(choice, modifier = Modifier.padding(14.dp))
            }
        }
        selected?.let { s ->
            Text((if (s == q.answerIndex) "Correct. " else "Not quite. ") + q.explanation)
            Button(onClick = { index += 1; selected = null }) {
                Text(if (index + 1 < questions.size) "Next question" else "See result")
            }
        }
    }
}

@Composable
fun CoinFlipScreen(copy: CoinFlipCopy, onBack: () -> Unit) {
    var mode by remember { mutableStateOf(FlipMode.ENTANGLED) }
    var last by remember { mutableStateOf<FlipResult?>(null) }
    var tally by remember { mutableStateOf(Tally()) }
    val reset = { last = null; tally = Tally() }

    Page(title = copy.title, onBack = onBack) {
        Text(copy.intro, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            FlipMode.entries.forEachIndexed { i, m ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { mode = m; reset() },
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = FlipMode.entries.size),
                ) { Text(if (m == FlipMode.ENTANGLED) copy.entangledLabel else copy.classicalLabel) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)) {
            Coin(last?.a)
            Coin(last?.b)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { CoinFlipModel.flipPair(mode).also { last = it; tally = tally.record(it) } }) { Text(copy.measureLabel) }
            OutlinedButton(onClick = reset) { Text(copy.resetLabel) }
        }
        Text(
            "${tally.flips} measurements · ${tally.agreements} agreed · ${(tally.rate * 100).roundToInt()}% agreement",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(copy.disclaimer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Coin(face: Face?) {
    val ring = when (face) {
        Face.HEADS -> Violet
        Face.TAILS -> Teal
        null -> Color.Gray
    }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(3.dp, ring),
        modifier = Modifier.size(120.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(face?.label ?: "?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
