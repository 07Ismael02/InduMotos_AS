# Paridad funcional móvil — 23/11/2026

Rama móvil: feature/sprint2-paridad-web, desde 42445cb. Referencias descargadas: web main f2981a3; backend main 5b37b9b. Son instantáneas de código; que main exista no certifica cierre del Sprint. No se modifican estas referencias ni PharmaMobile.

| Flujo | Diferencia y trabajo |
|---|---|
| Catálogo / solicitudes | Usar catálogo maestro y opciones autorizadas por API. Administración agrega; Comercial selecciona sin texto libre. |
| Llegadas | Endpoint actual numeroGuia; nueva paginación por PENDIENTE/CONFIRMADA, fotos JPG/PNG (1–3), consulta protegida. |
| Registro de fichas | Selección de guía pendiente, fotos de referencia, bloques de 5, borradores en servidor, OCR solo sugerido/revisado y finalización transaccional. |
| Inventario | Catálogo en filtros y edición con FICHAS_EDITAR; mantener copia de chasis y detalle. |
| Seguimiento / preparación | Mantener transiciones, motivo, asignación exclusiva y retiro. Resolver nombres de destino. |
| Ubicación / traslados | Conservar historial y selección de unidad; permisos efectivos del perfil. |
| Sesión | No copiar sessionStorage de web a Android. Mantener JWT en memoria, borrado al salir y 401/403. |

Arquitectura: pantallas → ViewModel → contratos de dominio → repositorio REST. DTO tipados para los flujos nuevos; Koin proporciona contratos. Capacidades nativas se adaptan desde Android. Sin migraciones propias ni ampliación silenciosa de permisos. Web y Figma permanecen fuera de nuestra implementación.

Backend actual usa Flyway V1–V23; la base de pruebas anterior tenía V15. No se iniciará el backend nuevo sobre esa base ni se ejecutarán migraciones sin preparar una copia aislada y verificar respaldo. La aplicación necesita la API actual; no fingir compatibilidad del catálogo con el servidor anterior.

Verificación: compilar y probar cada bloque relevante; agrupar recorridos finales en Samsung. Las capturas del 19/11 pertenecen al APK anterior y no demuestran los cambios de esta rama. Mantener applicationId y firma. No merge a main antes de validación final.
