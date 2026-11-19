# Sprint 2 — corte real del 19/11/2026

## Entorno y alcance

Aplicativo: `feature/sprint2-recorrido`, basado en `df9b61c`. Android Studio abre `C:/Users/Ismael Logacho/Documents/PharmaMobil/proyecto-final-dam/InduMotos-Sprint2`.
Backend correcto: Akatosh4019/BACK-END-INDUMOTOS, rama `sprint-2`, commit `fbfaab5e6c50b36efb4809c9d212273cef7718ed`. Worktree de ejecución: `C:/Users/Ismael Logacho/Documents/INDUMOTOS/Backend-Sprint2-Validacion`. Código del backend sin cambios.

Se respaldó la base Sprint 1 fuera del repositorio y se creó la copia aislada `indumotos_s2_verificacion`. Las pruebas escriben únicamente datos académicos identificados en esa copia. No se crearon usuarios ni se restablecieron contraseñas; se conserva `admin.indumotos`. No se modificaron bases originales, volúmenes ni migraciones.

API: `127.0.0.1:8090`; APK: `http://127.0.0.1:8089`. Samsung por USB: `adb reverse tcp:8089 tcp:8090`. PostgreSQL permanece en loopback. Salud real: GET `/api/health`, HTTP 200. `/actuator/health` devuelve 401; no es la ruta pública de salud del proyecto.

## Implementación nueva

Solicitudes multilínea, listado paginado y detalle, seguimiento de estados, asociación y retiro motivado de unidades, anulación pendiente con motivo y consulta/registro de traslados. Pantallas y acciones respetan los permisos del perfil; el backend conserva la autorización efectiva. Formularios validan datos y deshabilitan envíos simultáneos. Una respuesta incierta de escritura no se reintenta automáticamente. Sesión expirada vuelve al login y el estado de operaciones se descarta al abandonar la cuenta.

`OperationsScreen.kt` → `OperationsViewModel.kt` → `OperationsRepository` → `RemoteRepository.kt` → API Spring → PostgreSQL. Koin registra los componentes. El historial de traslados del backend viene ascendente: la ubicación actual se toma del último evento.

## Verificación ejecutada

- Compilación Android Debug aprobada.
- 14 pruebas Android Host aprobadas: 5 de errores HTTP, 3 de reglas y 6 nuevas del ViewModel (doble envío, líneas inválidas, respuesta incierta, conflicto, anulación y cancelación de trabajo/limpieza de estado).
- Backend: suite Maven y empaquetado aprobados; conservar sus informes Surefire locales.
- 58 comprobaciones reales HTTP/persistencia aprobadas. Evidencia: [http-resultados.json](evidencias/sprint2/http-resultados.json). Incluyen 200/201/204/400/401/403/409, dos líneas, asignación exclusiva, retiro, atención, rechazo con liberación, traslado, anulación con historial y revocación del token al logout.
- Script reproducible: `scripts/Verificar-Sprint2.py`; genera datos nuevos solo en la base aislada. No repetirlo innecesariamente.

Estas comprobaciones no acreditan todavía los recorridos visuales nuevos en Android. Samsung conectado y autorizado; no se halló `pe.indumotos.asapp` en el usuario principal al comprobarlo. No se borraron datos ni se desinstalaron aplicaciones.

## Pendientes y límites

Validar las pantallas nuevas con el Samsung y capturar evidencia auténtica; verificar APK exacto instalado. La imagen del emulador está incompleta y no se utiliza. HU-27 implementa la regla del backend (solo PENDIENTE); falta aceptación del responsable de negocio para declararla cerrada. No afirmar terminados los artefactos web, UML/Figma ni las tareas del equipo sin evidencia. iOS no se compiló en Windows. No hay offline/SQLDelight/refresh token implementados en esta app. Main permanece intacta; no se fusionan estas funciones automáticamente.

## Samsung: comprobación adicional

APK Debug 0.2.0-sprint2, código 2, instalado con éxito sin limpiar datos. Login real aprobado; listado real, detalle de solicitud #3 anulada e historial consultados; formulario abierto. Capturas en `evidencias/sprint2/samsung-*.png`. La captura de login incluye el aviso Samsung Pass, no una contraseña visible. No se guardó la credencial en Samsung Pass. Trazabilidad y hashes en `evidencias/sprint2/build.json`. Quedan por ejecutar las escrituras nuevas desde Android; no confundirlas con las 58 verificaciones del servidor.

## Mejora de selección solicitada el 19/11/2026

Nueva solicitud ofrece selectores marca → modelo → color, derivados de todas las páginas de inventario mediante GET motocicletas autenticado. Cambiar marca limpia modelo/color y cambiar modelo limpia color. Se eliminan duplicados sin distinguir mayúsculas; se conserva la escritura del servidor. No existe catálogo maestro en el backend: se permite entrada manual explícita para solicitar variantes aún no recibidas. No se inventan marcas ni se modifica el backend ni la base.

Sedes iniciales/destinos y transiciones de estado ya son seleccionables; chasis elegibles se eligen del resultado del servidor. Cantidad requiere número positivo; motivos/observaciones y nuevos identificadores siguen como entrada. Selector de motocicleta por chasis para Traslados y catálogos maestros para fichas/filtros son mejoras futuras, no verificaciones completadas en este corte.

Continuación manual: crear solicitud con dos líneas, verificar ID y estado Pendiente; pasar a preparación; asociar una unidad compatible sin duplicarla; comprobar rechazo o atención y su historial; registrar traslado de una unidad con ubicación inicial; anular otra solicitud pendiente con motivo. Conservar evidencias y contrastar los IDs con la API. HU-27 requiere aceptación de la regla de negocio; no declarar todo el Sprint cerrado con estas pantallas solamente.

