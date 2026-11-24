Revisión de estructura y formato (24/11/2026): [comparación completa con PharmaMobile](docs/ESTRUCTURA-DEL-PROYECTO.md). Formato Kotlin unificado; refactorización de responsabilidades todavía pendiente. El APK publicado sigue en 0.4.4.

**Corte de defensa — 24/11/2026: 0.4.4-evolucion / código17.** [APK Android de demostración](entregables/android/Indumotos-AS-0.4.4-evolucion-debug.apk), [manifiesto y hash](docs/build-044.json), [estado de entrega y límites](docs/ENTREGA-DEFENSA-20261124.md). Build y 64 pruebas móviles aprobados; instalada con firma original y datos persistentes conservados. [Corrección de guías offline](docs/GUIAS-OFFLINE-044.md). El modo offline permite lecturas y borradores de guías existentes; no ofrece todas las operaciones online. Backend compatible: rama feature/ismael-catalogo-ventas, commit67e54f2; no sustituirlo por main sin resolver las migraciones incompatibles. Los apartados siguientes son antecedentes.

# Indumotos AS — aplicativo móvil

Estado vigente: **0.3.4-sprint2-offline / código 11**, trabajo del 23/11/2026. Rama `feature/ismael-offline-cuentas`; main permanece estable y sin este incremento. Leer primero [continuidad](docs/CONTINUIDAD.md), AGENTS.md y [uso offline](docs/OFFLINE-USO.md).

Cuentas legibles, ajustes de datos y evidencia de cuatro perfiles en Samsung: [cuentas y perfiles](docs/CUENTAS-Y-PERFILES-20261123.md). Backend del bloque actual: `feature/ismael-cuentas-presentacion`; el binario sigue en95261ad, sin cambios Java.

Login simplificado con selector offline: [versión0.3.4](docs/LOGIN-034.md). [Pendientes concretos de entrega](docs/PENDIENTES-ENTREGA.md).

## Funciones y arquitectura

Kotlin Multiplatform/Compose: `androidApp` inicia Android; `shared` separa presentation (pantallas/ViewModels), domain (modelos/contratos) y data (repositorios REST/locales), conectados mediante Koin. Pantalla → ViewModel → repositorio → Ktor/API o SQLDelight privado. Las autorizaciones definitivas las aplica el backend.

Incluye catálogo con selección marca→modelo→color y alta administrativa explícita; inventario/búsqueda/paginación; fichas y copia de chasis; recepción con fotos protegidas, borradores, OCR revisable y finalización; solicitudes multilínea, asignación/retiro, historial, anulación y traslados; usuarios y permisos efectivos. PharmaMobile es referencia y no se modifica. Web/Figma corresponden a otros colaboradores.

## Offline implementado

Descarga explícita de catálogo, inventario/fichas y guías con fotos. Consulta de lo descargado y guardado de borradores de guías previamente descargadas, separados por servidor/usuario. Cada pendiente conserva baseline y revisión; al reconectar renueva permisos y envía PUT condicional If-Match. Conflictos requieren revisión; no se reenvían automáticamente.

Reapertura local protegida por credencial nativa Android, máximo 24 horas desde login online. No restaura sesión del servidor ni guarda JWT/contraseñas. Se pueden conservar varios perfiles independientes. Logout retira la reapertura de la cuenta activa y conserva borradores separados para el próximo login de la misma cuenta. No se cachean credenciales ni administración de personal. Datos locales son la última lectura, no stock actual confirmado.

Sin conexión no se crean llegadas/fotos, ejecuta OCR, finaliza stock, asignan unidades, trasladan ni administran accesos. Esas operaciones requieren servidor. Sincronización explícita y periódica mientras la app está activa; no se promete ejecución con la app cerrada. [Procedimiento completo](docs/OFFLINE-USO.md).

## Entorno actual

Backend correcto: [BACK-END-INDUMOTOS](https://github.com/Akatosh4019/BACK-END-INDUMOTOS), rama `feature/sprint2-offline-borradores`, código `95261ad`. Añade versión/If-Match sin migraciones nuevas y mantiene la web sin cabecera. Referencias contrastadas: web main `f2981a3`, backend main `5b37b9b`.

Docker: PostgreSQL existente y API `indumotos-paridad-api`, imagen `indumotos-paridad-ocr:95261ad`, API solo `127.0.0.1:8090`. Copia aislada `indumotos_s2_paridad_20261123` V23; base histórica V15 y volúmenes conservados. No ejecutar down -v ni migraciones desde cero. Contenedor anterior conservado detenido como `indumotos-paridad-api-pre-offline`, con reinicio automático desactivado.

```powershell
./scripts/Iniciar-Entorno-Paridad.ps1 -Serial RFGL11XYCTD
./scripts/Conectar-USB.ps1 -Serial RFGL11XYCTD
```

APK incorpora `http://127.0.0.1:8089`; túnel teléfono8089→PC8090. Restablecerlo tras reiniciar ADB/USB. Offline funciona sin cable para datos ya descargados; sincronizar actualmente requiere este túnel. No se contrató servidor público ni se expuso PostgreSQL. No afirmar conectividad Wi-Fi/HTTPS desplegada.

## Build, instalación y evidencia

Java21 y wrappers existentes. Configuración para ahorrar RAM: Gradle1536MB, un worker, Kotlin in-process; Android Studio puede permanecer cerrado.

```powershell
./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain
```

APK estable local ignorado por Git: `entrega/Indumotos-AS-0.3.4-sprint2-offline-debug.apk`. AppID `pe.indumotos.asapp`, misma firma Debug e instalación -r sin borrar datos. Código del APK `adff08e`; hash/certificado/ruta exactos en [manifiesto](docs/build-login-20261123.json). No es release de producción ni AAB/iOS certificado.

Verificación: **53 pruebas móviles JVM/SQLite/HTTP aprobadas en0.3.3**; ajuste visual0.3.4 compilado y tres comprobaciones Samsung aprobadas, sin repetir esa suite, 2 unitarias backend y 14 comprobaciones HTTP reales de control de concurrencia. Las 110 comprobaciones API anteriores pertenecen al bloque previo. No son 51 recorridos físicos. Resultado Samsung y evidencias: [continuidad offline](docs/OFFLINE-CONTINUIDAD.md). Los hitos históricos no sustituyen la validación del APK actual.

## Pendientes y entrega

Completar los recorridos físicos integrales de Sprint2 por rol, tamaños/temas restantes, aceptación de negocio HU-27, firma release y validación iOS en macOS. No declarar Sprint2 terminado por la compilación o las pruebas API. No merge/main/tag final sin revisión y autorización.

Excel: únicamente pestaña existente SCRUM S2 19-11-2026, fecha real, sin enlaces ni alterar formato. Este bloque no acredita su actualización. Nunca publicar claves, archivos privados, JWT, contraseñas o documentos personales. [Historia de paridad](docs/CIERRE-PARIDAD-20261123.md).
