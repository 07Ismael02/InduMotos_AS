# Offline — resultado vigente, 23/11/2026

Versión 0.3.2-sprint2-offline/código9, APK Debug instalado por actualización compatible en Samsung SM-S948U1, Android17/API37. Código móvil 92f3bce; backend 95261ad. No merge a main ni etiqueta final.

## Comprobaciones

- 51 pruebas móviles JVM/SQLite/HTTP aprobadas: persistencia, aislamiento de cuentas, caducidad/logout, imágenes de 5MB leídas por bloques, red/401/403 y concurrencia. No son 51 recorridos físicos.
- Backend: 2 pruebas unitarias y 14 comprobaciones HTTP reales aprobadas; If-Match obsoleto devuelve412 sin sobrescribir; la web sin cabecera conserva contrato; guardar borrador no genera stock.
- Samsung: consulta de guía/foto descargadas sin túnel; guardado local de un borrador; actualización -r a0.3.2 y reapertura protegida por credencial Android. El usuario completó el desbloqueo; se observó pendiente1 y motor MOTOROFFLINEQA2026 conservado.
- Samsung final0.3.2: login online, sincronización y contador local0. Lectura API confirma guía3 con un solo borrador, motor MOTOROFFLINEQA2026 y cantidadFichas0. Captura final revisada y resultado en evidencias/offline-20261123.
- Las capturas previas de reapertura contienen una ventana personal superpuesta: se conservaron privadas y no se publican. Esa parte tiene observación registrada, no captura pública limpia.

## Entorno y límites

PostgreSQL se encontró detenido durante la validación; se inició el contenedor existente y se reinició solo la API. HTTP200 recuperado. Contenedor de respaldo pre-offline detenido con restart=no para evitar su reinicio automático. Ningún volumen/base recreado, ninguna migración nueva, ningún usuario histórico restablecido.

Consulta local y borradores de guías descargadas funcionan sin servidor; no se confirman stock, altas con fotos, OCR, asignaciones, traslados o administración offline. Reconexion actual: USB8089→PC8090. No hay despliegue HTTPS público ni sincronización con app cerrada. iOS no compilado. Pruebas físicas integrales por rol y firma release siguen pendientes; esto no cierra todo Sprint2.

Evidencias: resultado-cas-http.json (14), resultado-dispositivo-offline.json (lectura servidor) y offline-032-sincronizacion.png (pantalla final). Datos sintéticos aislados; los identificadores técnicos preexistentes se conservan para trazabilidad. Para nuevas pruebas usar nombres sencillos como Prueba1.

Uso y arquitectura: OFFLINE-USO.md. Hash/firma/ruta: build-offline-20261123.json. Compilar con un worker, Gradle1536MB y Kotlin in-process. No recompilar solo para repetir verificaciones ya aprobadas.
