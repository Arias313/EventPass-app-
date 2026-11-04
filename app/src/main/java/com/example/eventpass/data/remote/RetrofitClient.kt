package com.example.eventpass.data.remote

import android.content.Context
import com.example.eventpass.util.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente singleton de Retrofit configurado para conectar con el backend en Render.
 * Incluye un interceptor de autenticación que inyecta automáticamente el token JWT en las cabeceras.
 */
object RetrofitClient {

    private const val BASE_URL = "https://eventpass-web-cnyn.onrender.com/"

    private var sessionManager: SessionManager? = null

    /**
     * Inicializa el SessionManager para que el interceptor pueda leer el token.
     */
    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(context)
        }
    }

    /**
     * Interceptor que inyecta la cabecera Authorization con el token Bearer en todas las
     * solicitudes salientes, excepto las rutas de autenticación (como login).
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath

        // Omitir cabecera Authorization en la petición de login
        if (requestPath.contains("api/auth/login")) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val token = sessionManager?.getAuthToken()
        val modifiedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(modifiedRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: EventPassApi by lazy {
        retrofit.create(EventPassApi::class.java)
    }
}
