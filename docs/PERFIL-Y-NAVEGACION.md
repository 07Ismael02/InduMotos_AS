# Perfil, navegación y acceso local — 24/11/2026

Rama: feature/ismael-catalogo-ventas. Actualización Android 0.4.1-evolucion, código14. Backend y esquema sin modificaciones para estas correcciones.

## Correcciones

- Cerrar sesión deja de eliminar el perfil de consulta offline. Se elimina el token en memoria y se cierra el acceso actual; online se solicita además la revocación al servidor. El perfil conservado no contiene contraseña ni JWT, permanece separado por servidor/usuario y exige desbloqueo de Android. No se renueva su plazo de 24 horas al salir o volver a entrar offline. Un 401 confirmado del servidor sigue retirando el perfil local afectado.
- Una cuenta que la versión anterior ya retiró necesita iniciar sesión online nuevamente. No se inventan permisos ni se restaura una cuenta desde datos incompletos.
- Ajustes personales, Perfil y acceso y Preferencias se presentan como pantallas completas, con identidad, rol y permisos reales. Se conserva la paleta de Indumotos y el tema del dispositivo. No se añaden datos personales, estadísticas ni funciones ficticias de las referencias visuales.
- Atrás en Solicitudes vuelve de unidades al detalle y del detalle a la lista. Traslados vuelve del recorrido a la selección. Guías vuelve de ficha/foto/formulario/detalle al nivel anterior y mantiene la confirmación de cambios sin guardar. Inventario conserva el cierre de ficha/edición; los diálogos de usuarios, pagos y confirmaciones cierran primero su propio nivel. Desde el módulo se vuelve a Inicio, con aviso para salir; el segundo toque dentro de dos segundos termina la actividad.

## Verificación

Se ejecutaron 59 pruebas móviles, cero fallos/errores, incluidas las regresiones de repositorio HTTP/SQLite para cerrar sesión online y offline conservando el perfil sin sesión activa. Revisión física pendiente: ADB inalámbrico dejó de aceptar la dirección anterior y se solicitó al responsable la dirección actual. No se declara aprobado el recorrido visual por estas pruebas de repositorio.

Pendientes del bloque anterior: práctica de fotografías propias, ventas y offline/reconexión por hotspot. La conexión Wi-Fi con servidor es online; no equivale a modo offline.
