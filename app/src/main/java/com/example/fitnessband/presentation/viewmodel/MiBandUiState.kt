package com.example.fitnessband.presentation.viewmodel

import com.example.fitnessband.domain.model.ConnectionState
import com.example.fitnessband.domain.model.FitnessData

data class MiBandUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val fitnessData: FitnessData = FitnessData(),
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)