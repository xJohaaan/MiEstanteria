package com.example.miestanteria.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Construye el cliente Retrofit que agrega el token JWT a cada peticion. */
object ApiClient {

    fun create(baseUrl: String, tokenProvider: () -> String?, debug: Boolean): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = tokenProvider()
            val request = if (token != null) {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            // La busqueda de respaldo (Open Library) puede tardar; se espera hasta 30 s
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .apply {
                if (debug) {
                    // En depuracion se ve el trafico en Logcat, pero nunca el cuerpo (contiene contrasenas)
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
