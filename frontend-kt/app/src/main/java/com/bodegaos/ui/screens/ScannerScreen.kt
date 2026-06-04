package com.bodegaos.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bodegaos.data.SyncManager
import com.bodegaos.data.model.PendingScan
import com.bodegaos.data.model.Product
import com.bodegaos.viewmodel.ScannerViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun ScannerScreen(viewModel: ScannerViewModel = viewModel()) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Escáner de Productos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(20.dp))

        if (viewModel.scannedCode == null) {
            // --- PANTALLA PRINCIPAL ---
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scanner.startScan().addOnSuccessListener { barcode -> 
                                barcode.rawValue?.let { viewModel.onSkuChanged(it) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Abrir Cámara", fontSize = 16.sp) }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            OutlinedTextField(
                value = viewModel.manualInput,
                onValueChange = { viewModel.manualInput = it },
                label = { Text("Escribe el código de barras o SKU") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (viewModel.isCheckingProduct) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (viewModel.manualInput.isNotBlank()) {
                        focusManager.clearFocus()
                        viewModel.onSkuChanged(viewModel.manualInput)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isCheckingProduct
            ) { Text("Buscar Producto") }

        } else if (viewModel.justAddedMessage) {
            // --- PANTALLA ÉXITO ---
            Surface(color = Color(0xFFD1FAE5), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("¡Transacción Exitosa!", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Text(if (isOnline(context)) "Enviado al Inventario Central" else "Guardado en Sync (Offline)", fontSize = 12.sp, color = Color(0xFF065F46))
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.resetScanner() },
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) { Text("Escanear otro producto", fontSize = 16.sp) }

        } else {
            // --- PANTALLA DETALLE (EXISTENTE / NUEVO) ---
            if (!viewModel.isNewProduct) {
                Surface(color = Color(0xFFEEF4FB), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(viewModel.description, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("SKU: ${viewModel.scannedCode}", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Tipo de transacción:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.transactionType = "Entrada" }, colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.transactionType == "Entrada") Color(0xFF10B981) else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Entrada") }
                    Button(onClick = { viewModel.transactionType = "Salida" }, colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.transactionType == "Salida") Color(0xFFEF4444) else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Salida") }
                }
            } else {
                Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Producto Nuevo", fontWeight = FontWeight.Bold, color = Color.Red)
                            Text("No se encontró en la base de datos.", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = viewModel.description, onValueChange = { viewModel.description = it }, label = { Text("Descripción del producto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = viewModel.quantity,
                onValueChange = { viewModel.quantity = it },
                label = { Text(if (!viewModel.isNewProduct) "Cantidad a mover" else "Cantidad inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val q = viewModel.quantity.toIntOrNull() ?: 0
                    val isEntry = viewModel.transactionType == "Entrada"
                    val scannedSku = viewModel.scannedCode ?: ""

                    if (isOnline(context)) {
                        viewModel.checkProductExists(scannedSku) { existingProduct ->
                            if (existingProduct != null) {
                                // MODO ACTUALIZACIÓN (MOVIMIENTO)
                                // Si es entrada, el cambio es positivo (ej +5)
                                // Si es salida, el cambio es negativo (ej -5)
                                val qtyToSend = if (isEntry) q else -q
                                
                                // Enviamos el CAMBIO al backend, no el valor final
                                viewModel.recordMovement(existingProduct.id!!, qtyToSend, viewModel.transactionType)
                            } else {
                                // MODO CREACIÓN (POST)
                                val newProduct = Product(
                                    name = viewModel.description.ifEmpty { "Producto Nuevo" },
                                    sku = scannedSku,
                                    description = viewModel.description,
                                    stock = q
                                )
                                viewModel.addProduct(newProduct)
                            }
                        }
                    } else {
                        // Guardado Offline (Sync)
                        SyncManager(context).savePendingScan(PendingScan(scannedSku, viewModel.description, viewModel.quantity, viewModel.transactionType))
                        viewModel.resetScanner()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = viewModel.quantity.isNotBlank() && viewModel.description.isNotBlank()
            ) { Text("Confirmar Transacción", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = Color.Gray) }
        }
    }
}
