# Hyundai Smart Home (Android TV / Android 7.1.1)

Proyecto independiente para probar una pantalla de inicio moderna en el televisor Hyundai/Realtek. No modifica ni elimina `com.toptech.launcher`.

## Incluye
- Interfaz horizontal para control remoto, fondo vino/negro, reloj/fecha, botones Entradas/Red/Ajustes y tarjetas de aplicaciones.
- Lectura de aplicaciones instaladas que tienen actividad de inicio.
- Añadir aplicaciones desde la lista instalada.
- Mantener OK sobre una tarjeta para moverla al inicio o quitarla de favoritos.
- Favoritos guardados con SharedPreferences.
- Accesos de TV/HDMI/AV/reproductor/USB como lanzadores de mejor esfuerzo. El cambio real de entrada depende del firmware Realtek y no está garantizado todavía.

## Cómo compilar sin instalar Android Studio

Este proyecto incluye un flujo de GitHub Actions para compilar el APK en la nube.

1. Crea una cuenta o inicia sesión en https://github.com/.
2. Crea un repositorio nuevo, por ejemplo `HyundaiSmartLauncher`. Si aparece la opción, déjalo **Private**.
3. En el repositorio, selecciona **Add file → Upload files**.
4. Extrae este ZIP en tu PC y sube **todo el contenido de la carpeta `HyundaiSmartLauncher`**, incluida la carpeta oculta `.github` y los archivos `gradlew`, `settings.gradle`, `build.gradle` y `app`.
5. Pulsa **Commit changes**.
6. Abre la pestaña **Actions** y selecciona **Build Hyundai Smart Launcher APK**. Si no aparece un proceso en curso, pulsa **Run workflow**.
7. Cuando termine con una marca verde, abre la ejecución y descarga el artefacto `HyundaiSmartLauncher-debug-apk`.
8. Dentro del ZIP descargado estará `app-debug.apk`.

## Compatibilidad
- `minSdk 25` (Android 7.1.1).
- Paquete independiente `com.hyundai.smartlauncher`.
- El launcher original queda intacto.

## Antes de instalar
Primero hay que confirmar que la compilación terminó correctamente. Después se podrá instalar el APK por ADB y probar el mando, la pantalla de inicio y las entradas. No deshabilites el launcher original hasta verificar el funcionamiento.
