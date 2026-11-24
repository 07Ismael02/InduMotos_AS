# Cuentas y consulta offline — 23/11/2026

## Ramas

App: feature/ismael-offline-cuentas, desde c74cbe4. Backend: feature/ismael-cuentas-presentacion, desde f551cce. No se modifica main ni se reescribe el historial. Los cambios anteriores permanecen en sus ramas originales.

## Datos locales de presentación

Solo copia indumotos_s2_paridad_20261123. Respaldo completo previo de266483 bytes verificado con pg_restore --list; archivo privado fuera del repositorio. Hash y cambios exactos en evidencias/cuentas-20261123/resultado-cuentas-catalogo.json. No se publica el dump ni contraseñas/hashes de usuarios.

| Cuenta | Rol |
|---|---|
| admin.indumotos | Administrador |
| almacen.indumotos | Almacén |
| inventarios.indumotos | Inventarios |
| comercial.indumotos | Comercial |
| facturacion.indumotos | Facturación |
| ensamblaje.indumotos | Ensamblaje |

Las cinco cuentas operativas reemplazan nombres sintéticos conservando sus IDs. La cuenta administrativa de pruebas redundante queda renombrada administracion.indumotos e inactiva; no se borra porque tiene historial. Se conserva admin.indumotos. Contraseña académica común establecida localmente por instrucción del responsable; BCrypt con sal independiente por cuenta. La política general de alta de usuarios no se debilitó; no son credenciales de producción ni se incorporan al APK/Git.

Seis logins reales HTTP200; roles/permisos efectivos registrados en verificacion-cuentas-presentacion.json. El responsable aprobó habilitar solo consulta a Facturación. Se añadió INVENTARIO_CONSULTAR al rol en la copia local: inventario y ficha devuelven200; crear/editar ficha, registrar llegada, trasladar y crear cuenta devuelven403. Sin nuevos permisos de escritura. Evidencia: verificacion-facturacion.json. Comercial y Ensamblaje conservan sus permisos de solicitudes; no reciben inventario por cambiar el nombre de cuenta.

Catálogo sintético3: Suzuki / GN125 / Amarillo, mismo ID. Guías1–3: Prueba1, Prueba2, Prueba3. Sedes: Almacén central, Tienda principal, Almacén auxiliar, Tienda de apoyo. Nombres anteriores y evidencias históricas no se falsifican; chasis, motores, vínculos y movimientos se conservan. No se modifican migraciones ni bases históricas. La configuración de cuentas/permisos corresponde solo a esta copia local; clonar Git no recrea automáticamente sus datos.

## Perfiles offline

0.3.3/código10 conserva varios perfiles independientes usando la caché SQLDelight existente; no requiere recrear ni migrar destructivamente SQLite. La instalación anterior conserva su perfil y sus borradores. Cada cuenta debe iniciar sesión online y disponer de datos descargados propios; no se copian datos del administrador a otros usuarios.

Al reabrir aparecen las cuentas habilitadas. La credencial nativa del dispositivo desbloquea la consulta local, no autentica contra el backend. Límite24h por perfil. Cerrar sesión explícitamente o recibir401 revoca solo el perfil activo; no elimina borradores ni los perfiles de otras cuentas. No se garantiza reapertura de cuentas cerradas explícitamente. Usuarios sin lecturas autorizadas descargadas no aparecen como consulta offline.

53 pruebas móviles aprobadas, incluyendo persistencia de varios perfiles tras reabrir SQLite, migración lógica del perfil anterior y revocación selectiva sin borrar borradores. Samsung: instalada0.3.3 con misma firma; login y descarga de Inventarios, Almacén, Administrador y Facturación; cierre/reapertura conserva las cuatro opciones simultáneas. Capturas originales revisadas en cuentas-offline-033.png y cuentas-offline-033-cuatro.png. No se afirma haber desbloqueado físicamente cada cuenta con PIN en este bloque; el desbloqueo nativo se comprobó en0.3.2 y no se cambió.

APK Debug0.3.3; manifiesto build-cuentas-20261123.json. Los resultados offline0.3.2 anteriores siguen siendo históricos válidos. Faltan recorridos integrales por rol, release e iOS; no se declara cerrado Sprint2.
