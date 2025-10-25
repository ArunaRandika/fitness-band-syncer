package com.example.fitnessband.domain.model

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Authenticating : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}