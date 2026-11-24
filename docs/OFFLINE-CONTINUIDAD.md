# Offline — bloque activo, 23/11/2026

Base be14bcf, rama feature/sprint2-paridad-web. No tocar PharmaMobile ni migraciones del servidor. Objetivo acordado: consulta local y conservación de borradores de motocicletas para guías previamente descargadas. No publicar nuevas llegadas/fotos, finalizar, asignar unidades, trasladar o administrar accesos sin confirmación online.

Diseño: SQLDelight privado por servidor/usuario; catálogo, páginas de inventario, fichas y guías visitadas con fecha de descarga; borradores locales independientes del stock. Cada pendiente conserva baseline y revisión. Antes del PUT idempotente, consultar registro servidor; si cambió respecto al baseline, exigir revisión. No reintentar POST sensibles. No hacer fallback ante 401/403. No cachear usuarios/permisos administrativos ni JWT/contraseñas.

Reapertura offline: perfil de consulta limitado temporalmente, autenticación por bloqueo del dispositivo Android, sin restablecer token ni habilitar escrituras remotas. Logout impide reabrir el perfil offline; conserva borradores particionados para recuperar solo tras login de la misma cuenta. Sincronización explícita y al recuperar conexión mientras app esté activa; no afirmar ejecución en segundo plano sin implementarla.

Hitos pendientes: persistencia y pruebas SQLite; repositorios offline; UI/seguridad nativa; pruebas de conflictos/aislamiento/reinicio; APK y validación física. Registrar commits y resultados conforme ocurran. No marcar este diseño como implementado/verificado.

Hito 1: esquema SQLDelight generado con 2.1.0 disponible localmente. SqlOfflineStore y OfflineSession implementados, drivers Android/iOS y contexto Koin conectados. Seis pruebas OfflinePersistenceTest aprobadas en SQLite JVM real (incluyen archivo cerrado/reabierto, aislamiento, revisión concurrente, logout y caducidad de perfil). Log local tmp/sprint2-auditoria/offline-store-test.log. No se ha verificado iOS ni dispositivo. Repositorios/UI de offline todavía en integración: no considerar este checkpoint un APK offline terminado.

Hito 2: repositorios con caché de lecturas operativas, fotos privadas y borradores persistentes; sincronización con baseline, revisión y PUT If-Match. Dieciséis pruebas offline aprobadas (6 persistencia + 10 sincronización/lecturas): 401/403 sin fallback, no duplicar envío confirmado, conflictos y permisos. Backend en worktree Backend-Offline-Integracion, rama feature/sprint2-offline-borradores, commit 95261ad; agrega versión/If-Match bajo bloqueo sin migraciones y conserva compatibilidad web. Dos pruebas backend aprobadas; despliegue y HTTP CAS aún pendientes. UI/reapertura protegida aún en integración.

Referencias oficiales: https://developer.android.com/topic/architecture/data-layer/offline-first y https://sqldelight.github.io/sqldelight/ . PharmaMobile solo se inspecciona como referencia; no se modifica.
