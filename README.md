# EventPass App — Control de Asistencia a Eventos con QR

Aplicación móvil desarrollada para Android con **Kotlin** y **Jetpack Compose**. El proyecto permite gestionar el acceso y control de asistencia a eventos mediante el escaneo de códigos QR en tiempo real.

---

## 🎨 Diseños de Interfaz

El diseño UI/UX se conceptualizó previamente en Figma y se implementó de manera declarativa con Jetpack Compose:

| 1. Splash Screen | 2. Home Screen | 3. Escáner QR |
| :---: | :---: | :---: |
| ![Splash](screenshots/splash.png) | ![Home](screenshots/home.png) | ![Scanner](screenshots/scanner.png) |

---

## 📱 Vistas Implementadas

1. **Splash Screen:** Pantalla de inicio con identidad visual, temporizador y transición automática al panel principal.
2. **Home Screen (Dashboard):** Panel central con monitoreo de aforo en tiempo real, conteo de accesos y botón de navegación al lector.
3. **Escáner QR:** Interfaz con integración en vivo a CameraX, gestión de permisos en tiempo real y superposición visual para lectura de boletos.

---

## 🛠️ Tecnologías y Herramientas

* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Navegación:** Jetpack Navigation Compose
* **Cámara & Visión:** CameraX & Google ML Kit Barcode Scanning
* **Diseño & IDE:** Figma, Android Studio
* **Control de Versiones:** Git / GitHub

---

## 🔗 Enlace al Repositorio
https://github.com/Arias313/EventPass-app-
