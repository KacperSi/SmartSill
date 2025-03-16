# Smart Pot Configurator - Android App

## Overview
Smart Pot Configurator is an Android application built with **Android Studio** that allows users to configure and manage their smart self-watering pots. Users can add new pots, edit their settings, and monitor their status directly from their mobile devices.

## Features
- **Dashboard**: View a list of all configured smart pots.
- **Add New Pots**: Register a new smart pot and connect it to the system.
- **Pot Configuration**:
  - Edit watering schedule.
  - Adjust soil moisture thresholds.
  - Enable/disable notifications.
  - Configure authentication settings.
- **Bluetooth & Wi-Fi Setup**: Securely connect smart pots via BLE and Wi-Fi.
- **Real-Time Monitoring**: Display soil moisture levels and watering history.
- **Secure Authentication**: Uses a **challenge-response mechanism over HTTPS** combined with **BLE key exchange** for enhanced security.

## Tech Stack
- **Language**: Java
- **Framework**: Android SDK
- **Network Communication**: Retrofit (for HTTPS API requests)
- **Bluetooth**: Android BLE API
- **Local Storage**: Room Database
- **UI Components**: Jetpack Compose (or XML layouts)

## Usage
### 3. **Monitoring and Notifications**
- View real-time soil moisture data.
- Enable push notifications for low moisture alerts.



## Security Considerations
- **Encrypted Communication**: All data is transmitted over HTTPS.
- **Two-Factor Authentication**: Requires BLE key exchange for critical actions.
- **Local Data Protection**: Uses Android Keystore for secure storage of credentials.

# Visualization

![dodawanie_urzadzenia](https://github.com/user-attachments/assets/c696089a-99f8-44d8-b0e7-7057f0a6324c)
![konfiguracja](https://github.com/user-attachments/assets/d615398a-cc57-469c-9290-605d8a0b42b1)
