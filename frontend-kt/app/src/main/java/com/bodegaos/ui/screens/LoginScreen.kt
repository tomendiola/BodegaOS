package com.bodegaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodegaos.ui.theme.*
import com.bodegaos.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel() // <-- Inyectado con Hilt
) {
    // 1. Observamos todos los estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.loginSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    // 2. Si el login fue exitoso en el ViewModel, navegamos al Dashboard
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image and Overlay
        Box(modifier = Modifier.fillMaxSize().background(PrimaryDark)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Section
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BodegaOS",
                color = White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Gestión de Inventario · QR / Código de Barras",
                color = White.copy(alpha = 0.75f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Iniciar sesión", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray800)
                    Text(text = "Accede con tu cuenta corporativa", fontSize = 13.sp, color = Gray500, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

                    Text(text = "USUARIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray500, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = email, // <-- Conectado al ViewModel
                        onValueChange = { viewModel.email.value = it }, // <-- Actualiza el ViewModel
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        placeholder = { Text("Ingresa tu usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gray500) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Gray50,
                            unfocusedContainerColor = Gray50,
                            unfocusedBorderColor = Gray200,
                            focusedBorderColor = Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gray500, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = password, // <-- Conectado al ViewModel
                        onValueChange = { viewModel.password.value = it }, // <-- Actualiza el ViewModel
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gray500) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Gray50,
                            unfocusedContainerColor = Gray50,
                            unfocusedBorderColor = Gray200,
                            focusedBorderColor = Primary
                        )
                    )

                    // 3. Mostramos el error si el ViewModel dice que hay uno
                    if (!errorMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ErrorBg, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = errorMessage!!, color = Error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        // 4. Llamamos a la función de red del ViewModel
                        onClick = { viewModel.onLoginClick() },
                        enabled = !isLoading, // Deshabilita el botón si está cargando
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 5. Animación de carga o texto normal
                            if (isLoading) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = "Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Gray500, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Usuario demo: admin@bodegaos.com / admin1234", fontSize = 12.sp, color = Gray500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "v1.0 · Aranda Rico F. & Rico Mendiola A. · 2026",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = White.copy(alpha = 0.55f),
                fontSize = 11.sp
            )
        }
    }
}