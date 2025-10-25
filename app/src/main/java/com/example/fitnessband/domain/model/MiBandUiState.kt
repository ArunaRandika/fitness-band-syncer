package com.example.fitnessband.domain.model

data class MiBandUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val fitnessData: FitnessData = FitnessData(),
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)