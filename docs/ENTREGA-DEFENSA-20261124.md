# Entrega de defensa — 24/11/2026

## Entregables

- APK de demostración Android0.4.4/código17, applicationId pe.indumotos.asapp: [descargar](../entregables/android/Indumotos-AS-0.4.4-evolucion-debug.apk).
- [Manifiesto, SHA256 y certificado](build-044.json). Firma debug original; no es una publicación de producción.
- Código del APK:277bd46. Documentación posterior no modifica ese binario.
- [Guías offline](GUIAS-OFFLINE-044.md), [cámara y perfil](CAMARA-Y-PERFIL.md), [navegación y acceso local](PERFIL-Y-NAVEGACION.md).
- Backend compatible publicado en [feature/ismael-catalogo-ventas](https://github.com/Akatosh4019/BACK-END-INDUMOTOS/tree/feature/ismael-catalogo-ventas), código67e54f2.

## Estado comprobado

Build Android y64 pruebas móviles aprobados. Suite backend existente:123 pruebas sin fallos/errores; no repetida durante este cierre. APK instalado conservando datos persistentes y certificado. Docker recuperado, PostgreSQLhealthy y salud HTTPS200. Perfil agrupado y selector oscuro revisados en Samsung; captura real confirmada en0.4.2. No se declara probado el registro en servidor de esa fotografía.

0.4.4 permite editar borradores desde Llegadas cuando la cuenta tiene FICHAS_REGISTRAR e INVENTARIO_CONSULTAR. Se comprobó mediante API que admin.indumotos tenía deshabilitado el primer permiso; se guardó respaldo privado y se habilitó solo esa capacidad mediante la API administrativa. Se renovó su perfil y se descargaron datos. La revisión física final de edición offline se interrumpió al cambiar la pantalla del teléfono; no se presenta como aprobada ni se publica contenido personal de esa pantalla.

## Límites que deben declararse

Offline: consulta descargada y borradores de fichas de guías existentes. Las nuevas recepciones con fotos, finalización de stock, traslados, pagos y administración de cuentas necesitan conexión; no existe una cola general para todas las operaciones. La sincronización revalida cuenta/permisos y revisa conflictos de versión. Los borradores usan guía/posición, no IDs negativos de motocicletas definitivas.

Pagos Yape/Plin/transferencia: registro académico con referencia/comprobante y confirmación manual autorizada. No integración bancaria ni confirmación automática. Recorrido móvil completo de pagos y última prueba de reconexión pendientes. iOS no compilado ni validado. Fotografías de prueba privadas y secretos no forman parte de la entrega pública.

## Backend: integración a main bloqueada

El main observado aee0e86 contiene avances nuevos del equipo. Sus migraciones y las de nuestra rama reutilizan versiones para contenidos distintos:

| Versión | Main del equipo | Rama de integración |
|---|---|---|
| V32 | Datos del trabajador | Catálogo activo, ediciones y reportes |
| V33 | Permisos completos del administrador | Fichas ampliadas y fotos |
| V34 | Bitácora de datos maestros | Pagos manuales con comprobante |

Un merge directo deja versiones duplicadas de Flyway. Se conserva main y se publica nuestra rama; antes de integrar se necesita conciliación probada en copia aislada con respaldo. No renumerar/reparar migraciones aplicadas ni alterar una base del equipo para ocultar el conflicto. Revisar también coherencia de disponibilidad/ventas para motocicletas asignadas.

## Demostración en esta PC

Activar hotspot Windows192.168.137.1. Iniciar contenedores existentes en orden: indumotos-distribuidas-db-1, luego indumotos-evolucion-api. API HTTPS192.168.137.1:8443, loopback8091; PostgreSQL solo loopback55432. ADB sirve para instalar/inspeccionar y puede cambiar de puerto; no es la conexión de negocio. Sin exponer API o base en campus, recrear volúmenes ni publicar configuración privada.
