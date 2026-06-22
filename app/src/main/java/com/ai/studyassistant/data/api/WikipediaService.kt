package com.ai.studyassistant.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class WikiSummaryDto(
    val title: String = "",
    val extract: String = "",
    val description: String? = null,
    val thumbnail: WikiThumbnailDto? = null
)

data class WikiThumbnailDto(
    val source: String
)

interface WikipediaService {

    @GET("page/summary/{title}")
    suspend fun getSummary(@Path("title") title: String): WikiSummaryDto

    companion object {
        private const val BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

        fun create(): WikipediaService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaService::class.java)
        }
    }
}
