package com.brewkery.app

import com.brewkery.app.network.BrewkeryApi
import com.brewkery.app.network.MenuRepository
import com.brewkery.app.storage.CartRepository
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer {

    private val baseUrl = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"

    private val gson = GsonBuilder().serializeNulls().create()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: BrewkeryApi by lazy { retrofit.create(BrewkeryApi::class.java) }

    val menuRepository: MenuRepository by lazy { MenuRepository(api) }

    val cartRepository: CartRepository by lazy { CartRepository() }
}
