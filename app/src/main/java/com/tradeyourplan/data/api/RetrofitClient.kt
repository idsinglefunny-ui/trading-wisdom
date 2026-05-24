package com.tradeyourplan.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // TODO: 替换为实际的生产环境 API 地址
    private const val BASE_URL = "https://trading.tangping.me/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (isDebug()) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private fun isDebug(): Boolean {
        // Simple debug check - in production you'd use BuildConfig.DEBUG
        return true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quoteApiService: QuoteApiService by lazy {
        retrofit.create(QuoteApiService::class.java)
    }
}
