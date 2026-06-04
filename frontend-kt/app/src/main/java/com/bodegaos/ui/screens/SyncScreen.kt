package com.bodegaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodegaos.data.SyncManager
import com.bodegaos.data.model.PendingScan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.widget.Toast

@Composable
fun SyncScreen() {
    val context = LocalContext.current
    val syncManager = remember { SyncManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Estados de la pantalla
    var pendingItems by remember { mutableStateOf(syncManager.getPendingScans()) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncComplete by remember { mutableStateOf(false) }

    // Función para refrescar la lista cada vez que entras a la pantalla
    LaunchedEffect(Unit) {
        pendingItems = syncManager.getPendingScans()
        syncComplete = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Fondo gris claro
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Centro de Sincronización", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
        Text("Sube tus escaneos offline a la nube", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        if (syncComplete) {
            // --- ESTADO 1: ÉXITO TOTAL ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD1FAE5) // Fondo Verde Suave
            ) {
                Column(
                    modifier = Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(70.dp), tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Sincronización Exitosa!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                    Text("Todos los datos están en la nube", fontSize = 14.sp, color = Color(0xFF065F46))
                }
            }
        } else if (pendingItems.isEmpty()) {
            // --- ESTADO 2: NADA PENDIENTE ---
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Todo está al día", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("No hay movimientos offline pendientes.", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            // --- ESTADO 3: ELEMENTOS PENDIENTES ---
            Surface(
                color = Color(0xFFFEF3C7), // Fondo Amarillo Suave
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SyncProblem, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tienes ${pendingItems.size} elementos pendientes", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Text("Requieren conexión a internet para subir.", fontSize = 12.sp, color = Color(0xFF92400E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de elementos guardados
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pendingItems) { scan ->
                    SyncItemCard(scan, syncManager) {
                        pendingItems = syncManager.getPendingScans() // Refresca la vista
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            // Botón Maestro de Sincronización
            Button(
                onClick = {
                    if (!isOnline(context)) {
                        Toast.makeText(context, "No hay conexión a internet para sincronizar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSyncing = true
                    coroutineScope.launch {
                        delay(2000)

                        // PASAMOS TODOS LOS ELEMENTOS OFFLINE A LA NUBE
                        pendingItems.forEach { scan ->
                            com.bodegaos.data.CloudDatabase.addOrUpdateProduct(
                                context,
                                sku = scan.sku,
                                description = scan.description,
                                quantity = scan.quantity.toIntOrNull() ?: 0,
                                isEntry = scan.type == "Entrada",
                                isSync = true
                            )
                        }
                        syncManager.clearSyncQueue() // Limpiamos la bóveda
                        pendingItems = emptyList() // Vaciamos la lista visual
                        isSyncing = false
                        syncComplete = true // Mostramos pantalla de éxito
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp).padding(bottom = 10.dp),
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Subiendo a la nube...", fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sincronizar Ahora", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SyncItemCard(scan: PendingScan, syncManager: SyncManager, onRefresh: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Variables temporales para la edición
    var editSku by remember { mutableStateOf(scan.sku) }
    var editDesc by remember { mutableStateOf(scan.description) }
    var editQty by remember { mutableStateOf(scan.quantity) }

    // Diálogo de Eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar escaneo offline") },
            text = { Text("¿Deseas eliminar '${scan.description}'? Ya no se subirá a la nube.") },
            confirmButton = {
                TextButton(onClick = {
                    syncManager.deletePendingScan(scan.timestamp)
                    showDeleteDialog = false
                    onRefresh()
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de Editar
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar escaneo offline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Descripción") }, singleLine = true)
                    OutlinedTextField(value = editSku, onValueChange = { editSku = it }, label = { Text("SKU") }, singleLine = true)
                    OutlinedTextField(value = editQty, onValueChange = { editQty = it }, label = { Text("Cantidad") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    syncManager.updatePendingScan(scan.timestamp, editSku, editDesc, editQty)
                    showEditDialog = false
                    onRefresh()
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFEEF4FB), CircleShape), contentAlignment = Alignment.Center) {
                Icon(if (scan.type == "Entrada") Icons.Default.ArrowDownward else Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = scan.description.ifEmpty { "Producto Nuevo" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(text = "SKU: ${scan.sku}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = scan.quantity, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                Row {
                    IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}