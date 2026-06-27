# 🍩 Homero Chino Clicker

¡Bienvenido a **Homero Chino Clicker**! Un divertido y adictivo juego tipo *clicker* para Android inspirado en Homero Simpson, donde el objetivo es alcanzar los 1,000 clics en el menor tiempo posible y registrar tu récord en una tabla de clasificación global.

---

## 🚀 Características

* **Interfaz Premium**: Diseñada completamente en **Jetpack Compose** con fondos con gradientes de neón profundo, tarjetas con animaciones de pulso y bordes brillantes.
* **Efectos de Sonido Divertidos**: Reproduce los clásicos e icónicos sonidos de Homero al hacer clics o interactuar con el juego (ubicados en los recursos `raw`).
* **Autenticación e Integración con Firebase**:
  * Autenticación anónima integrada (y soporte preparado para inicio de sesión real con Google).
  * Persistencia en la nube de tu mejor marca.
* **Puntuaciones Globales**: Tabla de clasificación interactiva (Top 50) que muestra los tiempos más rápidos directamente desde **Firebase Realtime Database**.
* **Personalización de Perfil**: 
  * Selección rápida entre múltiples avatares temáticos (emojis representativos).
  * Posibilidad de subir tu propia foto personalizada desde tu galería, la cual se redimensiona automáticamente y se almacena en Base64 para ahorrar espacio.

---

## 🛠️ Stack Tecnológico

* **Lenguaje**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) (con Material 3)
* **Arquitectura**: MVVM (Model-View-ViewModel)
* **Base de Datos y Auth**: [Firebase](https://firebase.google.com/) (Authentication & Realtime Database)
* **Gestor de Dependencias**: Gradle (Kotlin DSL con Version Catalogs)

---

## ⚙️ Configuración y Requisitos

Para correr el proyecto en tu propio entorno y conectar tu base de datos:

1. **Prerrequisitos**:
   * Android Studio Jellyfish (o superior).
   * JDK 17 configurado en el proyecto.
2. **Configuración de Firebase**:
   * Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
   * Descarga el archivo `google-services.json` y colócalo en la carpeta `app/`.
   * Habilita **Anonymous Sign-in** en la pestaña de *Authentication -> Sign-in method*.
   * Crea una base de datos **Realtime Database** y configura las reglas de lectura/escritura (para desarrollo puedes usar `auth != null`).

---

## 📄 Licencia

Este proyecto está disponible bajo fines recreativos y educativos. Los recursos y marcas de Los Simpson pertenecen a sus respectivos creadores.
