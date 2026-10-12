package com.rapidquiz.data.api

import com.rapidquiz.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    /** `explicitNulls = true`: `choice_id: null` gövdede açıkça gider (web ile birebir). */
    val json: Json = Json {
        explicitNulls = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun create(baseUrl: String = BuildConfig.API_BASE_URL, debugLogging: Boolean = BuildConfig.DEBUG): RapidQuizApi {
        val http = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .apply {
                if (debugLogging) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                            redactHeader("X-Session-Token")
                        },
                    )
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RapidQuizApi::class.java)
    }
}
