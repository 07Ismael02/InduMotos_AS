# Estructura del aplicativo

Revisión del 24/11/2026. Referencia leída: PharmaMobile, en AndroidStudioProjects. Comparación del repositorio móvil completo: fuentes Android, comunes e iOS, pruebas, recursos, Gradle, documentación y scripts. No es una nueva validación funcional ni una auditoría del backend.

## Comparación con PharmaMobile

| Aspecto | PharmaMobile | Indumotos | Evaluación |
|---|---|---|---|
| Módulos Gradle | androidApp y shared | androidApp y shared | Se mantiene la misma base multiplataforma. |
| Inicio Android | Activity, Application y puente nativo | MainActivity, MainApplication y CameraPhoto | La cámara y el desbloqueo pertenecen a Android; no al backend. |
| Código común | commonMain | commonMain | Comparte reglas, pantallas y acceso a datos. |
| Presentación | domain/presentation, separada por función | presentation para ViewModels; pantallas en el paquete raíz | Separación incompleta en Indumotos: falta agrupar las pantallas por función y aligerar App.kt. |
| Dominio | model, repository, usecase, auth, permisos | domain, con modelos, contratos y reglas por archivo | Existe la capa. No importa Android, Compose, Ktor ni implementaciones de data. Sus contratos todavía usan JSON en algunas operaciones. |
| Datos | remote, dto, mapper, local, repository, auth | data, con repositorios remotos, almacenamiento, sesión y sincronizador | Las responsabilidades están identificadas, pero su distribución es más plana y RemoteRepository reúne varios contratos. |
| Inyección | di y módulos de plataforma | di y PlatformModule por plataforma | Koin conecta contratos con implementaciones. |
| Tema y navegación | theme y navigation | archivos del paquete raíz y decisiones en App.kt/pantallas | Falta una separación equivalente por responsabilidad. No basta con mover nombres de carpetas. |
| Base local | SQLDelight y archivos .sq | SQLDelight, Offline.sq | Fuentes locales separadas del código generado; no editar build. |
| Plataformas y pruebas | androidMain, iosMain y conjuntos de prueba | androidMain, iosMain, commonTest y androidHostTest | Son conjuntos necesarios de Kotlin Multiplatform, no carpetas sobrantes. |
| Formato Kotlin | KOTLIN_OFFICIAL en el estilo del proyecto | Aplicado el mismo esquema a las 75 fuentes Kotlin | Se añade .editorconfig para mantenerlo al editar. |

En la revisión había 221 archivos versionados en Indumotos y 344 en PharmaMobile; 75 y 122 fuentes Kotlin, respectivamente. commonMain tenía cinco paquetes en Indumotos y 29 en PharmaMobile. Por tanto, el problema principal no es un exceso de paquetes propios: es la concentración de responsabilidades y el código comprimido. Estas cifras describen este corte, no un límite de calidad.

## Qué carpetas se mantienen y por qué

| Carpeta | Contenido y función |
|---|---|
| androidApp/src/main | Arranque Android, manifiesto, integración con cámara, desbloqueo y recursos del sistema. |
| shared/src/commonMain/kotlin | Código compartido del aplicativo. App.kt coordina sesión y navegación; presentation contiene ViewModels; domain define modelos, contratos y reglas; data implementa REST, almacenamiento y sincronización; di configura Koin. |
| shared/src/commonMain/sqldelight | Esquema y consultas que generan OfflineDb. |
| shared/src/commonMain/composeResources | Recursos compartidos de Compose. |
| shared/src/androidMain | Driver SQLDelight, motor HTTP y adaptadores Android. |
| shared/src/iosMain | Adaptadores equivalentes de iOS. Su presencia no acredita validación en iPhone. |
| shared/src/commonTest | Pruebas de reglas y flujos compartidos. |
| shared/src/androidHostTest | Pruebas JVM con SQLite, HTTP y sincronización. |
| iosApp | Proyecto contenedor iOS del mismo aplicativo. |
| gradle | Wrapper y catálogo de versiones. |
| scripts | Ayudas de arranque y conexión del entorno de presentación. |
| docs | Historial y evidencia; leer primero CONTINUIDAD, no asumir que un informe antiguo describe el entorno actual. |
| entregables/android | APK de la entrega publicada. |
| entrega | Copias locales ignoradas por Git; no forma parte del código fuente. |
| build, .gradle, .kotlin, .idea | Compilación, cachés y configuración local del IDE. Están ignoradas; no representan capas del aplicativo. |

No se encontraron fuentes duplicadas por contenido. La única duplicación exacta en src es la imagen de marca usada como recurso Android y como recurso compartido; ambos sistemas de recursos tienen destinos distintos.

## Flujo que debe poder explicarse

MainApplication configura Koin. MainActivity abre App y aporta las capacidades nativas. La pantalla envía una intención al ViewModel; este aplica reglas y utiliza un contrato del dominio. Koin entrega el repositorio correspondiente. El repositorio consulta la API mediante Ktor o los datos locales mediante SQLDelight. El estado vuelve al ViewModel y Compose actualiza la pantalla.

La API verifica sesión, permisos e integridad y persiste en PostgreSQL. La pantalla no accede directamente a esa base. El desbloqueo Android protege la reapertura de datos descargados; no crea una sesión del servidor. Los borradores offline y sus conflictos se gestionan en OfflineGuideRepository y DraftSynchronizer.

## Corrección realizada y trabajo pendiente

Se reformatearon las 75 fuentes Kotlin con el esquema real del proyecto de referencia: sangría, espacios, parámetros y separación de sentencias. Se conservaron paquetes, identificadores, literales, contratos, recursos y funcionalidades. También se conserva la corrección previa de espacios entre literales y else en App.kt. PharmaMobile no se modificó.

La revisión identifica estos cambios estructurales pendientes, en orden:

1. Extraer de App.kt login, inicio, inventario y componentes de formulario; mantener en App únicamente la composición y coordinación general.
2. Reunir pantallas y ViewModels por función dentro de presentación; separar tema y navegación. Revisar también referencias y adaptadores de plataforma al cambiar paquetes.
3. Separar los contratos remotos agrupados en RemoteRepository y reemplazar gradualmente JSON genérico expuesto a presentación por modelos y mapeos explícitos.
4. Dividir domain en subpaquetes cuando facilite encontrar modelos, contratos y casos de uso; no crear capas vacías ni añadir clases que solo repitan llamadas.
5. Consolidar el acceso a documentación vigente: existen 103 archivos en docs, muchos históricos. Preservar las evidencias y señalar antecedentes en vez de borrarlos.

Este bloque no presenta esa refactorización como terminada. Mover archivos con cambios de paquetes requiere compilación y comprobación de los flujos afectados; debe quedar separado del cambio de formato para que el historial sea revisable. Tampoco acredita nuevas pruebas físicas, sincronización completa ni cierre integral de Sprint 2.

## Verificación del formato

Se respaldaron las 79 fuentes Kotlin y scripts Kotlin de Gradle antes del formateo. El lexer Kotlin instalado se utiliza para comparar los tokens, incluidos textos y comentarios, ignorando únicamente espacios de formato y normalizando finales de línea. No se genera otro APK por este cambio; el binario publicado continúa siendo 0.4.4. Las 64 pruebas documentadas pertenecen a su compilación anterior y no se presentan como una nueva ejecución.
