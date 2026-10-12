package com.rapidquiz.data

import com.rapidquiz.data.api.ApiClient
import com.rapidquiz.data.repo.ApiResult
import com.rapidquiz.data.repo.QuizRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Canlı backend duman testi (web'deki E2E_LIVE karşılığı): `RQ_LIVE=1 ./gradlew testDebugUnitTest` */
class LiveSmokeTest {
    @Test
    fun categories_from_live_backend() = runBlocking {
        assumeTrue(System.getenv("RQ_LIVE") == "1")
        val repo = QuizRepository(
            ApiClient.create(baseUrl = "https://rapidquizbackend.onrender.com/api/v1/", debugLogging = false),
        )
        val r = repo.categories()
        assertEquals("Beklenen Success, gelen: $r", true, r is ApiResult.Success)
        assertEquals(5, (r as ApiResult.Success).value.size)
    }
}
