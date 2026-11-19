# Evidencias del bloque HU-26 / identidad — 19/11/2026

Capturas sin retoques del AVD Medium_Phone_API_37.0, Android API 37, 1080×2400. Fecha de trabajo: America/Lima; la hora visible del emulador usa su propia zona. No son capturas del Samsung ni de una sesión autenticada.

- 01: login claro; campos vacíos, acceso deshabilitado, sin selector técnico.
- 02: cambio de sistema a oscuro; marca, texto y barras legibles.
- 03: icono IM en launcher; no se instaló sobre el Samsung.
- 04: contraseña enfocada y teclado abierto; controles de acceso visibles. No se escribió ni capturó una contraseña.
- XML: resultados originales de las 8 pruebas Android Host del build exitoso.

Comandos realmente ejecutados: wrapper `:androidApp:assembleDebug :shared:testAndroidHostTest`; apksigner `verify --print-certs`; aapt `dump badging`; ADB install/arranque, `cmd uimode night no/yes`, `screencap` y lectura del buffer crash (vacío). Comprobación negativa de configuración: Gradle help offline con HTTP remoto rechazado por la validación esperada.

Trazabilidad de APK y código: [build-login-20261119.json](../../build-login-20261119.json). El código de la app está en `2d12129`; las evidencias se capturaron con ese contenido antes de crear el commit.

Pendientes: login/logout con admin.indumotos contra backend autorizado, pantallas internas y sus temas con sesión, regresión de altas/permisos del Sprint 1, integración Sprint 2, firma anterior para actualizar el Samsung. Las pruebas de mapeo de errores y validación no sustituyen estos recorridos.
