package com.rapidquiz

import android.app.Application
import com.rapidquiz.data.api.ApiClient
import com.rapidquiz.data.repo.QuizRepository
import com.rapidquiz.domain.QuizSessionHolder

/** Elle DI: 4 ekran için Hilt gereksiz. */
class AppContainer {
    val repository = QuizRepository(ApiClient.create())
    val sessionHolder = QuizSessionHolder()
}

class RapidQuizApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
