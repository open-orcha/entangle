package io.openorcha.entangle

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Mirrors content/lessons.json, the contract shared with the iOS and web apps.

@Serializable
data class AppInfo(val name: String, val tagline: String, val contentVersion: Int)

@Serializable
data class Lesson(val id: String, val title: String, val summary: String, val body: String) {
    val paragraphs: List<String> get() = body.split("\n\n")
}

@Serializable
data class QuizQuestion(
    val id: String,
    val lessonId: String,
    val question: String,
    val choices: List<String>,
    val answerIndex: Int,
    val explanation: String,
)

@Serializable
data class CoinFlipCopy(
    val title: String,
    val intro: String,
    val entangledLabel: String,
    val classicalLabel: String,
    val measureLabel: String,
    val resetLabel: String,
    val disclaimer: String,
)

@Serializable
data class Content(
    val app: AppInfo,
    val lessons: List<Lesson>,
    val quiz: List<QuizQuestion>,
    val coinFlip: CoinFlipCopy,
) {
    fun lesson(id: String): Lesson? = lessons.firstOrNull { it.id == id }
}

object ContentStore {
    private val json = Json { ignoreUnknownKeys = true }

    /** lessons.json is packaged as an asset from ../../content by app/build.gradle.kts. */
    fun load(context: Context): Content =
        context.assets.open("lessons.json").bufferedReader().use { json.decodeFromString(Content.serializer(), it.readText()) }
}
