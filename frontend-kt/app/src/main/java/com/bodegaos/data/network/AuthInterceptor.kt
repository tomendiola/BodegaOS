package com.bodegaos.data.network

import com.bodegaos.data.security.AuthDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Usamos runBlocking porque los interceptores de OkHttp no son corrutinas por defecto
        val token = runBlocking {
            authDataStore.getToken().firstOrNull()
        }

        // Si hay un token guardado en DataStore, lo inyectamos
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}