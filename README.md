# 🏃‍♂️ Mi Band 6 Fitness Monitor

A native Android application that connects directly to Xiaomi Mi Band 6 via Bluetooth Low Energy (BLE) to monitor real-time fitness data including steps, heart rate, and battery level.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple?style=flat&logo=kotlin)
![Android](https://img.shields.io/badge/Android-26+-green?style=flat&logo=android)
![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.09-blue?style=flat)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat)

## ✨ Features

- 🔐 **Direct BLE Authentication** - Connects directly to Mi Band 6 without Mi Fit/Zepp app
- 👣 **Real-time Step Tracking** - Live step count updates
- ❤️ **Heart Rate Monitoring** - Continuous heart rate measurement
- 🔋 **Battery Status** - Monitor your band's battery level
- 📱 **Modern UI** - Beautiful Material Design 3 interface with Jetpack Compose
- 🏗️ **Clean Architecture** - Domain, Data, and Presentation layers
- 💉 **Dependency Injection** - Hilt for scalable architecture

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── data/                      # Data Layer
│   ├── miband/
│   │   ├── MiBand6BleManager.kt      # BLE communication
│   │   └── MiBand6Constants.kt       # UUIDs & Commands
│   └── repository/
│       └── MiBandRepositoryImpl.kt   # Repository implementation
│
├── domain/                    # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── ConnectionState.kt
│   │   └── FitnessData.kt
│   ├── repository/
│   │   └── MiBandRepository.kt       # Repository interface
│   └── usecase/
│       ├── ConnectMiBandUseCase.kt
│       ├── DisconnectMiBandUseCase.kt
│       ├── ObserveConnectionStateUseCase.kt
│       ├── ObserveFitnessDataUseCase.kt
│       └── StartHeartRateMonitoringUseCase.kt
│
├── presentation/              # Presentation Layer
│   ├── ui/
│   │   └── MiBandScreen.kt           # Compose UI
│   └── viewmodel/
│       ├── MiBandViewModel.kt
│       └── MiBandUiState.kt
│
└── di/                        # Dependency Injection
    └── AppModule.kt
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android device with API 26+ (Android 8.0+)
- Xiaomi Mi Band 6
- Mi Band 6 authentication key

### Getting Your Mi Band 6 Auth Key

Before using this app, you need to extract your Mi Band's authentication key:

1. **Install Xiaomi Wearable app** and pair your Mi Band 6
2. **Enable USB Debugging** on your Android device
3. **Connect via ADB** and pull the logs:
   ```bash
   adb pull /storage/emulated/0/Android/data/com.xiaomi.wearable/files/
   ```
4. **Search for your auth key** in the logs:
   ```bash
   grep -r "AUTH_SECRET" .
   ```
5. **Copy the 32-character hex string** (e.g., `6b5cdc76b76c1798acc4e9317e68ea20`)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/miband6-fitness-monitor.git
   cd miband6-fitness-monitor
   ```

2. **Update constants with your Mi Band details**
   
   Open `data/miband/MiBand6Constants.kt` and update:
   ```kotlin
   object MiBand6Constants {
       const val MAC_ADDRESS = "XX:XX:XX:XX:XX:XX"  // Your Mi Band MAC address
       
       val AUTH_KEY = byteArrayOf(
           0x6b, 0x5c, 0xdc, 0x76, 0xb7, 0x6c,  // Your auth key bytes
           0x17, 0x98, 0xac, 0xc4, 0xe9, 0x31,
           0x7e, 0x68, 0xea, 0x20
       )
   }
   ```

3. **Build and run**
   ```bash
   ./gradlew installDebug
   ```

## 📱 Usage

1. **Grant Bluetooth permissions** when prompted
2. **Tap "Connect to Mi Band 6"**
3. **Confirm pairing** on your Mi Band when it vibrates
4. **View live data** - Steps, heart rate, and battery level

## 🛠️ Tech Stack

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Async**: Kotlin Coroutines & Flow
- **BLE**: Android Bluetooth Low Energy APIs
- **Permissions**: Accompanist Permissions

### Dependencies

```kotlin
// Compose
implementation("androidx.compose:compose-bom:2024.09.00")
implementation("androidx.compose.material3:material3")

// Hilt
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

// Permissions
implementation("com.google.accompanist:accompanist-permissions:0.34.0")
```

## 🔐 How Authentication Works

The Mi Band 6 uses a challenge-response authentication mechanism:

```
1. App → Band: Request random number (0x02 0x00)
2. Band → App: Send 16-byte random number
3. App encrypts random with AUTH_KEY using AES/ECB
4. App → Band: Send encrypted result (0x03 0x00 + encrypted)
5. Band verifies and responds with success/failure
6. ✅ Authenticated! Start receiving data
```

## 📊 BLE Services & Characteristics

| Service | UUID | Purpose |
|---------|------|---------|
| Mi Band Service | `0000fee0-...` | Steps, battery, notifications |
| Auth Service | `0000fee1-...` | Authentication |
| Heart Rate | `0000180d-...` | Heart rate monitoring |

| Characteristic | UUID | Data Type |
|----------------|------|-----------|
| Auth | `00000009-...` | Authentication handshake |
| Steps | `00000007-...` | Step count (16-bit int) |
| Heart Rate | `00002a37-...` | BPM (8-bit int) |
| Battery | `00000006-...` | Percentage (8-bit int) |

## 🐛 Troubleshooting

### Connection Issues

**Problem**: "Connection timeout"
- **Solution**: Ensure Mi Band is not connected to Mi Fit/Zepp app
- **Solution**: Put band in pairing mode (factory reset if needed)

**Problem**: "Auth characteristic not found"
- **Solution**: Mi Band might use different service UUID - check logs

**Problem**: "Authentication failed"
- **Solution**: Verify your AUTH_KEY is correct (32 hex characters)

### MAC Address Changed

After factory reset, MAC address changes. Find new address:
```bash
# Using ADB
adb shell dumpsys bluetooth_manager | grep -i "mi band"

# Or use nRF Connect app to scan
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Future Enhancements

- [ ] Sleep tracking
- [ ] Activity detection (walking, running, cycling)
- [ ] Custom notifications
- [ ] Alarm management
- [ ] Historical data charts
- [ ] Export data to CSV/JSON
- [ ] Multi-device support
- [ ] Dark theme customization

## 📚 Resources & Credits

- **Gadgetbridge** - Open source Mi Band support (https://codeberg.org/Freeyourgadget/Gadgetbridge)
- **Bluetooth SIG** - BLE specifications (https://www.bluetooth.com/specifications/)
- **Mi Band Community** - Protocol documentation and reverse engineering

## ⚠️ Disclaimer

This is an unofficial app and is not affiliated with or endorsed by Xiaomi. Use at your own risk. The app requires extracting authentication keys from the official Xiaomi Wearable app, which may violate terms of service.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```

**⭐ If you found this project helpful, please give it a star!**

Made with ❤️ and Kotlin
