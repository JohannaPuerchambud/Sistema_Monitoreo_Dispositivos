# Sistema de Monitoreo de Dispositivos

Aplicación móvil de seguimiento en tiempo real

Este proyecto es una aplicación para Android diseñada para recolectar datos de ubicación GPS y del estado del dispositivo, y exponerlos mediante una API REST local protegida por autenticación.

---

## 🚀 Cómo Configurar y Ejecutar el Proyecto

Sigue estos pasos para configurar el entorno y probar la aplicación.

---

### 📋 Prerrequisitos

- **Android Studio** (última versión estable)
- **SDK de Android** compatible con el `targetSdk` del proyecto (actualmente `targetSdk 35`)
- **Un dispositivo Android físico o emulador**

---

### ⚙️ Configuración del Proyecto

#### Clonar el Repositorio

git clone https://github.com/JohannaPuerchambud/Sistema_Monitoreo_Dispositivos.git
cd Sistema_Monitoreo_Dispositivos
### 📂 Abrir en Android Studio

1. Abre Android Studio.
2. Selecciona `File > Open...` y busca la carpeta del proyecto que acabas de clonar.
3. Haz clic en `Open`.
4. Espera a que Gradle sincronice el proyecto.  
   Si no lo hace automáticamente, ve a:  
   `File > Sync Project with Gradle Files`.

---

## ▶️ Ejecución del Proyecto

### 🔌 Conectar dispositivo o emulador

- Conecta tu teléfono Android por USB y asegúrate de que la **depuración USB** esté habilitada.
- O inicia un emulador desde:  
  `Tools > Device Manager`.

---

### 🔐 Otorgar permisos esenciales

**⚠️ MUY IMPORTANTE:** Para que la aplicación funcione correctamente en segundo plano:

1. Ve a `Ajustes > Aplicaciones`.
2. Busca la app **"Sistema de Monitoreo de Dispositivos"**.
3. Ve a la sección de **Permisos**.
4. Selecciona **Ubicación**.
5. Establece la opción:  
   **"Permitir todo el tiempo"** (Allow all the time).

> Sin este permiso, el GPS no funcionará correctamente cuando la app esté en segundo plano.

---

### ▶️ Ejecutar la aplicación

1. En Android Studio, selecciona tu dispositivo o emulador desde el menú superior.
2. Haz clic en el botón de **Run** (ícono de triángulo verde).
3. La app se instalará e iniciará automáticamente en tu dispositivo.

---

### 📡 Iniciar recolección y servidor API

1. En la interfaz principal de la app, pulsa **"Iniciar Recolección GPS"**.
2. Si es la primera vez que inicias los servicios, se te pedirá el permiso de ubicación en tiempo real. Acepta.
3. El estado cambiará a **"Activo"** y aparecerá una notificación persistente.
4. Desde este punto, el GPS se recolectará cada 30 segundos y el servidor HTTP local quedará disponible.

---

## 🌐 Pruebas Remotas desde Otra Máquina

### 🌍 IP del dispositivo

- La aplicación mostrará automáticamente la **IP local** del dispositivo (por ejemplo: `192.168.1.45`).
- Usa esta dirección como host para acceder a la API REST desde otra máquina en la misma red Wi-Fi.

---

### 🔐 Token de autenticación

El token predefinido es:

jepuerchambudp


Inclúyelo en el header HTTP como:

Authorization: Bearer jepuerchambud

### 🔎 Ejemplos de Peticiones

#### 📲 Estado del dispositivo

curl -H "Authorization: Bearer jepuerchambud" http://192.168.1.45:8080/api/device_status

📍 Datos GPS recolectados
curl -H "Authorization: Bearer jepuerchambud" "http://192.168.1.45:8080/api/sensor_data?start_time=2025-07-22T00:00:00Z&end_time=2025-07-22T23:59:59Z"




  
