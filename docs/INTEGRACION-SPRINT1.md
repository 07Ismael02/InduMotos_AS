# Integración móvil Sprint 1 — 17/11/2026

## Alcance confirmado

El usuario confirmó usar `feature/roberto-sprint1-backend` como contrato inicial y `feature/integrador-13` para el aplicativo. El remoto móvil estaba vacío: se conserva una base técnica en `develop` y se adapta el trabajo en la rama de integración. No se fusionó ni publicó nada.

Fuentes consultadas: [carpeta del proyecto](https://drive.google.com/drive/folders/12AGdqJ3yhDUdPkWXQEAXKuFUHok-WACJ), [épicas, HU y Sprint 1 revisado](https://drive.google.com/file/d/1QS8WveG1IuCraO6FeTW5GrLiUMklg81O/view), [matriz preliminar](https://drive.google.com/file/d/1yXUff7L-U4ItsqzqRBkMpLLIA10r67ii/view) y [nota de avance](https://drive.google.com/file/d/1wNqH9Z67iPEqik8Qwbyvdijl3pMDv17W/view). Parte de esos documentos distingue propuestas de reglas aprobadas; esta implementación no certifica el DoD ni inventa Sprint Review.

Se priorizan HU-01/03 (consulta y filtros), HU-02 (ficha), HU-04 (llegada), HU-14/15 (cuentas y permisos) y acceso HU-26. Solicitudes, ubicaciones, movimientos, disponibilidad e inconsistencias pertenecen a incrementos posteriores y no tienen endpoints disponibles en este backend. Tampoco se traslada el catálogo farmacéutico ni su sincronización a un dominio diferente sin requisitos y contrato propios.

## Conexiones y responsabilidades

`App.kt → LogisticsViewModel → LogisticsUseCase/LogisticsRepository → RemoteRepository → Ktor/OkHttp → Spring Boot → PostgreSQL`.

- `App.kt`: pantallas, drawer, marca y formularios. No conoce SQL ni credenciales de base.
- `presentation/LogisticsViewModel.kt`: estados, permisos visibles, consultas, errores y navegación.
- `domain/Models.kt`: identidad, filtros, contratos y módulos del Sprint 1.
- `domain/LogisticsUseCase.kt`: validación y serialización de los campos de escritura; preserva literalmente la contraseña.
- `data/RemoteRepository.kt`: rutas, JWT Bearer y respuestas reales. No llama a `/mobile/*`, ausente en el backend seleccionado.
- `androidApp`: host Android, configuración HTTP local e identificador separado.

## Contrato reutilizado

| Operación | Ruta bajo `/api/v1` | Condición |
|---|---|---|
| Login y perfil | POST `auth/login`, GET `auth/me` | JSON nombreUsuario/clave; accessToken/expiresIn; perfil con rolCodigo/permisos |
| Logout | POST `auth/logout` | Revoca JWT antes de cerrar la sesión local |
| Inventario | GET `motocicletas` | chasis, marca, modelo, color, pagina y tamano; consume items y totales |
| Ficha | GET `motocicletas/{id}`, POST `motocicletas` | Consulta o registro según permisos; llegadaId real |
| Llegada | POST `llegadas`, GET `llegadas/{id}/detalle` | No se inventa un listado de recepciones inexistente |
| Crear cuenta | POST `usuarios` | USUARIOS_CREAR |
| Accesos | GET `usuarios/{id}/permisos`, PUT `usuarios/{id}/rol`, PUT/DELETE `usuarios/{id}/permisos/{codigo}` | PERMISOS_ASIGNAR; confirmación explícita en pantalla |

## Entorno aislado

Backend sin cambios de código, HEAD `d4acdd0641de470b3777ac0910fb7e76b04ad610`. JAR en la copia local `Documents/PharmaMobil/proyecto-final-dam/BACK-END-INDUMOTOS/target`. Escucha únicamente en `127.0.0.1:8089`.

Se creó una base **nueva** `indumotos_as_sprint1` dentro del PostgreSQL local ya disponible, contenedor `indumotos-distribuidas-db-1`, puerto local 55432. Las bases anteriores no se alteraron. Flyway V1–V7 se aplicó únicamente al esquema vacío; no se editaron migraciones. No se recreó ningún volumen ni se añadieron motocicletas ficticias. El usuario inicial `admin.indumotos` fue creado por el bootstrap oficial con contraseña aleatoria nueva; no es una contraseña recuperada de una cuenta previa. Configuración privada con ACL fuera de Git.

El certificado del APK es **Android Debug**, no una clave de distribución. El JWT permanece solo en memoria; al terminar el proceso se pide login. No hay restauración offline, almacenamiento SQLDelight ni refresh token en este incremento. Esto no equivale al flujo offline ya implementado en PharmaMobile. HTTP 401 vuelve al login; 403 conserva la sesión y muestra falta de permiso. Si no se confirma la revocación remota por falta de conexión, el logout muestra error y conserva la sesión.

## Comprobaciones ejecutadas

- Backend Maven package, omitiendo compilación y ejecución de pruebas: BUILD SUCCESS.
- Health HTTP 200; login y auth/me del administrador: 200; rol ADMIN y tres permisos habilitados.
- GET paginado de inventario: 200, total 0. Logout de la comprobación HTTP ejecutado.
- Flyway: versiones 1 a 7 con success=true.
- APK Debug: assembleDebug aprobado y firma verificada con apksigner.
- Samsung SM-S948U1, Android 17/API 37: instalación independiente, arranque, login real, identidad/rol, menú lateral y consulta de inventario vacío por USB.
- Evidencias originales: `evidencias/01-login-samsung.png`, `02-inicio-admin-samsung.png`, `03-menu-admin-samsung.png`, `04-inventario-samsung.png`. Se revisan visualmente; no se retocan para simular resultados.

## Pendientes reales

1. Validar manualmente altas de cuenta, llegada y ficha; duplicados y validación; roles Almacén/Inventarios; cambios y denegación individual; caducidad y logout desde la app.
2. Revisar fechas y mensajes con registros reales de prueba. La base está vacía y no se inventan fichas para presentar un catálogo.
3. Ejecutar commonTest y regresión cuando se autorice la fase de pruebas; se adaptaron los dobles existentes al contrato nuevo pero no se ejecutaron suites.
4. Firma release y publicación remota, cuando corresponda. No hay AAB release ni tag final.
5. Extender backend y requisitos de los siguientes sprints antes de habilitar esos módulos. Offline y persistencia protegida deben especificarse por cuenta y por operación antes de implementarlos.
6. Validar iOS en macOS/Xcode; no se generó IPA en Windows.

PharmaMobile y PharmaMobile-Backend no se modificaron. Sus credenciales, datos y artefactos se mantienen separados.

## Cierre de la revisión inicial en dispositivo

Se detectó y corrigió IND-AS-001: cierre al iniciar en Samsung/API 37 por solicitar `Window.getInsetsController()` antes de crear la vista. Solución: configurar las barras después de `setContent`, desde `window.decorView.post`. El APK corregido tuvo arranque frío correcto y se volvió a comprobar login, inicio, menú e inventario en el Samsung. Las cuatro capturas se actualizaron con esta ejecución; la primera captura incorrecta del escritorio del teléfono quedó sustituida y no es evidencia del login.

El cambio de código está en `3de365b`; trazabilidad exacta de APK, tamaño, hash y certificado en `build-sprint1.json`. APK local `entrega/Indumotos-AS-Sprint1-debug.apk`, 23 270 113 bytes. La comprobación no certifica registros de llegada/ficha ni cambios de permisos: esas operaciones continúan pendientes de validación manual.
