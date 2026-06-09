package com.bodegaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bodegaos.ui.theme.*
import com.bodegaos.viewmodel.HistoryViewModel
import com.bodegaos.viewmodel.InventoryViewModel

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.bodegaos.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    invViewModel: InventoryViewModel = hiltViewModel(), // Actualizado a Hilt
    histViewModel: HistoryViewModel = hiltViewModel()   // Actualizado a Hilt
) {
    val context = LocalContext.current
    val pendingSyncs by viewModel.pendingSyncsCount.collectAsState()
    val inventory by invViewModel.inventoryState.collectAsState()
    val history by histViewModel.historyState.collectAsState()

    // Forzar recarga al entrar a la pantalla principal
    LaunchedEffect(Unit) {
        viewModel.loadStats()
        invViewModel.loadInventory()
        histViewModel.loadHistory()
    }

    val totalUnits = inventory.sumOf { it.stock }
    val activeSkus = inventory.size

    // Obtenemos directamente la lista de productos que tienen bajo stock
    val lowStockItems = inventory.filter { it.stock in 1..9 }
    val outOfStockItems = inventory.filter { it.stock == 0 }

    val todaysMoves = history.size

    // Cálculo para la barra de salud (porcentaje de productos con buen stock)
    val healthyItemsCount = inventory.count { it.stock >= 10 }
    val healthPercentage = if (activeSkus > 0) healthyItemsCount.toFloat() / activeSkus.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "Hola, Admin", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Gray800)
                Text(text = "Administrador de bodega", fontSize = 12.sp, color = Gray500, fontWeight = FontWeight.Medium)
            }
            Surface(modifier = Modifier.size(40.dp).clickable { onLogout() }, shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Gray200), color = White) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = Gray800) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Card (Resumen Principal)
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Primary) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.background(White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Bodega Central · Turno Matutino", color = White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFFBAD6F0), modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "$totalUnits", color = White, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "unidades registradas en inventario", color = White.copy(alpha = 0.7f), fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    HeroStatItem(value = "$activeSkus", label = "SKUs activos", modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(White.copy(alpha = 0.15f)))
                    HeroStatItem(value = "$todaysMoves", label = "Movimientos", modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(White.copy(alpha = 0.15f)))
                    HeroStatItem(value = "$pendingSyncs", label = "Sin sync", modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 1: SALUD DEL INVENTARIO ---
        Text(text = "Salud del Inventario", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray800)
        Spacer(modifier = Modifier.height(10.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Gray200), color = White) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Óptimo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                    Text(text = "${(healthPercentage * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Gray800)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { healthPercentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFF3F4F6),
                    strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "$healthyItemsCount de $activeSkus productos tienen stock suficiente.", fontSize = 11.sp, color = Gray500)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 2: ATENCIÓN REQUERIDA (ALERTAS) ---
        Text(text = "Atención requerida", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray800)
        Spacer(modifier = Modifier.height(10.dp))

        if (outOfStockItems.isEmpty() && lowStockItems.isEmpty()) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Color(0xFFEEF4FB)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Todo está en orden. No hay alertas de stock.", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            // Mostrar productos agotados primero
            outOfStockItems.forEach { product ->
                AlertItemCard(icon = Icons.Default.ErrorOutline, iconColor = Color.White, iconBg = Color(0xFFEF4444), title = product.description, sku = "SKU: ${product.sku}", statusText = "Agotado")
            }
            // Luego mostrar productos con bajo stock
            lowStockItems.forEach { product ->
                AlertItemCard(icon = Icons.Default.WarningAmber, iconColor = Color(0xFFD97706), iconBg = Color(0xFFFEF3C7), title = product.description, sku = "SKU: ${product.sku}", statusText = "Quedan ${product.stock}")
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HeroStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = White.copy(alpha = 0.65f), fontSize = 11.sp)
    }
}

// Nueva tarjetita para las alertas de stock
@Composable
fun AlertItemCard(icon: ImageVector, iconColor: Color, iconBg: Color, title: String, sku: String, statusText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200),
        color = White
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gray800)
                Text(text = sku, fontSize = 12.sp, color = Gray500)
            }
            Text(text = statusText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (statusText == "Agotado") Color(0xFFEF4444) else Color(0xFFD97706))
        }
    }
}
