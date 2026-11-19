# Continuidad del aplicativo — 19/11/2026

## Backend localizado en esta PC

Clon en `C:/Users/socis/Documents/Back`. La rama activa de Roberto estaba en `d4acdd0`; se exportó la referencia local `origin/sprint-2` (`fbfaab5`) a una carpeta separada, sin cambiar el clon. Git remoto todavía no se ha verificado con una cuenta con acceso. El código de esta referencia incluye migraciones V8–V15 y estas rutas bajo `/api/v1`:

| HU | Rutas observadas en código |
|---|---|
| HU-05 | `/ubicaciones`, `/motocicletas/{id}/ubicacion` e historial |
| HU-06 | `/solicitudes` y `/solicitudes/{id}` |
| HU-07 | `/solicitudes/{id}/seguimiento` |
| HU-08 | `/motocicletas/elegibles` y `/solicitudes/{id}/lineas/{detalle}/unidades` |
| HU-09 | POST `/motocicletas/{id}/traslados` |
| HU-27 | POST `/solicitudes/{id}/anulacion` |

Estas rutas se comprobaron en los controladores, sin ejecutar llamadas. Docker Desktop no tenía el motor activo y `/api/health` no respondió en los puertos 8080 ni 8089. El clon no contiene `.env`. Para habilitar el login hace falta el entorno privado de desarrollo autorizado con la cuenta existente `admin.indumotos`, o la URL de una instancia ya en ejecución. Revisar DTO, permisos y reglas implementadas de HU-27 antes de integrar, y contrastarlas con las decisiones del equipo.

## Bloque actual en la nueva PC

Código y primera actualización documental: commit `2d12129085773b89669f38ae2748bde4f67c4ca1`. Cierre documental posterior incorpora [trazabilidad del APK](build-login-20261119.json) y [explicación de evidencias](evidencias/login-2026-11-19/README.md). Commits locales; no se hizo push ni merge. main permanece en `3d9334a`.

Base clonada: main `3d9334a`, limpia. Trabajo en `feature/login-identidad`, sin modificar main ni la rama de Roberto. Drive accesible: revisión real y matriz por HU en [REVISION-2026-11-19.md](REVISION-2026-11-19.md). HU-27 ya tiene 4 puntos en el plan de noviembre; la regla de corte sigue pendiente.

Implementado en código: colores claros/oscuros según el sistema en login y pantallas existentes, marca tipográfica adaptada al tema conservando el bitmap original, icono IM vectorial adaptativo con alternativa API 24/25, barras Android según tema conservando `decorView.post`, teclado de contraseña y eliminación del selector técnico. La URL Android llega desde BuildConfig, propiedad Gradle `indumotos.backendUrl` o variable `INDUMOTOS_BACKEND_URL`; por defecto se conserva loopback 8089. No cambia el contrato HTTP, las cuentas ni los permisos. Vinculado a HU-26, criterio de interfaz móvil (`DOD y DOR!C70`) y pedidos explícitos de identidad/conexión.

El script `Compilar-Android.ps1` permite indicar JavaHome/AndroidSdk/BackendUrl y verifica firma antes de copiar a un nombre de entrega único. Ya no impone la ruta Zulu de otra PC ni sobrescribe el APK histórico.

Verificación del bloque: `:androidApp:assembleDebug :shared:testAndroidHostTest` **BUILD SUCCESSFUL**, 8 pruebas sin errores/fallos/omisiones (3 RulesTest, 5 ApiErrorMapperTest). Gradle 9.1 y SDK 36 preparados con las versiones del proyecto. Un error de importación URI del script Gradle fue corregido antes del build exitoso. `help -Pindumotos.backendUrl=http://example.com --offline` rechazó la URL con el mensaje esperado; el BuildConfig generado conserva loopback 8089. Sintaxis PowerShell comprobada; no se repitió todo el script de entrega después del build.

APK verificado con apksigner y aapt: `pe.indumotos.asapp`, versión conservada `0.1.0-sprint1` / code 1, 22 641 741 bytes, SHA-256 `8382BCF66E4EDBD8296BF9483011926CE04D5800D27F0C7F47BBD49812B62380`. Certificado nuevo `ef933ea44b732c71c06f5f7d897b0af0ef7b13942dc8ec4c744eee4b6402be9c`; Samsung instalado `23c1a0373560f28c3d2b64103e50a0dff54bb955fd75b20ac40d52253e33b0f0`. **Firmas distintas: no se intentó reemplazar ni desinstalar la app del Samsung.** Hace falta la clave anterior por transferencia privada para actualizarla.