Verificación de esta mejora: BUILD SUCCESSFUL; 15 pruebas Android Host aprobadas. Instalación automática bloqueada por política de ejecución; todavía no hay evidencia visual de los selectores en Samsung. El APK anterior permanece instalado.

## Corte de usabilidad y validación manual — 2026-11-19T17:04:34-05:00

Diez capturas originales preservadas en `evidencias/sprint2/manual-samsung`. Demuestran creación multilínea, preparación, asociación, atención, traslado y anulación; SQL confirma #5 ATENDIDA y #8 ANULADA. Correcciones: regreso fijo sobre los mensajes, botón de navegación con tamaño mínimo 48 dp; solo avance como acción primaria; rechazo/anulación en contorno y color de error; selectores con apariencia de campo, modelo deshabilitado sin marca y color sin modelo; reinicio de selectores dependientes; carga de historial tras creación sin repetir POST si falla su consulta; limpieza de destino al cambiar recorrido y bloqueo de traslado al mismo destino.

BUILD SUCCESSFUL y 15 pruebas Android Host aprobadas. Firma SHA256 sin cambios. Nuevo APK actualizado en Samsung con `install -r Success`, sin borrar datos. Validación visual de estas correcciones aún pendiente.

Excel original actualizado en Drive, verificado por SHA256 tras descarga: 12 To Do, 31 Doing, 7 Done. Solo cambiaron CORTE REAL y REGISTRO REAL; respaldo previo en Drive. No se convierten planes históricos en ejecución.

Para cierre: validar estas correcciones en teléfono y errores de formulario; completar revisión de permisos/escenarios negativos de las operaciones nuevas (el 403 global de API no acredita cada pantalla); aceptar regla de HU-27; adjuntar/validar UML, Figma, web y ER exigidos por las tareas del equipo; revisión/DoD y aceptación antes de merge a main. El avance móvil no acredita por sí solo todo el Sprint.

## Búsqueda por datos de negocio — 2026-11-19T17:18:50-05:00

Fichas ahora reutiliza la lista de inventario y su búsqueda por chasis; el detalle completo permite seleccionar/copiar el chasis y muestra confirmación de copia. La apertura de inventario/fichas carga la última página en orden inverso (un subconjunto reciente de hasta 20 unidades, no un catálogo maestro). La búsqueda conserva la paginación existente. Marcas/modelos/colores compartidos no son duplicados de unidades: el chasis distingue cada motocicleta.

Traslados consulta recientes y busca por chasis; una tarjeta de motocicleta resuelve su ID internamente y abre el historial. No se exige escribir ID. Requiere permiso de inventario además de ubicación para encontrar unidades; no se ampliaron permisos. Solicitudes usa modelo/color/cantidad y acciones «Elegir motocicletas para este modelo» / «Asignar esta motocicleta» en lugar del ID de línea.

Llegadas: nuevo GET /api/v1/llegadas?guia=...&limite=20, resultados recientes por fecha/ID descendente, búsqueda literal sin comodines SQL, máximo 50 y guía de hasta 120 caracteres. Misma autorización INVENTARIO_CONSULTAR. Interfaz por guía/lista; las fichas nuevas seleccionan recepción por guía (hasta 20 recientes) en vez de escribir llegadaId. No hay migraciones ni escrituras de negocio en esta corrección. Selección de recepciones antiguas para alta de ficha sigue limitada a las 20 recientes; la consulta de llegadas sí permite buscar por guía.

Validación: 65 pruebas backend (incluidas 2 nuevas para límites/normalización), empaquetado correcto; salud 200; listado 200, búsqueda por guía 200 y límite 51 rechazado con 400. Evidencia en consulta-llegadas.json. Android compilado y 15 pruebas aprobadas. No se instalaron cambios en el teléfono: ADB vacío; pendientes instalación segura, prueba visual y captura de copia de chasis/listas. No atribuir esta nueva UI a las capturas del APK anterior.

## Recuperación de servicios e instalación — 2026-11-19T17:46:16-05:00

Los commits de código móvil b7730cc y backend e0ab459 coinciden con sus ramas remotas. Se recuperaron Docker Desktop y el contenedor PostgreSQL existente (healthy), sin recrear volumen ni base. Backend local iniciado con el JAR ya verificado; Flyway desactivado en este arranque sobre el esquema existente. GET /api/health devuelve 200. USB 8089 → 8090 restablecido.

Se compararon las firmas SHA256 de APK instalado y nuevo: coinciden. Hash del APK nuevo coincide con build.json. Actualización `adb install -r`: Success, sin borrar datos. El Samsung abre el login; el usuario completa la autenticación manualmente. No se marcan como ejecutados los recorridos visuales de la nueva búsqueda/copia de chasis todavía.

Por instrucción del usuario, desde este corte se registra trabajo únicamente en la pestaña existente SCRUM S2 19-11-2026 del mismo Excel, respetando formato y sin enlaces. Web y Figma pertenecen a otros colaboradores y no bloquean por sí solos la entrega de su parte Android. El cierre móvil requiere sus propias pruebas y aceptación; no equivale a cierre integral del equipo.

## Revisión física final del incremento — 2026-11-19T17:53:20-05:00

Consultas de fichas/traslados/llegadas, copia y pegado real de chasis, secuencia de selectores, error por formulario incompleto, logout y login incorrecto/correcto comprobados. 17 capturas en evidencias/sprint2/validacion-final; cinco checks de selectores aprobados. No se repitieron compilaciones ni escrituras ya comprobadas. No se amplió el rol admin. Pendientes específicos documentados en el README de estas evidencias.
