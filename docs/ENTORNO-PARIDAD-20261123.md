# Entorno físico — 23/11/2026

## Comprobado

- Docker disponible. Contenedor PostgreSQL existente `indumotos-distribuidas-db-1` iniciado; Oracle de PharmaMobile no se inició.
- Base origen `indumotos_s2_verificacion` permanece en V15.
- Respaldo custom PostgreSQL: 55 121 bytes; SHA-256 `40655fa176243e697ff42b5aba63d9f97f0ca326fd0718927529ea89b7b91047`. Listado con pg_restore validado y restauración sin errores. Conteos de todas las tablas públicas coincidentes antes de migrar.
- Copia aislada `indumotos_s2_paridad_20261123`: migraciones V16–V23 aplicadas correctamente. No se reescribieron migraciones. V23 restringe catálogo al rol ADMIN según contrato del backend.
- JAR del backend main `5b37b9b812c3c08e7edd4aeac67c3216e62850c7` generado con Maven local, sin repetir pruebas. Iniciado sin perfil demo, usando credenciales privadas existentes y cambiando solo la conexión a la copia y dirección/puerto local.
- Backend HTTP 200: `http://127.0.0.1:8090/api/health`. PostgreSQL 55432 y API 8090 escuchan en 127.0.0.1. Sin cambios de firewall ni exposición pública.
- Cuenta conservada: admin.indumotos / ADMIN / ACTIVO. No se crearon usuarios ni se restablecieron contraseñas.
- Samsung RFGL11XYCTD, SM-S948U1, Android 17/API 37 autorizado por el usuario.
- Firma instalada 0.2.0 y actualización 0.2.1: mismo certificado SHA-256 `23c1a0373560f28c3d2b64103e50a0dff54bb955fd75b20ac40d52253e33b0f0`.
- `adb install -r`: Success. Versión instalada 0.2.1-sprint2/código 3, paquete pe.indumotos.asapp. No se desinstaló ni se limpiaron datos; conservación funcional de datos locales pendiente de observar.
- Túnel confirmado: `UsbFfs tcp:8089 tcp:8090`. Actividad real abierta: pe.indumotos.asapp/pe.indumotos.mobile.MainActivity.

## Operación y privacidad

La API corre como proceso Java local: Docker muestra PostgreSQL, no un contenedor API. Respaldo, manifiesto de conteos y log privado están fuera del repositorio en `C:/Users/Ismael Logacho/Documents/PharmaMobil/tmp/sprint2-auditoria/privado-paridad/`, con herencia de permisos retirada y acceso al usuario local. No publicar: el respaldo contiene hashes de contraseñas.

El lanzador local `tmp/sprint2-auditoria/iniciar-paridad-local.py` usa configuración privada existente, apunta a la copia V23 y rechaza iniciar si 8090 está ocupado. El script de preparación rechaza sobrescribir copia/respaldo: no repetirlo.

Después de reconectar USB o reiniciar ADB: `adb -s RFGL11XYCTD reverse tcp:8089 tcp:8090`. La URL incorporada en la app es http://127.0.0.1:8089.

## Pendiente

- Login y consultas desde Samsung: esperando intervención manual. Salud del PC y túnel creado no prueban por sí solos la conexión desde la app.
- Solo existe ADMIN. Recepción, fichas y operación por otros roles requieren cuentas individuales autorizadas por el usuario; no ampliar ADMIN ni inventar cuentas.
- Tesseract no disponible en PATH; OCR no validado. No se instalaron herramientas. El registro manual debe probarse.
- Los ocho recorridos de PRUEBAS-PARIDAD-SPRINT2.md siguen pendientes de ejecución real. La actualización instalada no acredita su aprobación.
- Sin cambios en Excel, main, código backend o datos de PharmaMobile.
