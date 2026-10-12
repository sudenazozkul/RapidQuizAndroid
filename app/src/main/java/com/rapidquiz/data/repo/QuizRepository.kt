package com.rapidquiz.data.repo

import com.rapidquiz.data.api.ApiClient
import com.rapidquiz.data.api.RapidQuizApi
import com.rapidquiz.data.api.dto.AnswerRequest
import com.rapidquiz.data.api.dto.AnswerResult
import com.rapidquiz.data.api.dto.ApiErrorBody
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.data.api.dto.CurrentQuestion
import com.rapidquiz.data.api.dto.LeaderboardEntry
import com.rapidquiz.data.api.dto.ScoreRequest
import com.rapidquiz.data.api.dto.ScoreResponse
import com.rapidquiz.data.api.dto.SessionCreateRequest
import com.rapidquiz.data.api.dto.SessionCreateResponse
import com.rapidquiz.data.api.dto.SessionResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class QuizRepository(private val api: RapidQuizApi) {

    suspend fun categories(): ApiResult<List<Category>> = call { api.categories() }

    suspend fun createSession(category: String): ApiResult<SessionCreateResponse> =
        call { api.createSession(SessionCreateRequest(category = category)) }

    suspend fun question(sessionId: String, token: String): ApiResult<CurrentQuestion> =
        call { api.question(sessionId, token) }

    suspend fun answer(sessionId: String, token: String, questionId: Int, choiceId: Int?): ApiResult<AnswerResult> =
        call { api.answer(sessionId, token, AnswerRequest(questionId, choiceId)) }

    suspend fun result(sessionId: String, token: String): ApiResult<SessionResult> =
        call { api.result(sessionId, token) }

    suspend fun submitScore(sessionId: String, token: String, playerName: String): ApiResult<ScoreResponse> =
        call { api.submitScore(sessionId, token, ScoreRequest(playerName)) }

    suspend fun leaderboard(category: String, limit: Int = 10): ApiResult<List<LeaderboardEntry>> =
        call { api.leaderboard(category, limit) }

    /** Sonuç beklenmez; yalnızca uyuyan sunucuyu uyandırır. */
    suspend fun wakeUp() {
        call { api.health() }
    }

    private suspend fun <T> call(block: suspend () -> T): ApiResult<T> =
        try {
            ApiResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            ApiResult.Failure(e.toApiError())
        } catch (e: IOException) {
            ApiResult.Failure(
                ApiError(ApiError.Kind.Network, message = "Bağlantını kontrol et ve tekrar dene."),
            )
        } catch (e: Exception) {
            ApiResult.Failure(ApiError(ApiError.Kind.Unknown, message = "Beklenmeyen bir hata oluştu."))
        }

    /** Gövde her zaman ayrıştırılamayabilir (örn. proxy 502 HTML döner): genel mesaj kullanılır. */
    private fun HttpException.toApiError(): ApiError {
        val parsed = runCatching {
            response()?.errorBody()?.string()?.let { ApiClient.json.decodeFromString<ApiErrorBody>(it) }
        }.getOrNull()
        return ApiError(
            kind = ApiError.Kind.Http,
            code = parsed?.error?.code,
            message = parsed?.error?.message ?: "Sunucu hatası (${code()}). Tekrar dene.",
            httpStatus = code(),
        )
    }
}
