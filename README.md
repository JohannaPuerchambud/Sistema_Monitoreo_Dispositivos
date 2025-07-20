# Device_Monitor
Aplicativo de Sistema de seguimiento en tiempo real

Este proyecto es una aplicación móvil para Android diseñada para recolectar datos de ubicación GPS y el estado del dispositivo, y exponerlos a través de una API REST local con autenticación.

🚀 Cómo Configurar y Ejecutar el Proyecto
Sigue estos pasos para poner en marcha la aplicación en tu entorno de desarrollo y probarla.

📋 Prerrequisitos
Antes de comenzar, asegúrate de tener instalados los siguientes programas:

Android Studio: La última versión estable.

SDK de Android: Versión compatible con el targetSdk del proyecto (actualmente targetSdk 35).

Un dispositivo Android físico o emulador: Para ejecutar la aplicación.

⚙️ Configuración del Proyecto
Clonar el Repositorio:
Abre tu terminal o Git Bash y clona el repositorio del proyecto:

  git clone https://github.com/JAPASPUELS/Device_Monitor.git
  cd SistemaMonitoreoDispositivos

Abrir en Android Studio:

Abre Android Studio.

Selecciona File > Open... y navega hasta la carpeta SistemaMonitoreoDispositivos que acabas de clonar.

Haz clic en Open.

Espera a que Android Studio sincronice el proyecto y descargue todas las dependencias de Gradle. Asegúrate de tener una conexión a internet activa.

Sincronizar Gradle:
Si Android Studio no sincroniza automáticamente, puedes hacerlo manualmente desde: File > Sync Project with Gradle Files.

▶️ Ejecución del Proyecto
Conectar Dispositivo o Iniciar Emulador:

Conecta tu dispositivo Android físico vía USB y asegúrate de que la depuración USB esté habilitada.

Alternativamente, inicia un emulador de Android a través de Tools > Device Manager.

Otorgar Permisos Esenciales (Configuración del Dispositivo):
¡MUY IMPORTANTE! Para que la aplicación pueda recolectar datos GPS en segundo plano y funcionar correctamente, debes otorgar el permiso de ubicación de forma manual después de la primera instalación (o si el usuario lo deniega inicialmente).

En tu dispositivo Android (físico o emulador), ve a Ajustes (Settings).

Navega a Aplicaciones (Apps) o Aplicaciones y notificaciones (Apps & notifications).

Busca y selecciona la aplicación "Sistema de Monitoreo de Dispositivos".

Ve a Permisos (Permissions).

Localiza el permiso de Ubicación (Location).

Selecciona la opción "Permitir todo el tiempo (Allow all the time)". Esta opción es crucial para que la recolección de datos GPS funcione incluso cuando la aplicación no está en uso activo en primer plano.

Ejecutar la Aplicación desde Android Studio:

En Android Studio, selecciona tu dispositivo o emulador conectado en el menú desplegable junto al botón "Run".

Haz clic en el botón "Run 'app'" (el icono de triángulo verde).

Iniciar Recolección y Servidor API:

Una vez que la aplicación se inicie en tu dispositivo, verás la interfaz principal.

Pulsa el botón "Iniciar Recolección GPS".

Si es la primera vez que inicias los servicios o si los permisos no se han concedido aún, la aplicación te solicitará el permiso de ubicación en tiempo de ejecución. Asegúrate de concederlo.

Verifica que el estado en la UI cambie a "Activo" y que observes las notificaciones persistentes en la barra de estado, indicando que los servicios están en funcionamiento.

🌐 Prueba de la Aplicación y Peticiones Remotas
Una vez que la aplicación esté ejecutándose y los servicios iniciados:

Obtén la IP Local: La aplicación mostrará tu dirección IP local en la interfaz de usuario (ej. 192.168.1.XX). Anota esta IP.

Token de Autenticación: El token predefinido para las pruebas es: JAPASPUELS.

Usa Postman o cURL: Desde otra máquina en la misma red Wi-Fi, puedes enviar peticiones HTTP a tu dispositivo.

Ejemplo de Petición (Obtener estado del dispositivo - Autenticado):

  curl -H "Authorization: Bearer JAPASPUELS" http://<TU_IP_LOCAL>:8080/api/device_status

Ejemplo de Petición (Obtener datos del sensor - Autenticado):

  curl -H "Authorization: Bearer your_default_secure_token_123" http://<TU_IP_LOCAL>:8080/api/sensor_data

¡Listo! Con estos pasos, deberías poder configurar, ejecutar y empezar a interactuar con tu aplicación de monitoreo remoto de dispositivos.
