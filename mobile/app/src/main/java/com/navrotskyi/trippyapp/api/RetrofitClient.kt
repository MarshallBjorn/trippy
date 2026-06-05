package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val plainClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val plainRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(plainClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val refreshApi: AuthRefreshApi by lazy { plainRetrofit.create(AuthRefreshApi::class.java) }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(GlobalErrorInterceptor())
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator())
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}