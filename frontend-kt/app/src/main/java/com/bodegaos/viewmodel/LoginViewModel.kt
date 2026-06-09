package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    var email = MutableStateFlow("admin@bodegaos.com")
    var password = MutableStateFlow("admin1234")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onLoginClick() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val success = loginUseCase(email.value, password.value)

            if (success) {
                _loginSuccess.value = true
            } else {
                _errorMessage.value = "Credenciales incorrectas"
            }
            _isLoading.value = false
        }
    }
}