Instalación nueva en AVD temporal API 37: Success; arranque frío `Status: ok`, 2046 ms; buffer crash vacío. Inspección visual real de login claro/oscuro, barras, icono y teclado en [evidencias/login-2026-11-19](evidencias/login-2026-11-19). Selector técnico ausente; botón deshabilitado con campos vacíos. No se inició sesión, crearon cuentas ni registros. No se comprobaron otras pantallas con sesión ni el teléfono con la nueva versión.

Bloqueo externo: el backend correcto devuelve Repository not found y no aparece en el conector GitHub. El usuario indica que pedirá acceso y que se priorice el aplicativo. No se inventan rutas Sprint 2, datos ni reglas para simular integración. Siguiente paso: obtener el contrato actual de la base de Roberto (o OpenAPI autorizado) y las decisiones pendientes de estados/anulación; retomar las tareas T07/T08 de las seis HU de Sprint 2 en una feature propia. Para login real, configurar un backend autorizado y acceso privado de admin.indumotos. Para Samsung, resolver primero la firma.

## Antecedente del equipo anterior (antes de este bloque)

El incremento publicado terminó en `8fd7618`; el código que generó el APK comprobado corresponde a `3de365b`. Se subieron develop y feature/integrador-13; posteriormente se creó main con esa base y se configuró como predeterminada. El backend correcto es BACK-END-INDUMOTOS, feature/roberto-sprint1-backend, d4acdd0; no se cambió su código.

Se creó localmente feature/login-identidad para los siguientes pedidos del usuario. **No se implementaron aún** los cambios visuales: la conversación se detuvo para aclarar cuentas y backend. Solo se descargó la tipografía Audiowide y su licencia como material de trabajo; no constituyen una nueva identidad final ni un cambio validado. No hay que buscar un APK con esas correcciones porque no se generó.

## Pedidos abiertos, en orden

1. Mantener main estable y separar funcionalidades en ramas; no partir automáticamente de develop, que conserva una base antigua.
2. Icono de launcher reconocible: el logo completo sobre negro queda diminuto en la cuadrícula de apps. Diseñar una variante legible, respetando la identidad Indumotos y validando el icono en el teléfono.
3. Login profesional con tema claro y oscuro reales: quitar el rectángulo negro del logo en modo claro; asegurar contraste del nombre/logotipo y evitar letras blancas ilegibles sobre fondo claro. La instrucción de color del usuario expresa adaptar la marca al tema; no resolverla con una combinación sin contraste. Conservar el logo original y revisar las variantes visualmente.
4. Retirar Configuración de conexión del flujo del trabajador. La URL se define por entorno/configuración de compilación y no la configura cada usuario. Actualmente la UI aún contiene el campo y default localhost:8089.
5. Mantener admin.indumotos en la base nueva; **no crear una cuenta de demostración**. No restablecer a Roberto del proyecto anterior. Si se cambia la contraseña actual, hacerlo solo mediante entrada privada y procedimiento seguro para la cuenta identificada. La contraseña no se ha cambiado; no documentar una nueva inventada.
6. Sprint 2: ubicación y traslados, solicitudes y ensamblaje; revisar HU-27 y reglas pendientes. El usuario requiere avanzar para hoy, 19/11. Ver SPRINT2.md.

## Límites del contexto transferible

Git contiene código, documentación y capturas; no contiene PostgreSQL, el estado de Docker, el proceso Spring, las credenciales, la clave Debug privada ni las sesiones. La nueva IA debe verificar el entorno de esa PC antes de afirmar que algo está funcionando. No repetir instalaciones de la recuperación de PharmaMobile.

No modificar la aplicación de farmacia ni copiar sus credenciales, datos, applicationId o migraciones. Reutilizar como referencia: menú lateral, identidad y rol, navegación según permisos, separación UI/casos de uso/repositorio, errores y verificaciones auténticas. SQLDelight/offline de farmacia no está en Indumotos y requiere contrato y alcance propios antes de adaptarse.

## Próxima acción concreta del nuevo chat

Leer README, este archivo, SPRINT2 y el backend. Inspeccionar ramas remotas actuales; coordinar el contrato del Sprint 2 con la base de Roberto. Producir una tabla corta de endpoints/permisos disponibles y faltantes, resolver lo comprobablemente bloqueante del login y avanzar al recorrido vertical de solicitud → atención → chasis asociado → traslado. Preguntar solo por secretos privados o decisiones operativas que no estén en las fuentes, mientras se continúa el resto.

## Cómo registrar cada avance

- Fecha/hora y rama/commit.
- Problema concreto y archivos afectados.
- Resultado de compilación, prueba o interacción realmente ejecutada, con ruta de evidencia.
- Diferenciar código terminado, dispositivo comprobado y pendientes.
- Actualizar contrato cuando cambie la API; no sustituir endpoints reales por respuestas simuladas para aparentar integración.
- Dejar instrucciones de arranque reproducibles y sin rutas personales obligatorias ni secretos.
