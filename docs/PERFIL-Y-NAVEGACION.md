# Perfil, navegación y acceso local — 24/11/2026

Rama: feature/ismael-catalogo-ventas. Actualización Android 0.4.1-evolucion, código14. Backend y esquema sin modificaciones para estas correcciones.

## Correcciones

- Cerrar sesión deja de eliminar el perfil de consulta offline. Se elimina el token en memoria y se cierra el acceso actual; online se solicita además la revocación al servidor. El perfil conservado no contiene contraseña ni JWT, permanece separado por servidor/usuario y exige desbloqueo de Android. No se renueva su plazo de 24 horas al salir o volver a entrar offline. Un 401 confirmado del servidor sigue retirando el perfil local afectado.
- Una cuenta que la versión anterior ya retiró necesita iniciar sesión online nuevamente. No se inventan permisos ni se restaura una cuenta desde datos incompletos.
- Ajustes personales, Perfil y acceso y Preferencias se presentan como pantallas completas, con identidad, rol y permisos reales. Se conserva la paleta de Indumotos y el tema del dispositivo. No se añaden datos personales, estadísticas ni funciones ficticias de las referencias visuales.
- Atrás en Solicitudes vuelve de unidades al detalle y del detalle a la lista. Traslados vuelve del recorrido a la selección. Guías vuelve de ficha/foto/formulario/detalle al nivel anterior y mantiene la confirmación de cambios sin guardar. Inventario conserva el cierre de ficha/edición; los diálogos de usuarios, pagos y confirmaciones cierran primero su propio nivel. Desde el módulo se vuelve a Inicio, con aviso para salir; el segundo toque dentro de dos segundos termina la actividad.

## Verificación

Se ejecutaron 59 pruebas móviles, cero fallos/errores, incluidas las regresiones de repositorio HTTP/SQLite para cerrar sesión online y offline conservando el perfil sin sesión activa. Compilación Android aprobada y firma original verificada. Se recuperó ADB inalámbrico sin nuevo emparejamiento.

En el Samsung se comprobaron Perfil → Ajustes, Preferencias → Ajustes, detalle de Solicitud → lista y recorrido de Traslados → selección utilizando KEYCODE_BACK. Tras cerrar sesión online, admin.indumotos siguió visible en el selector offline. Se capturó la barra compacta en horizontal; fecha, mensajes y acciones se consultan mediante «Ver estado». Ajuste posterior de contraste del tema oscuro compilado y con suite aprobada. La salida y reentrada desde una sesión offline requiere todavía completar el desbloqueo de Android en la prueba física; no se declara ese último recorrido aprobado únicamente por la regresión automatizada.

Evidencias privadas conservadas fuera de Git: 041-ajustes.png, 041-perfil.png, 041-atras-solicitudes.png, 041-atras-traslados.png, 041-horizontal.png y 041-cuenta-conservada.png en tmp/sprint2-auditoria/privado-paridad del espacio de trabajo. Las capturas preceden al último ajuste de contraste; no acreditan su apariencia final.

### Cierre del defecto de cuentas offline

En el APK final se solicitó el desbloqueo de Android y el responsable lo completó personalmente. Se observó la sesión de consulta local, se cerró desde Ajustes y se abrió otra vez el selector: admin.indumotos permaneció visible y seleccionable. Evidencias privadas: 041-sesion-local.png y 041-cuenta-tras-salida-offline.png. Ajustes con el contraste final revisado visualmente en 041-ajustes-final.png. Queda pendiente, si se desea verificar el segundo desbloqueo físico completo, pulsar Abrir datos y volver a desbloquear; no hace falta restaurar la cuenta ni iniciar sesión online de nuevo dentro del plazo vigente.

APK final y hash en build-041.json. Commits de código: 0216b95 y c48370b. No cambios de backend, migraciones ni datos de negocio en esta corrección.

Pendientes del bloque anterior: práctica de fotografías propias, ventas y offline/reconexión por hotspot. La conexión Wi-Fi con servidor es online; no equivale a modo offline.
