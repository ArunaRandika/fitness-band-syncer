package com.example.fitnessband.data.btdatasource


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.example.fitnessband.data.constants.MiBand6Constants
import com.example.fitnessband.domain.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    private val context: Context
) {
    private var bluetoothGatt: BluetoothGatt? = null
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _battery = MutableStateFlow(-1)
    val battery: StateFlow<Int> = _battery.asStateFlow()

    private var isAuthenticated = false

    @SuppressLint("MissingPermission")
    fun connect(macAddress: String) {
        Log.d(TAG, "🔵 Connecting to: $macAddress")
        _connectionState.value = ConnectionState.Connecting

        val device = adapter?.getRemoteDevice(macAddress)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "✅ Connected to GATT server")
                    _connectionState.value = ConnectionState.Authenticating
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "❌ Disconnected from GATT server")
                    _connectionState.value = ConnectionState.Disconnected
                    isAuthenticated = false
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "📡 Services discovered")
                authenticate(gatt)
            } else {
                Log.e(TAG, "❌ Service discovery failed: $status")
                _connectionState.value = ConnectionState.Error("Service discovery failed")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleCharacteristicUpdate(characteristic)
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "✍️ Write successful: ${characteristic.uuid}")
            } else {
                Log.e(TAG, "❌ Write failed: ${characteristic.uuid}, status: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "✍️ Descriptor write successful")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristicUpdate(characteristic)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun authenticate(gatt: BluetoothGatt) {
        Log.d(TAG, "🔐 Starting authentication...")

        val service = gatt.getService(MiBand6Constants.UUID_SERVICE_MIBAND)
        val authCharacteristic =
            service?.getCharacteristic(MiBand6Constants.UUID_CHARACTERISTIC_AUTH)

        if (authCharacteristic == null) {
            Log.e(TAG, "❌ Auth characteristic not found")
            _connectionState.value = ConnectionState.Error("Auth characteristic not found")
            return
        }

        // Enable auth notifications
        gatt.setCharacteristicNotification(authCharacteristic, true)

        val descriptor =
            authCharacteristic.getDescriptor(MiBand6Constants.UUID_DESCRIPTOR_UPDATE_NOTIFICATION)
        descriptor?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
        }

        // Request random auth number
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            authCharacteristic.value = MiBand6Constants.CMD_AUTH_REQUEST_RANDOM
            gatt.writeCharacteristic(authCharacteristic)
            Log.d(TAG, "📤 Sent auth request")
        }, 500)
    }

    @SuppressLint("MissingPermission")
    private fun handleCharacteristicUpdate(characteristic: BluetoothGattCharacteristic) {
        when (characteristic.uuid) {
            MiBand6Constants.UUID_CHARACTERISTIC_AUTH -> {
                handleAuthResponse(characteristic.value)
            }

            MiBand6Constants.UUID_CHARACTERISTIC_STEPS -> {
                val steps = parseSteps(characteristic.value)
                Log.d(TAG, "👣 Steps: $steps")
                _steps.value = steps
            }

            MiBand6Constants.UUID_CHARACTERISTIC_HEART_RATE_MEASURE -> {
                val hr = parseHeartRate(characteristic.value)
                if (hr > 0) {
                    Log.d(TAG, "❤️ Heart Rate: $hr bpm")
                    _heartRate.value = hr
                }
            }

            MiBand6Constants.UUID_CHARACTERISTIC_BATTERY -> {
                val battery = parseBattery(characteristic.value)
                Log.d(TAG, "🔋 Battery: $battery%")
                _battery.value = battery
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleAuthResponse(data: ByteArray) {
        if (data.isEmpty()) return

        Log.d(TAG, "🔐 Auth response: ${data.toHexString()}")

        when (data[0].toInt()) {
            0x10 -> {
                // Check what kind of response this is
                if (data.size >= 3) {
                    when (data[2].toInt()) {
                        0x01 -> {
                            // Authentication successful!
                            Log.d(TAG, "✅ Authentication successful!")
                            isAuthenticated = true
                            _connectionState.value = ConnectionState.Connected
                            initializeDevice()
                        }

                        0x04 -> {
                            Log.e(TAG, "❌ Authentication failed - invalid key")
                            _connectionState.value = ConnectionState.Error("Invalid auth key")
                        }

                        else -> {
                            // Received random number, send encrypted auth key
                            if (data.size >= 17) {
                                val randomNumber = data.copyOfRange(3, 19)
                                val encryptedKey = encryptAuthKey(randomNumber)

                                val authCommand = byteArrayOf(0x03, 0x00) + encryptedKey

                                val gatt = bluetoothGatt ?: return
                                val service = gatt.getService(MiBand6Constants.UUID_SERVICE_MIBAND)
                                val authChar =
                                    service?.getCharacteristic(MiBand6Constants.UUID_CHARACTERISTIC_AUTH)

                                authChar?.let {
                                    it.value = authCommand
                                    gatt.writeCharacteristic(it)
                                    Log.d(TAG, "📤 Sent encrypted auth key")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun encryptAuthKey(randomNumber: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            val keySpec = SecretKeySpec(MiBand6Constants.AUTH_KEY, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            cipher.doFinal(randomNumber)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Encryption error", e)
            ByteArray(16)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeDevice() {
        val gatt = bluetoothGatt ?: return

        Log.d(TAG, "🚀 Initializing device...")

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // Enable step notifications
            enableNotifications(gatt, MiBand6Constants.UUID_CHARACTERISTIC_STEPS)

            // Enable heart rate notifications
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                enableNotifications(gatt, MiBand6Constants.UUID_CHARACTERISTIC_HEART_RATE_MEASURE)
            }, 500)

            // Read battery
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val service = gatt.getService(MiBand6Constants.UUID_SERVICE_MIBAND)
                val batteryChar =
                    service?.getCharacteristic(MiBand6Constants.UUID_CHARACTERISTIC_BATTERY)
                batteryChar?.let { gatt.readCharacteristic(it) }
            }, 1000)

            // Start continuous heart rate monitoring
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                startHeartRateMonitoring()
            }, 1500)
        }, 500)
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt, characteristicUuid: java.util.UUID) {
        val service =
            if (characteristicUuid == MiBand6Constants.UUID_CHARACTERISTIC_HEART_RATE_MEASURE) {
                gatt.getService(MiBand6Constants.UUID_SERVICE_HEART_RATE)
            } else {
                gatt.getService(MiBand6Constants.UUID_SERVICE_MIBAND)
            }

        val characteristic = service?.getCharacteristic(characteristicUuid)

        characteristic?.let {
            gatt.setCharacteristicNotification(it, true)

            val descriptor = it.getDescriptor(MiBand6Constants.UUID_DESCRIPTOR_UPDATE_NOTIFICATION)
            descriptor?.let { desc ->
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(desc)
                Log.d(TAG, "🔔 Enabled notifications for: $characteristicUuid")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startHeartRateMonitoring() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(MiBand6Constants.UUID_SERVICE_HEART_RATE)
        val controlChar =
            service?.getCharacteristic(MiBand6Constants.UUID_CHARACTERISTIC_HEART_RATE_CONTROL)

        controlChar?.let {
            it.value = MiBand6Constants.CMD_START_HEART_RATE_CONTINUOUS
            gatt.writeCharacteristic(it)
            Log.d(TAG, "💓 Started continuous heart rate monitoring")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopHeartRateMonitoring() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(MiBand6Constants.UUID_SERVICE_HEART_RATE)
        val controlChar =
            service?.getCharacteristic(MiBand6Constants.UUID_CHARACTERISTIC_HEART_RATE_CONTROL)

        controlChar?.let {
            it.value = MiBand6Constants.CMD_STOP_HEART_RATE_CONTINUOUS
            gatt.writeCharacteristic(it)
            Log.d(TAG, "💓 Stopped continuous heart rate monitoring")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        isAuthenticated = false
        _connectionState.value = ConnectionState.Disconnected
        Log.d(TAG, "🔌 Disconnected")
    }

    private fun parseSteps(data: ByteArray): Int {
        return if (data.size >= 2) {
            (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
        } else 0
    }

    private fun parseHeartRate(data: ByteArray): Int {
        return if (data.isNotEmpty()) {
            data[0].toInt() and 0xFF
        } else 0
    }

    private fun parseBattery(data: ByteArray): Int {
        return if (data.isNotEmpty()) {
            data[0].toInt() and 0xFF
        } else -1
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

    companion object {
        private const val TAG = "MiBand6BLE"
    }
}