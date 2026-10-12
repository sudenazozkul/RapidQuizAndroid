package com.rapidquiz.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val slug: String,
    val description: String = "",
    val icon: String = "",
    val color: String = "",
    @SerialName("question_count") val questionCount: Int = 0,
)

@Serializable
data class CategoryBrief(val slug: String, val name: String)

@Serializable
data class SessionCreateRequest(val category: String, val platform: String = "android")

@Serializable
data class SessionCreateResponse(
    @SerialName("session_id") val sessionId: String,
    @SerialName("session_token") val sessionToken: String,
    val category: CategoryBrief,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("time_limit_seconds") val timeLimitSeconds: Int,
)

@Serializable
data class Choice(val id: Int, val text: String)

@Serializable
data class QuestionOut(val id: Int, val text: String)

@Serializable
data class CurrentQuestion(
    val index: Int,
    val total: Int,
    val question: QuestionOut,
    val choices: List<Choice>,
    @SerialName("time_limit_ms") val timeLimitMs: Int,
    @SerialName("remaining_ms") val remainingMs: Int,
)

/** `choiceId == null` süre doldu demektir; gövdede açıkça `null` gider (explicitNulls = true). */
@Serializable
data class AnswerRequest(
    @SerialName("question_id") val questionId: Int,
    @SerialName("choice_id") val choiceId: Int?,
)

@Serializable
data class AnswerResult(
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("correct_choice_id") val correctChoiceId: Int,
    val points: Int,
    @SerialName("response_ms") val responseMs: Int,
    val score: Int,
    val finished: Boolean,
)

@Serializable
data class SessionResult(
    val score: Int,
    @SerialName("correct_count") val correctCount: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("total_time_ms") val totalTimeMs: Long,
    val category: CategoryBrief,
)

@Serializable
data class ScoreRequest(@SerialName("player_name") val playerName: String)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    @SerialName("player_name") val playerName: String,
    val score: Int,
    @SerialName("correct_count") val correctCount: Int,
)

@Serializable
data class ScoreResponse(
    val rank: Int,
    @SerialName("in_top10") val inTop10: Boolean,
    val leaderboard: List<LeaderboardEntry>,
)

@Serializable
data class ApiErrorBody(val error: ApiError) {
    @Serializable
    data class ApiError(val code: String, val message: String)
}
