package com.rapidquiz.data

import com.rapidquiz.data.api.ApiClient
import com.rapidquiz.data.repo.ApiResult
import com.rapidquiz.data.repo.QuizRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: QuizRepository

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        repo = QuizRepository(ApiClient.create(baseUrl = server.url("/api/v1/").toString(), debugLogging = false))
    }

    @After
    fun tearDown() = server.shutdown()

    private fun json(code: Int, body: String) =
        MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body)

    @Test
    fun categories_parsed() = runBlocking {
        server.enqueue(
            json(
                200,
                """[{"name":"Yazılım","slug":"yazilim","description":"d","icon":"code","color":"#3B82F6","question_count":20}]""",
            ),
        )
        val r = repo.categories() as ApiResult.Success
        assertEquals("yazilim", r.value.single().slug)
        assertEquals(20, r.value.single().questionCount)
        assertEquals("/api/v1/categories/", server.takeRequest().path)
    }

    @Test
    fun createSession_sendsAndroidPlatform_and_accepts201() = runBlocking {
        server.enqueue(
            json(
                201,
                """{"session_id":"abc","session_token":"tok","category":{"slug":"yazilim","name":"Yazılım"},"total_questions":20,"time_limit_seconds":5}""",
            ),
        )
        val r = repo.createSession("yazilim") as ApiResult.Success
        assertEquals("abc", r.value.sessionId)
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains("\"platform\":\"android\""))
        assertTrue(body, body.contains("\"category\":\"yazilim\""))
    }

    @Test
    fun answer_timeout_sendsExplicitNull_and_token_header() = runBlocking {
        server.enqueue(
            json(200, """{"is_correct":false,"correct_choice_id":1012,"points":0,"response_ms":5100,"score":0,"finished":false}"""),
        )
        val r = repo.answer("abc", "tok", questionId = 101, choiceId = null) as ApiResult.Success
        assertEquals(1012, r.value.correctChoiceId)
        val req = server.takeRequest()
        assertEquals("tok", req.getHeader("X-Session-Token"))
        val body = req.body.readUtf8()
        assertTrue(body, body.contains("\"choice_id\":null"))
    }

    @Test
    fun question_parsed() = runBlocking {
        server.enqueue(
            json(
                200,
                """{"index":0,"total":20,"question":{"id":101,"text":"q"},"choices":[{"id":1,"text":"a"},{"id":2,"text":"b"},{"id":3,"text":"c"},{"id":4,"text":"d"}],"time_limit_ms":5000,"remaining_ms":5000}""",
            ),
        )
        val r = repo.question("abc", "tok") as ApiResult.Success
        assertEquals(4, r.value.choices.size)
        assertEquals(5000, r.value.remainingMs)
    }

    @Test
    fun leaderboard_and_score_parsed() = runBlocking {
        server.enqueue(json(200, """[{"rank":1,"player_name":"Sude","score":3100,"correct_count":19}]"""))
        val lb = repo.leaderboard("yazilim") as ApiResult.Success
        assertEquals("Sude", lb.value.single().playerName)
        assertEquals("/api/v1/leaderboard/?category=yazilim&limit=10", server.takeRequest().path)

        server.enqueue(json(201, """{"rank":3,"in_top10":true,"leaderboard":[{"rank":1,"player_name":"x","score":1,"correct_count":1}]}"""))
        val sc = repo.submitScore("abc", "tok", "Sude") as ApiResult.Success
        assertTrue(sc.value.inTop10)
    }

    @Test
    fun errorBody_mapped_to_code() = runBlocking {
        server.enqueue(json(409, """{"error":{"code":"question_mismatch","message":"Gönderilen soru uyuşmuyor"}}"""))
        val r = repo.answer("abc", "tok", 1, 2) as ApiResult.Failure
        assertEquals("question_mismatch", r.error.code)
        assertEquals(409, r.error.httpStatus)
    }

    @Test
    fun nonJsonErrorBody_gives_generic_message() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(502).setBody("<html>Bad Gateway</html>"))
        val r = repo.categories() as ApiResult.Failure
        assertEquals(null, r.error.code)
        assertEquals(502, r.error.httpStatus)
        assertTrue(r.error.message.isNotBlank())
    }

    @Test
    fun networkFailure_mapped() = runBlocking {
        server.shutdown()
        val r = repo.categories() as ApiResult.Failure
        assertEquals(com.rapidquiz.data.repo.ApiError.Kind.Network, r.error.kind)
    }
}
