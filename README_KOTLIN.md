# BodegaOS - 100% Kotlin Version

This project has been migrated to a full Kotlin stack.

## 🚀 Backend (Kotlin + Ktor)
Located in `/backend-kt`
- **Framework:** Ktor
- **Database:** PostgreSQL (via Exposed ORM)
- **Serialization:** Kotlinx Serialization
- **Architecture:** Route-based with DTOs and Exposed Tables.

### How to run:
1. Navigate to `backend-kt`.
2. Ensure PostgreSQL is running and update `Application.kt` database config if needed.
3. Run `./gradlew run`.

## 📱 Frontend (Android + Jetpack Compose)
Located in `/frontend-kt`
- **Framework:** Jetpack Compose (Modern Declarative UI)
- **Networking:** Ktor Client (or Retrofit recommended for future scale)
- **Design:** Matching the original BodegaOS aesthetic with high performance.

### Highlights:
- **Login Screen:** Fully implemented with Material 3 and custom styling.
- **Dashboard Screen:** Stats, Hero cards, and Quick Actions implemented.
- **Scanning:** (Structure ready) Integration with CameraX + ML Kit.

## 🛠 Project Structure
- `backend-kt/src/main/kotlin/com/bodegaos/`:
    - `models/`: Table definitions and DTOs.
    - `routes/`: API endpoint logic.
    - `plugins/`: Ktor configuration (CORS, Serialization).
- `frontend-kt/app/src/main/java/com/bodegaos/ui/`:
    - `theme/`: Colors and styles.
    - `screens/`: Compose UI components.
