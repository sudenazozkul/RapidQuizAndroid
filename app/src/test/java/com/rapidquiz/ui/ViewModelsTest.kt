package com.rapidquiz.ui

import com.rapidquiz.data.api.ApiClient
import com.rapidquiz.data.repo.QuizRepository
import com.rapidquiz.domain.LastScore
import com.rapidquiz.domain.QuizSessionHolder
import com.rapidquiz.ui.common.categoryColor
import com.rapidquiz.ui.common.categoryEmoji
import com.rapidquiz.ui.common.parseHexColor
import com.rapidquiz.ui.home.HomeUiState
import com.rapidquiz.ui.home.HomeViewModel
import com.rapidquiz.ui.leaderboard.EntriesState
import com.rapidquiz.ui.leaderboard.LeaderboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: QuizRepository

    private val categoriesJson = """[
        {"name":"Yazılım","slug":"yazilim","description":"d","icon":"code","color":"#3B82F6","question_count":20},
        {"name":"Fizik","slug":"fizik","description":"d","icon":"atom","color":"#EC4899","question_count":20}]"""

    private fun entriesJson(vararg names: String) = names.mapIndexed { i, n ->
        """{"rank":${i + 1},"player_name":"$n","score":${3000 - i * 100},"correct_count":${19 - i}}"""
    }.joinToString(prefix = "[", postfix = "]")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                val body = when {
                    path.startsWith("/api/v1/categories/") -> categoriesJson
                    path.contains("category=yazilim") -> entriesJson("Ali", "Ayşe", "Can")
                    path.contains("category=fizik") -> entriesJson("Zeynep")
                    path.startsWith("/api/v1/health/") -> """{"status":"ok"}"""
                    else -> return MockResponse().setResponseCode(404)
                }
                return MockResponse().setHeader("Content-Type", "application/json").setBody(body)
            }
        }
        server.start()
        repo = QuizRepository(ApiClient.create(server.url("/api/v1/").toString(), debugLogging = false))
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun <T> StateFlow<T>.awaitState(predicate: (T) -> Boolean): T =
        runBlocking { withTimeout(10_000) { first(predicate) } }

    @Test
    fun home_loads_categories() {
        val vm = HomeViewModel(repo)
        val s = vm.state.awaitState { it is HomeUiState.Loaded } as HomeUiState.Loaded
        assertEquals(listOf("yazilim", "fizik"), s.categories.map { it.slug })
    }

    @Test
    fun home_error_then_retry() {
        val working = server.dispatcher
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(500)
        }
        val vm = HomeViewModel(repo)
        vm.state.awaitState { it is HomeUiState.Error }

        server.dispatcher = working // sunucu düzeldi → "Tekrar Dene"
        vm.load()
        assertTrue(vm.state.awaitState { it is HomeUiState.Loaded } is HomeUiState.Loaded)
    }

    @Test
    fun leaderboard_selects_first_category_and_loads_entries() {
        val vm = LeaderboardViewModel(repo, QuizSessionHolder(), initialSlug = null)
        val s = vm.state.awaitState { it.entries is EntriesState.Loaded }
        assertEquals("yazilim", s.selectedSlug)
        assertEquals(3, (s.entries as EntriesState.Loaded).entries.size)
        assertNull(s.highlightRank)
    }

    @Test
    fun leaderboard_uses_initial_slug_and_switches_tabs() {
        val vm = LeaderboardViewModel(repo, QuizSessionHolder(), initialSlug = "fizik")
        assertEquals("fizik", vm.state.awaitState { it.entries is EntriesState.Loaded }.selectedSlug)
        vm.select("yazilim")
        val s = vm.state.awaitState { it.selectedSlug == "yazilim" && it.entries is EntriesState.Loaded }
        assertEquals("Ali", (s.entries as EntriesState.Loaded).entries.first().playerName)
    }

    @Test
    fun leaderboard_unknown_initial_slug_falls_back_to_first() {
        val vm = LeaderboardViewModel(repo, QuizSessionHolder(), initialSlug = "yok")
        assertEquals("yazilim", vm.state.awaitState { it.entries is EntriesState.Loaded }.selectedSlug)
    }

    @Test
    fun leaderboard_highlights_last_score_only_in_its_category_and_top10() {
        val holder = QuizSessionHolder().apply { setLastScore(LastScore("yazilim", rank = 2, inTop10 = true)) }
        val vm = LeaderboardViewModel(repo, holder, initialSlug = "yazilim")
        assertEquals(2, vm.state.awaitState { it.entries is EntriesState.Loaded }.highlightRank)

        vm.select("fizik")
        assertNull(vm.state.awaitState { it.selectedSlug == "fizik" && it.entries is EntriesState.Loaded }.highlightRank)

        holder.setLastScore(LastScore("yazilim", rank = 14, inTop10 = false))
        vm.select("yazilim")
        assertNull(vm.state.awaitState { it.selectedSlug == "yazilim" && it.entries is EntriesState.Loaded }.highlightRank)
    }

    @Test
    fun leaderboard_entries_error_is_reported() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.path.orEmpty().startsWith("/api/v1/categories/")) {
                    MockResponse().setHeader("Content-Type", "application/json").setBody(categoriesJson)
                } else {
                    MockResponse().setResponseCode(500)
                }
        }
        val vm = LeaderboardViewModel(repo, QuizSessionHolder(), initialSlug = null)
        assertTrue(vm.state.awaitState { it.entries is EntriesState.Error }.entries is EntriesState.Error)
    }

    @Test
    fun category_style_helpers() {
        assertEquals("💻", categoryEmoji("code"))
        assertEquals("✨", categoryEmoji("bilinmeyen"))
        assertEquals("✨", categoryEmoji(null))
        assertEquals(parseHexColor("#3B82F6"), categoryColor("#3B82F6", "x"))
        // Renk yoksa/geçersizse slug yedeği, o da yoksa brand
        assertEquals(parseHexColor("#14B8A6"), categoryColor("", "bilgisayar-muhendisligi"))
        assertEquals(parseHexColor("#14B8A6"), categoryColor("zz", "bilgisayar-muhendisligi"))
        assertEquals(parseHexColor("#7C3AED"), categoryColor(null, "yeni"))
        assertNull(parseHexColor("#12345"))
    }
}
