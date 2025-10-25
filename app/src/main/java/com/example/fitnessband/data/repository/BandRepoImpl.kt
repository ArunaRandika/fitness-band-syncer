package com.example.fitnessband.data.repository


import com.example.fitnessband.data.btdatasource.BleManager
import com.example.fitnessband.domain.model.ConnectionState
import com.example.fitnessband.domain.repository.MiBandRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MiBandRepositoryImpl @Inject constructor(
    private val bleManager: BleManager
) : MiBandRepository {

    override val connectionState: Flow<ConnectionState> = bleManager.connectionState

    override val steps: Flow<Int> = bleManager.steps

    override val heartRate: Flow<Int> = bleManager.heartRate

    override val battery: Flow<Int> = bleManager.battery

    override fun connect(macAddress: String) {
        bleManager.connect(macAddress)
    }

    override fun disconnect() {
        bleManager.disconnect()
    }

    override fun startHeartRateMonitoring() {
        bleManager.startHeartRateMonitoring()
    }

    override fun stopHeartRateMonitoring() {
        bleManager.stopHeartRateMonitoring()
    }
}