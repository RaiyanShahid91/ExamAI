package com.ai.studyassistant.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class TriviaResponse(
    val response_code: Int = -1,
    val results: List<TriviaQuestionDto> = emptyList()
)

data class TriviaQuestionDto(
    val category: String = "",
    val type: String = "",
    val difficulty: String = "",
    val question: String = "",
    val correct_answer: String = "",
    val incorrect_answers: List<String> = emptyList()
)

interface OpenTriviaService {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int? = null,
        @Query("type") type: String = "multiple"
    ): TriviaResponse

    companion object {
        private const val BASE_URL = "https://opentdb.com/"

        fun create(): OpenTriviaService {
            val userAgentInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "ExamAI-StudyAssistant-Android/1.0 (https://github.com/RaiyanShahid91/ExamAI)")
                    .build()
                chain.proceed(request)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(userAgentInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenTriviaService::class.java)
        }
    }
}
