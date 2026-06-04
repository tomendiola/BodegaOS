package com.bodegaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodegaos.data.CloudDatabase
import com.bodegaos.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val history = CloudDatabase.transactionLog

    // Estado para saber qué filtro está activo
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filterOptions = listOf("Todos", "Entrada", "Salida", "Sync", "Edición", "Eliminación")

    // Aplicamos el filtro a la lista
    val filteredHistory = if (selectedFilter == "Todos") {
        history
    } else {
        history.filter { it.type == selectedFilter }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Historial de Movimientos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
        Text("Registro detallado de la bodega", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        // Fila de Filtros Deslizables
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { filterName ->
                FilterChip(
                    selected = selectedFilter == filterName,
                    onClick = { selectedFilter = filterName },
                    label = { Text(filterName, fontWeight = if (selectedFilter == filterName) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredHistory.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No hay resultados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredHistory) { transaction ->
                    HistoryItemCard(transaction)
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun HistoryItemCard(transaction: Transaction) {
    // Definir colores y logos para los 5 tipos de acciones CRUD
    val (bgColor, iconColor, icon) = when (transaction.type) {
        "Entrada" -> Triple(Color(0xFFD1FAE5), Color(0xFF10B981), Icons.Default.ArrowDownward) // Verde
        "Salida" -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), Icons.Default.ArrowUpward) // Rojo
        "Sync" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Default.Sync) // Naranja
        "Edición" -> Triple(Color(0xFFDBEAFE), Color(0xFF3B82F6), Icons.Default.Edit) // Azul
        "Eliminación" -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), Icons.Default.Delete) // Gris
        else -> Triple(Color.White, Color.Black, Icons.Default.Info)
    }

    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = formatter.format(Date(transaction.timestamp))

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = bgColor) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.description, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(text = "SKU: ${transaction.sku}", fontSize = 12.sp, color = Color(0xFF4B5563))
            }
            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (transaction.type) {
                    "Salida", "Eliminación" -> "-"
                    "Entrada", "Sync" -> "+"
                    else -> "" // Edición no lleva signo
                }
                Text(text = "$prefix${transaction.quantity}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = iconColor)
                Text(text = dateString, fontSize = 10.sp, color = Color(0xFF4B5563))
            }
        }
    }
}