# Offline — bloque activo, 23/11/2026

Base be14bcf, rama feature/sprint2-paridad-web. No tocar PharmaMobile ni migraciones del servidor. Objetivo acordado: consulta local y conservación de borradores de motocicletas para guías previamente descargadas. No publicar nuevas llegadas/fotos, finalizar, asignar unidades, trasladar o administrar accesos sin confirmación online.

Diseño: SQLDelight privado por servidor/usuario; catálogo, páginas de inventario, fichas y guías visitadas con fecha de descarga; borradores locales independientes del stock. Cada pendiente conserva baseline y revisión. Antes del PUT idempotente, consultar registro servidor; si cambió respecto al baseline, exigir revisión. No reintentar POST sensibles. No hacer fallback ante 401/403. No cachear usuarios/permisos administrativos ni JWT/contraseñas.

Reapertura offline: perfil de consulta limitado temporalmente, autenticación por bloqueo del dispositivo Android, sin restablecer token ni habilitar escrituras remotas. Logout impide reabrir el perfil offline; conserva borradores particionados para recuperar solo tras login de la misma cuenta. Sincronización explícita y al recuperar conexión mientras app esté activa; no afirmar ejecución en segundo plano sin implementarla.

Hitos pendientes: persistencia y pruebas SQLite; repositorios offline; UI/seguridad nativa; pruebas de conflictos/aislamiento/reinicio; APK y validación física. Registrar commits y resultados conforme ocurran. No marcar este diseño como implementado/verificado.

Referencias oficiales: https://developer.android.com/topic/architecture/data-layer/offline-first y https://sqldelight.github.io/sqldelight/ . PharmaMobile solo se inspecciona como referencia; no se modifica.
