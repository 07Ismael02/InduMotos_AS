# Consulta offline y borradores locales

## Preparar el teléfono

1. Iniciar sesión online con la cuenta propia.
2. Pulsar **Descargar datos** y esperar la confirmación. Se guardan los datos autorizados de inventario/fichas y guías con imágenes. No se copian cuentas del personal ni credenciales.
3. La información está separada por servidor y usuario en el almacenamiento privado de Indumotos AS. No depende de comprar un servidor: el PC/Docker puede estar inaccesible durante la consulta.

## Trabajar sin servidor

El aviso **Sin servidor · consulta local** distingue los datos guardados de la disponibilidad actual. La fecha corresponde a la última lectura online, no a una confirmación de stock vigente. Las búsquedas offline se limitan a lo descargado.

Para una guía previamente descargada, completar o editar un bloque y pulsar **Guardar borrador**. **Guardado en el teléfono · pendiente de envío** significa persistencia local, no confirmación del servidor. No se añade stock y no se finaliza la recepción offline. Los campos aún no guardados no se garantizan después de cerrar forzadamente la app; se solicita confirmación al abandonar una pantalla con cambios no guardados.

Tras cerrar/reabrir, usar **Consultar sin conexión · usuario** y autenticar con el bloqueo del dispositivo. La consulta local dura como máximo 24 horas desde el login online; no restaura un JWT. Si el dispositivo no tiene bloqueo seguro o caduca el perfil, se requiere login online. Se pueden conservar varias cuentas previamente autenticadas y descargadas, seleccionables al reabrir. Cerrar sesión retira la reapertura offline solo de la cuenta activa, pero conserva los borradores de esa cuenta para su próximo login correcto.

## Reconectar

En este APK, conectar al backend local mediante USB y restablecer el túnel indicado abajo. Una conexión directa por Wi-Fi requeriría configurar dirección/HTTPS y no está habilitada en este build. Si se está en consulta local, pulsar **Iniciar sesión online** con la misma cuenta. Después pulsar **Sincronizar**. La app también comprueba conexión periódicamente mientras está visible y tiene sesión online; no se promete trabajo en segundo plano con la app cerrada.

Cada borrador conserva su versión de partida. Antes del envío se renuevan permisos y se compara con el registro del servidor; el PUT lleva If-Match. HTTP 412/409 o cambios distintos quedan en **requiere revisión**. Ver las dos versiones, corregir y confirmar **Revisé mis datos: reenviar**, o descartar explícitamente solo la copia local. No hay reintentos automáticos de conflictos ni de finalizaciones. Una respuesta de red incierta conserva el pendiente; al reintentar se comprueba primero si el servidor ya guardó exactamente esos datos.

## Siempre online

Login del servidor, alta de llegadas con fotos, OCR, finalización/stock, edición de fichas existentes, solicitudes, asignaciones, traslados y administración. Estos cambios no se ponen en una cola offline. Los roles y restricciones efectivas los decide el backend.

## Entorno y recuperación

- Móvil: rama feature/ismael-offline-cuentas. Backend offline: feature/sprint2-offline-borradores, base 95261ad, sin migraciones nuevas.
- API Docker indumotos-paridad-api, imagen indumotos-paridad-ocr:95261ad, puerto 127.0.0.1:8090. Base de pruebas aislada indumotos_s2_paridad_20261123. Contenedor anterior conservado detenido como indumotos-paridad-api-pre-offline.
- App: http://127.0.0.1:8089; `adb -s RFGL11XYCTD reverse tcp:8089 tcp:8090`. Tras reiniciar ADB/USB, restablecer el túnel para sincronizar; su ausencia no impide consultar lo ya descargado.
- El modo offline Android usa SQLDelight, almacenamiento privado, copia de seguridad Android deshabilitada y confirmación nativa del dispositivo. No guarda contraseñas ni JWT. iOS conserva adaptadores de base, pero no se ha compilado ni se ofrece el desbloqueo offline como verificado.

Los resultados exactos y pendientes de dispositivo se registran en OFFLINE-CONTINUIDAD.md y el manifiesto del APK. No confundir pruebas SQLite/HTTP con una captura de Android.
