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
import com.bodegaos.data.CloudDatabase
import com.bodegaos.data.SyncManager
import com.bodegaos.data.model.PendingScan
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    // Memoria blindada de la pantalla
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var productExists by remember { mutableStateOf(false) }
    var justAddedMessage by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("Entrada") }

    var isManualEntry by remember { mutableStateOf(false) }
    var manualInput by remember { mutableStateOf("") }

    // Autocompletado inteligente al detectar código
    LaunchedEffect(scannedCode) {
        if (scannedCode != null) {
            val existing = CloudDatabase.inventory.find { it.sku == scannedCode }
            if (existing != null) {
                productExists = true
                description = existing.description
            } else {
                productExists = false
                description = ""
            }
            quantity = ""
            transactionType = "Entrada"
            justAddedMessage = false
            isManualEntry = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Escáner de Productos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(20.dp))

        if (scannedCode == null) {
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
                            scanner.startScan().addOnSuccessListener { barcode -> scannedCode = barcode.rawValue }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Abrir Cámara", fontSize = 16.sp) }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = { isManualEntry = !isManualEntry }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(if (isManualEntry) Icons.Default.Close else Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isManualEntry) "Cancelar Manual" else "Ingreso Manual")
            }

            if (isManualEntry) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Escribe el código de barras o SKU") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (manualInput.isNotBlank()) {
                            focusManager.clearFocus()
                            scannedCode = manualInput
                            manualInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Buscar Producto") }
            }

        } else if (justAddedMessage) {
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
                onClick = { scannedCode = null; justAddedMessage = false },
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) { Text("Escanear otro producto", fontSize = 16.sp) }

        } else {
            // --- PANTALLA DETALLE (EXISTENTE / NUEVO) ---
            if (productExists) {
                Surface(color = Color(0xFFEEF4FB), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(description, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("SKU: $scannedCode", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Tipo de transacción:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { transactionType = "Entrada" }, colors = ButtonDefaults.buttonColors(containerColor = if (transactionType == "Entrada") Color(0xFF10B981) else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Entrada") }
                    Button(onClick = { transactionType = "Salida" }, colors = ButtonDefaults.buttonColors(containerColor = if (transactionType == "Salida") Color(0xFFEF4444) else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Salida") }
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
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción del producto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(if (productExists) "Cantidad a mover" else "Cantidad inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val q = quantity.toIntOrNull() ?: 0
                    val isEntry = transactionType == "Entrada"

                    if (isOnline(context)) {
                        CloudDatabase.addOrUpdateProduct(context, scannedCode!!, description, q, isEntry)
                    } else {
                        SyncManager(context).savePendingScan(PendingScan(scannedCode!!, description, quantity, transactionType))
                    }
                    justAddedMessage = true
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = quantity.isNotBlank() && description.isNotBlank()
            ) { Text("Confirmar Transacción", fontSize = 16.sp) }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = { scannedCode = null }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = Color.Gray) }
        }
    }
}