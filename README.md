# BitCount 🔢⚡

**BitCount** is a modern, feature-rich Android application built with **Jetpack Compose** and **Material 3**. It provides deep binary and bit-level analysis, multi-base number conversion, customizable bit-widths, interactive bit inspection, calculation history, and flexible data export/import capabilities.

---

## 📱 Features

### 🧮 Multi-Base Conversion & Analysis
- **Supports Multiple Bases**: Input numbers in **Decimal**, **Binary**, **Octal**, or **Hexadecimal**.
- **Real-Time Conversion**: Instantly converts and displays values across all bases simultaneously.
- **Large Number Support**: Utilizes `BigInteger` for arbitrary-precision mathematical operations.

### ⚙️ Bit-Level Inspection
- **Bit Density**: Calculates set bit percentage (`1`s density) relative to chosen bit-width.
- **Bit Statistics**: Shows set bits (`1`s), cleared bits (`0s`), highest set bit position, and lowest set bit position.
- **Interactive Bit Row**: Visualizes binary representation with grouped bit views for easy reading.
- **Bit Position Query**: Query individual bit states (0 or 1) at any given bit index.
- **Set & Clear Positions**: Detailed list of all active set bit indices and cleared bit indices.

### 📐 Flexible Bit-Width Modes
- **Natural Width**: Uses the minimum bit length required to represent the number.
- **Standard Fixed Widths**: Quick toggles for **8-bit (Byte)**, **16-bit (Short)**, **32-bit (Int)**, and **64-bit (Long)**.
- **Custom Bit Width**: Define custom bit lengths up to 1024 bits.

### 📜 History & Saved Calculations
- **Calculation History**: Automatic timestamped history tracking for all analyzed inputs.
- **Bookmarks**: Star important calculations for quick access.
- **Filtering & Search**: Filter history by base type, bookmarked status, or search query.
- **Export & Import**: Export calculation history as JSON files and import them seamlessly.

### 🎨 Customization & Modern UI
- **Material 3 Design**: Built using standard Material 3 components and color schemes.
- **Theme Options**: Dynamic Colors (Android 12+ Material You), Dark Mode, Light Mode, and System Default.
- **Haptic Feedback**: Subtle haptic responses for interactive buttons and toggles.
- **Smooth Animations**: Seamless transitions powered by Compose Navigation.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **State Management**: `StateFlow` & `collectAsState`
- **Navigation**: Compose Navigation Component
- **Persistence**: `SharedPreferences` with JSON serialization for History & Settings
- **Min SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 37 (Android 15 / Latest)

---

## 📁 Project Structure

```text
com.example.bitcount/
├── analysis/         # Bit analysis engines, formatters, parsers, & validators
├── model/            # Core domain models (BitCountResult, AppSettings, BitWidthMode, etc.)
├── repository/       # Data persistence repositories for History & Settings
├── ui/
│   ├── about/        # About & App info screens
│   ├── components/   # Reusable UI components, cards, & bit views
│   ├── history/      # Calculation history screen & view logic
│   ├── home/         # Main analyzer screen UI state & ViewModel
│   ├── navigation/   # App navigation routes & graph definition
│   ├── settings/     # App preferences & settings screen
│   ├── theme/        # Material 3 colors, typography, & theme logic
│   └── SplashScreen  # Splash screen entry point
└── util/             # Helper utilities (Haptics, etc.)
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Hedgehog or newer (recommended)
- **JDK**: Java 11 or higher
- **Android SDK**: API 37 installed

### Building the Project
1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/BitCount.git
   cd BitCount
   ```
2. **Open in Android Studio**:
   Open Android Studio and select **Open**, then choose the `BitCount` root directory.
3. **Build & Run**:
   Sync Gradle and run the app on an Android Emulator or physical device running API 26+.

### Running Tests
Run unit tests directly using Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
