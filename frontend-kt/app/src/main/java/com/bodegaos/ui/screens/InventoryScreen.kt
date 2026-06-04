package com.bodegaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodegaos.data.CloudDatabase
import com.bodegaos.data.Product

import androidx.compose.ui.platform.LocalContext

@Composable
fun InventoryScreen() {
    // Leemos los productos directamente de nuestra "nube"
    val products = CloudDatabase.inventory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Inventario Central", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
        Text("Productos almacenados en la base de datos", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        if (products.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Inventario vacío", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(products) { product ->
                    InventoryItemCard(product)
                }
                item { Spacer(modifier = Modifier.height(100.dp)) } // Espacio para la barra inferior
            }
        }
    }
}

@Composable
fun InventoryItemCard(product: Product) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Memoria para la edición (¡Ahora incluimos el SKU!)
    var editSku by remember { mutableStateOf(product.sku) }
    var editDesc by remember { mutableStateOf(product.description) }
    var editStock by remember { mutableStateOf(product.stock.toString()) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar '${product.description}'? Esta acción quedará registrada en el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    CloudDatabase.deleteProduct(context, product.sku)
                    showDeleteDialog = false
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ¡El SKU ahora es editable!
                    OutlinedTextField(value = editSku, onValueChange = { editSku = it }, label = { Text("SKU") }, singleLine = true)
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Descripción") }, singleLine = true)
                    OutlinedTextField(value = editStock, onValueChange = { editStock = it }, label = { Text("Stock Actual") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Enviamos el SKU viejo (para encontrarlo) y el nuevo (para reemplazarlo)
                    CloudDatabase.editProduct(context, product.sku, editSku, editDesc, editStock.toIntOrNull() ?: product.stock)
                    showEditDialog = false
                }) { Text("Guardar", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFEEF4FB), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.description, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(text = "SKU: ${product.sku}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${product.stock}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Row {
                    IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}