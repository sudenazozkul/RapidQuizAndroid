package com.rapidquiz.data.api

import com.rapidquiz.data.api.dto.AnswerRequest
import com.rapidquiz.data.api.dto.AnswerResult
import com.rapidquiz.data.api.dto.Category
import com.rapidquiz.data.api.dto.CurrentQuestion
import com.rapidquiz.data.api.dto.LeaderboardEntry
import com.rapidquiz.data.api.dto.ScoreRequest
import com.rapidquiz.data.api.dto.ScoreResponse
import com.rapidquiz.data.api.dto.SessionCreateRequest
import com.rapidquiz.data.api.dto.SessionCreateResponse
import com.rapidquiz.data.api.dto.SessionResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RapidQuizApi {
    @GET("categories/")
    suspend fun categories(): List<Category>

    @POST("sessions/")
    suspend fun createSession(@Body body: SessionCreateRequest): SessionCreateResponse

    @GET("sessions/{id}/question/")
    suspend fun question(
        @Path("id") id: String,
        @Header("X-Session-Token") token: String,
    ): CurrentQuestion

    @POST("sessions/{id}/answers/")
    suspend fun answer(
        @Path("id") id: String,
        @Header("X-Session-Token") token: String,
        @Body body: AnswerRequest,
    ): AnswerResult

    @GET("sessions/{id}/result/")
    suspend fun result(
        @Path("id") id: String,
        @Header("X-Session-Token") token: String,
    ): SessionResult

    @POST("sessions/{id}/score/")
    suspend fun submitScore(
        @Path("id") id: String,
        @Header("X-Session-Token") token: String,
        @Body body: ScoreRequest,
    ): ScoreResponse

    @GET("leaderboard/")
    suspend fun leaderboard(
        @Query("category") category: String,
        @Query("limit") limit: Int = 10,
    ): List<LeaderboardEntry>

    @GET("health/")
    suspend fun health(): Map<String, String>
}
