package com.example.notes.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(baseUrl: String): NotesApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NotesApi::class.java)
}
