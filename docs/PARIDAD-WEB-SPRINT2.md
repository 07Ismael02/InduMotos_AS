# Paridad funcional móvil — 23/11/2026

Rama móvil: feature/sprint2-paridad-web, desde 42445cb. Referencias descargadas: web main f2981a3; backend main 5b37b9b. Son instantáneas de código; que main exista no certifica cierre del Sprint. No se modifican estas referencias ni PharmaMobile.

| Flujo | Diferencia y trabajo |
|---|---|
| Catálogo / solicitudes | Usar catálogo maestro y opciones autorizadas por API. Administración agrega; Comercial selecciona sin texto libre. |
| Llegadas | Endpoint actual numeroGuia; nueva paginación por PENDIENTE/CONFIRMADA, fotos JPG/PNG (1–3), consulta protegida. |
| Registro de fichas | Selección de guía pendiente, fotos de referencia, bloques de 5, borradores en servidor, OCR solo sugerido/revisado y finalización transaccional. |
| Inventario | Mantener filtros combinados de la web y edición con FICHAS_EDITAR; conservar copia de chasis y detalle. |
| Seguimiento / preparación | Mantener transiciones, motivo, asignación exclusiva y retiro. Resolver nombres de destino. |
| Ubicación / traslados | Conservar historial y selección de unidad; permisos efectivos del perfil. |
| Sesión | No copiar sessionStorage de web a Android. Mantener JWT en memoria, borrado al salir y 401/403. |

Arquitectura: pantallas → ViewModel → contratos de dominio → repositorio REST. DTO tipados para los flujos nuevos; Koin proporciona contratos. Capacidades nativas se adaptan desde Android. Sin migraciones propias ni ampliación silenciosa de permisos. Web y Figma permanecen fuera de nuestra implementación.

Backend actual usa Flyway V1–V23; la base de pruebas anterior tenía V15. No se iniciará el backend nuevo sobre esa base ni se ejecutarán migraciones sin preparar una copia aislada y verificar respaldo. La aplicación necesita la API actual; no fingir compatibilidad del catálogo con el servidor anterior.

Verificación: compilar y probar cada bloque relevante; agrupar recorridos finales en Samsung. Las capturas del 19/11 pertenecen al APK anterior y no demuestran los cambios de esta rama. Mantener applicationId y firma. No merge a main antes de validación final.

## Implementación entregada en la rama

Catálogo / opciones de solicitud; guías pendientes y confirmadas con paginación; selección Android de fotos limitada y validada; fotos protegidas y zoom; borradores tipados por bloques de cinco; OCR por API con revisión obligatoria y plazo de lectura específico; reanudación y finalización; edición de ficha; nombres de destino en solicitudes; aislamiento al abandonar catálogo/guías; consulta antigua de llegadas eliminada del contenedor principal. Los nuevos flujos usan contratos/repository/ViewModel/pantalla separados.

No se copió sessionStorage, no se agregó offline, no se crearon cuentas ni se cambiaron reglas del backend. El selector nativo de fotos está implementado para Android; iOS no se ha compilado ni se declara equivalente.

Las fuentes de web/main y backend/main se inspeccionaron, pero no se ejecutaron sus suites en este bloque: no afirmar 81 pruebas del backend como nuevas. El detalle del build móvil se registra en build-paridad-20261123.json. La paridad end-to-end y la experiencia visual siguen pendientes de la matriz manual; tampoco hay resultado OCR acreditado con una guía real.

Resultado local final: BUILD SUCCESSFUL, 23 pruebas Android Host aprobadas (incluyen reglas de lote, OCR revisado, doble envío, respuesta incierta y navegación sin endpoint antiguo). apksigner aprobado; misma clave Debug. Sin pruebas físicas ni API V23 ejecutadas en este bloque.
