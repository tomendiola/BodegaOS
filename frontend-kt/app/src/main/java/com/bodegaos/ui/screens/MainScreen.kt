package com.bodegaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bodegaos.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var autoStartScan by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = White,
                contentColor = Gray500,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Color(0xFFEEF4FB))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                    label = { Text("Inventario") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Color(0xFFEEF4FB))
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2 
                        autoStartScan = true
                    },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Escanear") },
                    label = { Text("Escanear") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Color(0xFFEEF4FB))
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Color(0xFFEEF4FB))
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Sync, contentDescription = "Sync") },
                    label = { Text("Sync") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = Color(0xFFEEF4FB))
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Aquí es donde la barra decide qué pantalla mostrar arriba
            when (selectedTab) {
                0 -> DashboardScreen(onLogout = onLogout, onNavigateToScanner = {
                    selectedTab = 2
                    autoStartScan = true
                })
                1 -> InventoryScreen()
                2 -> ScannerScreen()
                3 -> HistoryScreen()
                4 -> SyncScreen()
            }
        }
    }
}
