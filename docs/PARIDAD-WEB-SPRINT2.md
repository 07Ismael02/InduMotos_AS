# Paridad funcional móvil — 23/11/2026

Actualización final: los faltantes de implementación indicados en el cotejo siguiente fueron cubiertos por 824be6c/f24119d (0.2.4). Ver CIERRE-PARIDAD-20261123.md: usuarios, permisos dinámicos y fichas vinculadas presentes; OCR funcionando en Docker y 110 comprobaciones API aprobadas. Queda validación física integral y no se declara aceptación completa. El cotejo inferior se conserva como diagnóstico anterior, no como lista vigente de ausencias.

## Cotejo posterior al APK 0.2.3: paridad todavía incompleta

Referencias main verificadas nuevamente en remoto el 23/11: web f2981a3 y backend 5b37b9b, sin cambios respecto a las copias inspeccionadas. La existencia de endpoints/pantallas no acredita su funcionamiento integral.

| Función de la web | Implementación Android real | Falta o validación |
|---|---|---|
| Login, perfil, menú por rol y logout | Presente; JWT en memoria, perfil /auth/me, manejo 401/403 | Login ADMIN y catálogo observados; expiración, logout y otros roles pendientes |
| Catálogo maestro | Presente; seleccionar existentes o añadir texto, dependencias, alta por API | Lectura/selección verificadas; alta, duplicados y rechazo por rol pendientes |
| Crear cuentas | Formulario presente con rol y contraseña | Probar alta autorizada y errores; no crear usuarios sin acuerdo |
| Listado/búsqueda/paginación de cuentas | Ausente: USERS solo muestra formulario/ayuda | Implementar GET /usuarios y selección por nombre/usuario como web |
| Administración de accesos | Cambio de rol/excepciones presente; consulta manual por ID | Sustituir ID por listado/búsqueda. PermissionLabels solo contiene seis permisos antiguos, omite catálogo y operaciones Sprint 2. Revisar restricciones de administración según web/backend |
| Inventario, filtros, paginación, ficha | Presente, búsqueda explícita con botón | Probar datos/edición por rol; web además busca con demora automática y tiene acceso directo a páginas |
| Ubicación inicial e historial/traslados | Presente, repartido entre ficha y Traslados | Verificar flujo integral; web lo reúne en Ubicaciones |
| Recepción con 1–3 fotos | Presente, selector Android y validación de archivos | Alta real y rechazo de archivos/cantidades pendientes |
| Listas de guías y fotos | Presente con pendientes/confirmadas, búsqueda/páginas, zoom | Prueba por rol pendiente |
| Fichas vinculadas al detalle de llegada | Ausente en nueva pantalla: GuidesState no contiene lista vinculada ni consulta /detalle | Implementar consulta y navegación a fichas, sin alterar datos |
| Borradores y finalización | Presente; bloques de cinco, guardar/reabrir, cierre por API | Prueba completa persistencia/stock/duplicados pendiente |
| OCR de fotos | Cliente presente con revisión humana | Tesseract no disponible en entorno local; lectura real pendiente |
| Edición de ficha | Presente con FICHAS_EDITAR y catálogo | Probar persistencia, duplicados y rechazo por rol |
| Solicitudes y seguimiento | Presente integrado en una pantalla; catálogo, líneas, estados, motivos, historial | Verificar todos los estados/roles; web separa Solicitudes y Seguimiento, separación visual no es ausencia de función |
| Asignar/retirar unidades, anular/rechazar | Presente en solicitudes | Verificar compatibilidad, cupo, exclusividad, liberación y motivo; aceptación HU-27 pendiente |
| Diseño y navegación Android | Catálogo adaptable, Atrás→Inicio/doble salida | Nueve comprobaciones físicas aprobadas; faltan otros módulos, horizontal/texto ampliado/tema claro |

Prioridad: cerrar listado de cuentas y selección de accesos, completar permisos disponibles, agregar fichas vinculadas en llegadas; después ejecutar recorrido por rol y corregir defectos observados. No confundir estos faltantes de implementación con las pruebas aún pendientes de módulos presentes.

Capacidad de verificación autónoma: contratos/código, build, pruebas pertinentes, API sin secretos, BD aislada y operaciones de UI por ADB. Decisiones humanas pendientes: cuentas individuales autorizadas (se rechazaron cuentas demo anteriormente), credenciales elegidas localmente y documentos autorizados de recepción. No hace falta ampliar ADMIN. Tesseract requiere resolver la dependencia del entorno antes de acreditar OCR.

Ver evidencias de navegación y catálogo en evidencias/paridad-20261123 y pruebas HTTP en verificacion-http-20261123.json. No se declara paridad completa ni cierre Sprint 2.

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
