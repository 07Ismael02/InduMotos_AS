# Indumotos AS — aplicativo móvil

Aplicación Kotlin Multiplatform y Compose para el **Sprint 1**, usando PharmaMobile como referencia de experiencia de usuario y separación de capas. El proyecto PharmaMobile permanece intacto.

## Base y ramas

- Repositorio del aplicativo: https://github.com/07Ismael02/InduMotos_AS
- `develop`: base móvil previa conservada, commit `1c900da`.
- `feature/integrador-13`: adaptación de marca, navegación y contrato del Sprint 1. Es un nombre de rama de integración, no una declaración de aprobación de S13.
- Backend: https://github.com/Akatosh4019/BACK-END-INDUMOTOS, rama `feature/roberto-sprint1-backend`, commit `d4acdd0641de470b3777ac0910fb7e76b04ad610`.
- Procedencia de la base técnica: [PROCEDENCIA.md](PROCEDENCIA.md).

## Funciones del primer incremento

Inicio de sesión, identidad y rol visibles, menú lateral por permisos, consulta paginada de inventario con filtros, consulta de fichas y recepción por ID, registro de llegadas/fichas según permiso, creación de cuentas y administración de roles/excepciones. No existe autorregistro público. Los permisos efectivos provienen del backend, no de reglas inventadas en Android.

La administración puede consultar inventario pero no registra llegadas ni fichas por defecto. Almacén registra llegadas; Inventarios registra fichas. El backend toma la decisión final y puede responder 403.

## Abrir y generar APK

Abrir esta carpeta en Android Studio. Módulos: `androidApp` y `shared`. Java 21, SDK 36 y Gradle Wrapper del repositorio. No se requiere Firebase para este incremento.

```powershell
./scripts/Compilar-Android.ps1
```

APK Debug: `entrega/Indumotos-AS-Sprint1-debug.apk`, generado localmente e ignorado por Git. No es una entrega release ni un artefacto de Play Store. Identificador independiente: `pe.indumotos.asapp`; nombre visible: **Indumotos AS**. No reemplaza la app anterior.

## Backend y USB

La configuración local y sus contraseñas están fuera del repositorio: `%LOCALAPPDATA%/IndumotosAS/sprint1.env`, restringido al usuario de Windows. No publicarlo ni adjuntarlo a evidencias.

```powershell
./scripts/Iniciar-Backend.ps1
./scripts/Conectar-USB.ps1 -Serial RFGL11XYCTD
adb -s RFGL11XYCTD install -r entrega/Indumotos-AS-Sprint1-debug.apk
```

Backend local: `http://127.0.0.1:8089`. Dentro de la app, Configuración de conexión permite revisar el servidor. ADB Reverse mantiene ese mismo localhost en el teléfono. Emulador: `http://10.0.2.2:8089`. Fuera del entorno local se exige HTTPS. No se abre el firewall ni se expone PostgreSQL.

Si Android informa conflicto de firma, conservar la instalación y sus datos; no desinstalar para ocultar el problema.

## Estado real

Compilación Debug y verificación de firma ejecutadas. Backend Sprint 1 compilado y operativo con Flyway V1–V7 en una base separada. Login, identidad, menú e inventario vacío comprobados en el Samsung físico; no se declara validado todo el recorrido de escritura ni los demás roles. No se ejecutaron suites automatizadas. iOS tiene target de origen, pero no está compilado ni firmado desde Windows.

Detalles, contrato y pendientes: [docs/INTEGRACION-SPRINT1.md](docs/INTEGRACION-SPRINT1.md).
