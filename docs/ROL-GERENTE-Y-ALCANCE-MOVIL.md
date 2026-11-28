# Rol Gerente y alcance del aplicativo — 28/11/2026

## Observación del profesor

1. Se concedió demasiado poder a «Administrador». Las decisiones de negocio corresponden a un rol **Gerente**; Administración debe limitarse a cuentas y accesos.
2. El aplicativo no replica la web. Es una herramienta rápida de campo, conectada al mismo backend y coherente con la web, centrada en la llegada de la guía.

## Reparto acordado

- Web y backend: Roberto. Android: Ismael. Lo que afecte a ambos se consulta o comunica antes de implementarlo.
- El rol GERENTE todavía **no existe** en el backend. `main` (aee0e86) y la web (199022b) concedieron todos los permisos a ADMIN; esta propuesta va en dirección contraria y requiere acuerdo con Roberto.

## Propuesta de permisos para el backend (pendiente de acuerdo)

Códigos tomados de las migraciones reales. REPORTES_CONSULTAR y PAGOS_* existen solo en `feature/ismael-catalogo-ventas` (V32/V34).

| Rol | Permisos propuestos |
|---|---|
| GERENTE | INVENTARIO_CONSULTAR, UBICACIONES_CONSULTAR, SOLICITUDES_CONSULTAR, SOLICITUDES_ANULAR (autoriza HU-27), DISPONIBILIDAD_CONSULTAR, DIFERENCIAS_CONSULTAR, TIENDA_CONSULTAR, COMPANIA_CONSULTAR, CATALOGO_GESTIONAR, REPORTES_CONSULTAR, PAGOS_CONFIRMAR |
| ADMIN | USUARIOS_CREAR, USUARIOS_CONSULTAR, USUARIOS_DESACTIVAR, PERMISOS_ASIGNAR |

Forma segura: una migración nueva posterior a la última aplicada en `main`, que cree GERENTE, le asigne sus permisos y retire de ADMIN los operativos. No editar ni renumerar migraciones aplicadas. Una cuenta Gerente debe crearla Administración desde la web; no se inventan cuentas ni contraseñas. Revisar en la web el cambio «Mostrar los permisos de ADMIN sin opción de denegarlos».

## Cambios del aplicativo (rama feature/ismael-alcance-movil)

| Commit | Cambio |
|---|---|
| ecb1d37 | Cuentas, roles y permisos se gestionan en la web; el perfil sigue mostrando permisos efectivos |
| c06373f | Ventas, stock por tienda y pagos retirados (Sprint 3, web) |
| 9d551c9 | Catálogo solo de lectura para elegir marca/modelo/color en fichas y solicitudes |
| 4a77805 | Rol GERENTE reconocido; ninguna pantalla depende ya del código ADMIN |
| 8be24eb | Orden de campo: Llegadas → Fichas → Inventario → Traslados → Solicitudes |

El aplicativo decide los módulos por permisos del servidor. Cuando el backend cree GERENTE, el móvil lo mostrará sin otra versión; la autorización final sigue siendo del backend.

## Módulos que conserva el móvil

| Módulo | Uso de campo | Roles típicos |
|---|---|---|
| Llegadas | Registrar la guía recibida con fotos de cámara o galería | Almacén |
| Fichas | Registrar las motos de la guía, borradores offline | Inventarios, Almacén |
| Inventario | Buscar por chasis, ficha, ubicación, recorrido | Todos los que consultan |
| Traslados | Mover a tienda o despacho con historial (HU-09) | Almacén |
| Solicitudes | Crear, avanzar, asignar chasis y anular (HU-06/07/08/27) | Comercial, Ensamblaje, Gerente |

## Verificación

`:shared:testAndroidHostTest` 59 pruebas sin fallos (64 anteriores − 8 retiradas con sus módulos + 3 nuevas de navegación por rol) y `:androidApp:assembleDebug` exitoso el 28/11/2026. No se instaló en el Samsung ni se probó contra la API en este bloque; versión todavía 0.4.4/código 17.

## Recorrido físico — 28/11/2026

APK 0.5.0-campo (código 18, commit bc3b4a9) instalado con install -r y la firma original en el Samsung, conectado por USB a la API de la imagen 67e54f2. Con admin.indumotos el inicio muestra solo los cinco módulos de campo y Llegadas se abre en modo consulta. Con almacen.indumotos aparece primero Llegadas con el botón de registro. Se registró una guía con una foto de cámara. El servidor confirma la llegada 5, del receptor almacen.indumotos, con una imagen JPEG de 413 226 bytes. Las fechas se ven en hora de Lima. Evidencia en evidencias/alcance-movil-20261128.

Observaciones abiertas: el número de guía acepta letras, igual que la web y el backend, y se aceptó una cantidad recibida mayor que la declarada. No se registraron fichas ni se finalizó el stock de esa guía.
