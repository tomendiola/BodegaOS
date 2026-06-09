package com.bodegaos.domain.usecase

import com.bodegaos.data.security.AuthDataStore
import kotlinx.coroutines.delay
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authDataStore: AuthDataStore
) {
    suspend operator fun invoke(email: String, pass: String): Boolean {
        delay(1000) // Simulamos el tiempo de respuesta de la red

        // Simulación del backend. Si el login es correcto, guardamos un JWT falso.
        if (email == "admin@bodegaos.com" && pass == "admin1234") {
            val fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.simulacion_de_token_seguro.12345"
            authDataStore.saveToken(fakeJwt)
            return true
        }
        return false
    }
}

class LogoutUseCase @Inject constructor(
    private val authDataStore: AuthDataStore
) {
    suspend operator fun invoke() {
        authDataStore.clearToken() // Borra el token al cerrar sesión
    }
}