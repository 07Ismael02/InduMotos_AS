# Indumotos AS — leer primero para continuar

**Actualizado: 19/11/2026 (America/Lima).** Este README es el punto de entrada para el equipo y para una IA en otra PC. Describe trabajo real, decisiones del usuario y pendientes; no certifica que el Sprint 1 o el Sprint 2 estén cerrados.

## Objetivo inmediato

**HU-05:** ubicación integrada en la ficha de inventario en `codex/feature/sprint2-ubicacion`. Compilación y pruebas Android Host aprobadas. Con datos sintéticos autorizados, la API registró la ubicación y confirmó el mismo destino y responsable en una consulta posterior. Falta verificar el recorrido visual en Android.

**Mi cuenta en Android:** toca tu avatar o nombre al pie del menú lateral. Desde allí abre Perfil, Apariencia o Cerrar sesión. La elección de tema se guarda en esta instalación.

**API local operativa:** Sprint 2 `fbfaab5` ejecutado en Docker. Salud, login, perfil y logout verificados por HTTP. El emulador usa ADB Reverse de 8089 a 8080. Ver [arranque y credenciales locales](docs/CONTINUIDAD.md).

**Backend localizado:** el clon `C:/Users/socis/Documents/Back` contiene `origin/sprint-2` en `fbfaab5`. Ya se pueden consultar las rutas del Sprint 2 desde esa referencia local. El servidor sigue pendiente de arranque y conexión con el Samsung. Ver [continuidad](docs/CONTINUIDAD.md).

Último commit de código de este bloque: `2d12129` en `feature/login-identidad` (local, sin push ni merge). [APK y firma](docs/build-login-20261119.json), [capturas y pruebas](docs/evidencias/login-2026-11-19/README.md). APK local ignorado por Git: `entrega/Indumotos-AS-login-20261119-debug.apk`; no instalar sobre el Samsung con la firma actual.

Completar un incremento Android del **Sprint 2 durante el 19/11/2026**, con solicitudes de ensamblaje, unidades asociadas, ubicación y traslados conectados al backend real, preservando el acceso e inventario del Sprint 1. La fecha es una prioridad del usuario, no evidencia de finalización. Primero leer [el plan y criterios del Sprint 2](docs/SPRINT2.md) y [el contexto de continuidad](docs/CONTINUIDAD.md).

**Bloque actual, 19/11/2026:** mejoras de login, temas, icono y configuración de entorno implementadas en `feature/login-identidad`. Compilación y 8 pruebas aprobadas; login claro/oscuro, teclado e icono comprobados en emulador API 37. No se actualizó el Samsung: la firma Debug nueva no coincide con la instalada. No confundir esta validación visual con autenticación real ni con cierre de HU-26. Se revisó Drive y se encontró planificación de noviembre posterior al libro citado originalmente: [fuentes, evidencia y estado por HU](docs/REVISION-2026-11-19.md). El backend actual no es accesible desde esta PC; el usuario está gestionando acceso y prioriza continuar el aplicativo.

## Repositorios correctos y ramas

