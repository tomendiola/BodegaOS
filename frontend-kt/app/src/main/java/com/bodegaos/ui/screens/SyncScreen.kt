package com.bodegaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bodegaos.viewmodel.SyncViewModel

@Composable
fun SyncScreen(viewModel: SyncViewModel = hiltViewModel()) {
    val pendingScans by viewModel.pendingScans.collectAsState()

    // Cargar los datos de Room cada vez que se abre la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadPendingScans()
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Cola de Sincronización", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("Movimientos guardados offline", color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        if (pendingScans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todo está sincronizado \uD83D\uDE0E", color = Color.Gray)
            }
        } else {
            Button(
                onClick = { viewModel.syncDataWithServer() },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sincronizar ${pendingScans.size} Movimientos", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pendingScans) { scan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(scan.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("SKU: ${scan.sku}", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (scan.type == "Entrada") Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${scan.type}: ${scan.quantity}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (scan.type == "Entrada") Color(0xFF065F46) else Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}