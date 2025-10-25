package com.example.fitnessband.domain.repository


import com.example.fitnessband.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow

interface MiBandRepository {
    val connectionState: Flow<ConnectionState>
    val steps: Flow<Int>
    val heartRate: Flow<Int>
    val battery: Flow<Int>

    fun connect(macAddress: String)
    fun disconnect()
    fun startHeartRateMonitoring()
    fun stopHeartRateMonitoring()
}