| Repositorio / rama | Estado conocido y uso |
|---|---|
| [InduMotos_AS](https://github.com/07Ismael02/InduMotos_AS) / `main` | Rama predeterminada y base funcional conservada. Código del incremento inicial en `8fd7618`; las actualizaciones posteriores de continuidad son documentación. No desarrollar funciones directamente aquí. |
| `feature/integrador-13` | Primera integración móvil; HEAD histórico `8fd76182132c38d75c7fc7f313849bfcda31a7b4`. El nombre no significa que una práctica S13 esté aprobada. |
| `feature/login-identidad` | Mejoras locales del 19/11: temas, marca, icono y conexión por compilación. Ver CONTINUIDAD; no fusionada a main. |
| `develop` | Base técnica antigua `1c900da`; **no usarla como base actual** sin integrar de manera explícita los cambios necesarios. |
| `docs/continuidad-sprint2` | Actualización documental del 19/11/2026. No implementa el Sprint 2. |
| [BACK-END-INDUMOTOS](https://github.com/Akatosh4019/BACK-END-INDUMOTOS) / `feature/roberto-sprint1-backend` | Backend confirmado por el usuario. Último HEAD remoto observado el 19/11: `d4acdd0641de470b3777ac0910fb7e76b04ad610`. También existe `feature/hu-01-busqueda-chasis`; no se observó main/develop. Volver a consultar porque el equipo sigue trabajando. |

**No confundir** con `Akatosh4019/backend-DAM-final` ni con `PROYECTO-FINAL-DAM` o la web Angular: son trabajos anteriores y no el backend/destino actual del aplicativo. PharmaMobile se usa solo como referencia y no se modifica.

## Estado de funciones y verificaciones

| Elemento | Estado real |
|---|---|
| Kotlin Multiplatform / Compose, androidApp + shared, Koin y Ktor | Implementados; APK Debug compilado el 17/11. |
| Login JWT, perfil y rol visible, menú lateral por permiso | Comprobados en Samsung SM-S948U1, Android 17/API 37. |
| Inventario, filtros y paginación | Implementados contra GET motocicletas. En teléfono se comprobó consulta vacía, no un catálogo completo. |
| Alta/consulta de llegada y ficha; cuentas y roles/excepciones | Implementadas en el móvil contra las rutas del Sprint 1; recorrido de escritura y otros roles **pendientes de validación**. |
| Bug IND-AS-001 al iniciar en Samsung | Corregido en `3de365b`: configurar barras desde decorView.post después de setContent; nuevo arranque frío comprobado. |
| Suites automatizadas | 19/11: 8 pruebas Android Host aprobadas (RulesTest: 3; ApiErrorMapperTest: 5). No cubren autenticación contra servidor ni Sprint 2. |
| Offline, SQLDelight, refresh token | No implementados en esta app. No presentar el offline de PharmaMobile como propio. |
| iOS | Target de origen existente; no compilado ni firmado en Windows. |
| Sprint 2 | Pendiente. La base histórica inspeccionada no ofrecía endpoints logísticos; acceso al backend actual bloqueado desde esta PC. |

Evidencia y contrato inicial: [INTEGRACION-SPRINT1.md](docs/INTEGRACION-SPRINT1.md). Trazabilidad del APK del 17/11: [build-sprint1.json](docs/build-sprint1.json). Capturas reales: [docs/evidencias](docs/evidencias). No repetir una verificación aprobada sin cambio o motivo técnico, pero una función nueva sí requiere comprobación pertinente.

## Identidad, sesión y decisión sobre credenciales

La cuenta del entorno **nuevo** es `admin.indumotos`. El usuario eligió mantenerla y rechazó crear una cuenta de demostración. No inventar ni publicar contraseñas; una futura sustitución debe introducirse de forma privada y actualizar la cuenta correcta con un procedimiento seguro. **No se cambió su contraseña en el trabajo anterior.**

`roberto` pertenece al backend/entorno anterior (puerto 8088); su contraseña aleatoria se generó en un script local, no es una contraseña fija en el código. No restablecerla ni tocar esa base para arreglar el aplicativo nuevo. El bootstrap del backend actual solo crea un administrador si no existe: cambiar ADMIN_PASSWORD en un archivo no restablece una cuenta existente.

El backend usa BCrypt (coste 12) y JWT Bearer de 900 segundos, perfil auth/me y logout con revocación. El móvil mantiene el token solo en memoria y vuelve al login al recibir 401; 403 informa falta de permiso. No rebajar esas medidas para facilitar la presentación.

## Continuar en otra PC

1. Clonar **ambos repositorios correctos** y leer los README/AGENTS de la rama elegida antes de editar. No asumir que el entorno local del 17/11 sigue disponible.
2. Inspeccionar git status, remotos, ramas y commits; preservar cambios de compañeros. Para login usar `feature/login-identidad`; para Sprint 2 crear `feature/sprint-2-integracion` desde main actualizado, incorporando solo mejoras comprobadas. En backend usar una feature propia desde la base acordada con Roberto, no cambiarle su rama de trabajo.
3. Revisar herramientas existentes: JDK 21, SDK Android 36, Android Studio y wrapper del repo. Este repo usa Gradle 9.1.0 y AGP 9.0.1; confirmar en los archivos. No copiar las versiones diferentes de PharmaMobile por inercia.
4. Preparar PostgreSQL local o un backend de desarrollo acordado; inspeccionar compose.yaml, application.yml y variables del backend antes de iniciarlo. No crear un volumen ni lanzar migraciones sobre una base ajena sin identificarla. No usar down -v, clean o reset para resolver fallos.
5. Obtener/configurar **privadamente** DB_URL, DB_USER, DB_PASSWORD, ADMIN_USERNAME, ADMIN_PASSWORD, JWT_SECRET_BASE64, PORT y SERVER_ADDRESS. Contraseñas, .env y el archivo local del equipo anterior no están en Git. Una nueva PC con una base nueva no contiene los datos del equipo anterior: requerirá restauración segura o datos de prueba expresamente acordados.
6. Ajustar JAVA_HOME y sdk.dir a esa PC. `scripts/Compilar-Android.ps1` y `Iniciar-Backend.ps1` contienen rutas de la PC original; revisarlas antes de usarlos. Alternativa: wrapper Gradle desde la raíz y Maven/JDK del entorno para el backend. No instalar herramientas ni ejecutar pruebas indiscriminadamente.
7. Para un teléfono autorizado: `adb devices -l`, después `adb -s <serial-real> reverse tcp:8089 tcp:8089`. El puerto debe coincidir con el backend. Android obtiene la URL de `-Pindumotos.backendUrl` o `INDUMOTOS_BACKEND_URL`; predeterminado `http://127.0.0.1:8089` para ADB Reverse. `http://10.0.2.2:8089` es exclusivo del emulador. No existe selector técnico en el login. Se acepta HTTP solo para esas direcciones locales y localhost; los demás servidores requieren HTTPS. No abrir puertos de la red universitaria ni exponer PostgreSQL.
8. Antes de instalar, comprobar applicationId, versión y **firma**. La clave Debug de otra PC será distinta: transferir la clave anterior de forma privada o acordar una instalación/perfil aislado. No desinstalar ni borrar datos ante conflicto. No cambiar applicationId o firma a escondidas.

Comandos iniciales, desde la carpeta de trabajo elegida (no contienen secretos):

```powershell
git clone https://github.com/07Ismael02/InduMotos_AS.git
git clone --branch feature/roberto-sprint1-backend https://github.com/Akatosh4019/BACK-END-INDUMOTOS.git
cd InduMotos_AS
git status --short
git branch -avv
git switch feature/login-identidad
# O, para Sprint 2, desde main actualizado y limpio:
# git switch -c feature/sprint-2-integracion
# Con JAVA_HOME/SDK correctos y solo si hace falta generar el APK:
# ./gradlew.bat :androidApp:assembleDebug --console=plain --max-workers=2
```

## Entorno anterior: antecedente, no configuración portable

El 17/11 se ejecutó el backend correcto en 127.0.0.1:8089, con base nueva `indumotos_as_sprint1`, PostgreSQL en contenedor `indumotos-distribuidas-db-1`, puerto loopback 55432. Flyway V1–V7 aplicado allí. Configuración privada del equipo: `%LOCALAPPDATA%/IndumotosAS/sprint1.env`. No publicar ese archivo. El código del backend no fue modificado.

App: `pe.indumotos.asapp`, nombre **Indumotos AS**, versión 0.1.0-sprint1, firma Android Debug. APK anterior local ignorado: `entrega/Indumotos-AS-Sprint1-debug.apk`. SHA-256 `706A9AB48287463505E7DBC00102BE1EB80CCF3AC96AFAF091519E2F5CA00A91`. No hay release, AAB de distribución ni IPA acreditados.

## Regla de actualización para quien continúe

Al terminar cada bloque, actualizar este README o CONTINUIDAD con fecha, rama/commit, archivos cambiados, verificaciones ejecutadas, pendientes y siguiente paso. Distinguir implementado de comprobado. Mantener main como referencia estable; commits pequeños en feature y revisar diferencias antes de proponer integración. No hacer force push, mezclar repositorios, publicar secretos ni dar por completado un Sprint por tener pantallas.

Documentación compartida: [Drive del equipo](https://drive.google.com/drive/folders/1pMnDFe5EijNiOnY7sDzdXngcv7KPniqv). El alcance actualizado y las reglas pendientes se detallan en [SPRINT2.md](docs/SPRINT2.md).

### Copias de continuidad en el Drive compartido

- [Leer primero / README](https://drive.google.com/file/d/13N_7sMQOwAYaWBjzDUWqhRwHH3Okr5QS/view).
- [Contexto técnico y último pendiente](https://drive.google.com/file/d/1dmfCSNDYXovl4C9m6If5cfqqNefFNrsj/view).
- [Sprint 2: ejecución y criterios](https://drive.google.com/file/d/1BVhCWbrwADLyXVj773aTrFvmvoItZL8V/view).

Git es la fuente del código y del estado por commit. Las copias fechadas de Drive sirven al equipo; si divergen, contrastar fecha/commit y actualizar la continuidad antes de implementar.
