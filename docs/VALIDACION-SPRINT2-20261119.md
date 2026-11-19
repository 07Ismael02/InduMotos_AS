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
