package com.example.fitnessband.data.constants

import java.util.UUID

object MiBand6Constants {
    // Your Mi Band 6 details
    const val MAC_ADDRESS = "D0:FF:19:FA:06:9B"

    // Auth key from Xiaomi Wearable logs
    val AUTH_KEY = byteArrayOf(
        0x6b, 0x5c, 0xdc.toByte(), 0x76, 0xb7.toByte(), 0x6c,
        0x17, 0x98.toByte(), 0xac.toByte(), 0xc4.toByte(),
        0xe9.toByte(), 0x31, 0x7e, 0x68, 0xea.toByte(), 0x20
    )

    // Service UUIDs
    val UUID_SERVICE_MIBAND = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val UUID_SERVICE_HEART_RATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    // Characteristic UUIDs
    val UUID_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700")
    val UUID_CHARACTERISTIC_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700")
    val UUID_CHARACTERISTIC_HEART_RATE_MEASURE =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val UUID_CHARACTERISTIC_HEART_RATE_CONTROL =
        UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")
    val UUID_CHARACTERISTIC_BATTERY = UUID.fromString("00000006-0000-3512-2118-0009af100700")

    // Descriptor UUID for notifications
    val UUID_DESCRIPTOR_UPDATE_NOTIFICATION =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Authentication Commands
    val CMD_AUTH_REQUEST_RANDOM = byteArrayOf(0x02, 0x00)
    val CMD_AUTH_SEND_KEY = byteArrayOf(0x03, 0x00)

    // Heart Rate Commands
    val CMD_START_HEART_RATE_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x01)
    val CMD_STOP_HEART_RATE_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x00)
    val CMD_START_HEART_RATE_MANUAL = byteArrayOf(0x15, 0x02, 0x01)
}