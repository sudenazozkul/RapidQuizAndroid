package com.rapidquiz.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rapidquiz.AppContainer
import com.rapidquiz.ui.home.HomeScreen
import com.rapidquiz.ui.home.HomeViewModel
import com.rapidquiz.ui.leaderboard.LeaderboardScreen
import com.rapidquiz.ui.leaderboard.LeaderboardViewModel
import com.rapidquiz.ui.quiz.QuizScreen
import com.rapidquiz.ui.result.ResultScreen

object Routes {
    const val HOME = "home"
    const val QUIZ = "quiz/{slug}"
    const val RESULT = "result/{slug}"
    const val LEADERBOARD = "leaderboard?slug={slug}"

    fun quiz(slug: String) = "quiz/$slug"
    fun result(slug: String) = "result/$slug"
    fun leaderboard(slug: String? = null) = if (slug == null) "leaderboard" else "leaderboard?slug=$slug"
}

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(container.repository))
            HomeScreen(
                viewModel = vm,
                onCategoryClick = { nav.navigate(Routes.quiz(it)) },
                onLeaderboardClick = { nav.navigate(Routes.leaderboard()) },
            )
        }
        composable(
            Routes.QUIZ,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { entry ->
            QuizScreen(slug = entry.arguments?.getString("slug").orEmpty(), onExit = { nav.popBackStack(Routes.HOME, false) })
        }
        composable(
            Routes.RESULT,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { entry ->
            ResultScreen(slug = entry.arguments?.getString("slug").orEmpty(), onExit = { nav.popBackStack(Routes.HOME, false) })
        }
        composable(
            Routes.LEADERBOARD,
            arguments = listOf(navArgument("slug") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) { entry ->
            val vm: LeaderboardViewModel = viewModel(
                factory = LeaderboardViewModel.factory(
                    container.repository,
                    container.sessionHolder,
                    entry.arguments?.getString("slug"),
                ),
            )
            LeaderboardScreen(
                viewModel = vm,
                onHome = { nav.popBackStack(Routes.HOME, false) },
                onPlayAgain = { nav.navigate(Routes.quiz(it)) },
            )
        }
    }
}
