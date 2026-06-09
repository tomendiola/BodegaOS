package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.model.PendingScan
import com.bodegaos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _pendingScans = MutableStateFlow<List<PendingScan>>(emptyList())
    val pendingScans: StateFlow<List<PendingScan>> = _pendingScans

    fun loadPendingScans() {
        viewModelScope.launch {
            _pendingScans.value = repository.getPendingScans()
        }
    }

    fun syncDataWithServer() {
        viewModelScope.launch {
            val scans = repository.getPendingScans()

            // Aquí iría tu lógica de Retrofit para enviar al servidor
            // por cada elemento en "scans".

            // Una vez enviado, limpiamos la base de datos local:
            repository.clearPendingScans()
            _pendingScans.value = emptyList()
        }
    }
}