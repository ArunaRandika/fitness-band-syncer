package com.example.fitnessband.domain.model

data class FitnessData(
    val steps: Int = 0,
    val heartRate: Int = 0,
    val battery: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)