# Corte 24/11/2026 — perfil y navegación

Último resultado: consulta offline desbloqueada por el responsable, sesión cerrada y misma cuenta administrativa visible nuevamente en el selector. Defecto de desaparición corregido y comprobado en Samsung. Captura de Ajustes final revisada. Ver PERFIL-Y-NAVEGACION.md y build-041.json. La espera de desbloqueo descrita debajo ya se resolvió para esta comprobación; segundo desbloqueo de reentrada no ejecutado aún. No presentar como finalizadas las prácticas anteriores de fotos, pagos y reconexión.

Actualización del corte: ADB recuperado, 0.4.1 instalada (firma compatible, install-r Success). Build final aprobado; 59 pruebas móviles sin fallos/errores. Ver resultados físicos y límites en PERFIL-Y-NAVEGACION.md. Barra de conexión compactada, contraste oscuro ajustado; todas las acciones están en «Ver estado». Falta que el responsable complete el desbloqueo Android para comprobar el ciclo físico de cierre y reentrada offline. No pedir ni registrar su PIN. El párrafo siguiente describe el impedimento anterior, ya resuelto.

Rama feature/ismael-catalogo-ventas. Leer PERFIL-Y-NAVEGACION.md y EVOLUCION-ACTIVA.md. Correcciones de permanencia del perfil offline, pantallas de ajustes/perfil y Atrás por niveles. Suite móvil: 59 pruebas, cero fallos/errores. Versión0.4.1, código14; comprobar final del build en tmp/sprint2-auditoria/perfil-041-build.log. Samsung antes en192.168.137.64:45165 dejó de aceptar ADB: se solicitó nueva dirección principal, sin nuevo emparejamiento. No declarar instalada ni revisada físicamente esta versión hasta comprobarlo. Backend de este entorno: indumotos-evolucion-api, HTTPS192.168.137.1:8443, copiaDBV34; originalV23 conservado. No cambios backend para este bloque. Main intacto.

## Antecedentes

# Estado anterior — 0.3.5

Leer DEMOSTRACION.md. Appf9c24db, backend0e84511; 53 pruebas móviles,8 backend y14 HTTP aprobadas. Recuperación mediante administrador, tokens anteriores invalidados y navegación de Traslados corregida. APK instalado sin borrado. Inventario: corrección del salto de carga, revisión perceptiva pendiente. Solo iniciar DB y API actuales; detalles/puertos/script en DEMOSTRACION.md. Main intacto.

## Antecedentes

# Estado vigente — login0.3.4

Código adff08e, rama feature/ismael-offline-cuentas. Leer LOGIN-034.md y PENDIENTES-ENTREGA.md. Login limpio con botón Sin conexión y selector de cuentas; tres comprobaciones Samsung y build aprobados, instalación compatible sin borrado. Suite53 anterior conservada, no repetida. Backend/BD no cambiaron en este bloque. Main intacto.

## Antecedentes

# Estado vigente — cuentas y perfiles0.3.3

Leer CUENTAS-Y-PERFILES-20261123.md. App feature/ismael-offline-cuentas (código14618b2); backend feature/ismael-cuentas-presentacion (sin cambios Java). 53 pruebas aprobadas y seis logins HTTP200. Samsung conserva cuatro perfiles descargados tras reapertura. Credenciales académicas configuradas localmente, sin publicarlas. Catálogo/sedes/guías sintéticos renombrados con respaldo e IDs conservados. Facturación habilitada solo para consulta por aprobación explícita: dos lecturas200 y cinco escrituras403 verificadas. Main intacto.

## Antecedentes

# Estado vigente — offline 0.3.2

Leer OFFLINE-CONTINUIDAD.md y OFFLINE-USO.md. APK código92f3bce, backend95261ad; 51 pruebas móviles,2 backend,14 HTTP aprobadas. Samsung conservó el borrador después de actualización/reapertura y lo sincronizó: API confirma mismo motor, un borrador y stock0. Evidencias y manifiesto en docs. Main intacto. Falta validación integral Sprint2/release/iOS; no confundir las notas históricas siguientes con estado vigente.

## Antecedentes cronológicos

# Continuidad — 23/11/2026

BLOQUE ACTIVO OFFLINE: ver OFFLINE-CONTINUIDAD.md. Usuario autorizó continuar con consulta local y conservación/sincronización segura de borradores, guardando avances por reinicios/RAM. Base be14bcf; arquitectura y alcance registrados antes de implementar. No confundir las pruebas anteriores con validación offline.

ESTADO MÁS RECIENTE: leer CIERRE-PARIDAD-20261123.md. Implementados usuarios/listado/búsqueda/paginación, acceso por nombre, permisos dinámicos, fichas vinculadas y datos de guía en ficha, búsqueda automática/páginas de inventario. Código 824be6c + f24119d. APK 0.2.4/código 6. API completa: 110 comprobaciones aprobadas; Docker API con Tesseract español, puerto loopback8090, copia V23. QA de roles aisladas y secretos privados. Samsung verificó cuentas/accesos/guías/ficha/copia/logout; recepción en teléfono quedó sin envío acreditado al fallar ADB y solicitar el usuario publicar ya. APK/manifiesto final build-cierre-20261123.json. No declarar los ocho recorridos físicos aprobados ni iOS. Las notas anteriores de faltantes y Tesseract ausente quedan superadas por este estado.

Cotejo solicitado por el usuario después de 0.2.3: PARIDAD-WEB-SPRINT2.md ahora diferencia implementado, faltante y no verificado. Faltantes reales: listado/búsqueda/paginación de usuarios; selección de cuenta por nombre en accesos en lugar de ID; catálogo de permisos de UI incompleto (solo seis antiguos); fichas vinculadas en detalle de llegada ausentes en nueva pantalla. No declarar paridad completa. Priorizar esos cambios antes de pruebas integrales por rol. Referencias remotas main comprobadas sin cambios (web f2981a3/backend 5b37b9b). Este bloque solo inspecciona/documenta, no modifica aplicación/BD ni recompila.

Validación física posterior 18:32 Lima: usuario ingresó con ADMIN. Nueve comprobaciones 0.2.3 de selectores y Atrás aprobadas; capturas revisadas e informe en docs/evidencias/paridad-20261123. Se comprobó regreso a Inicio, cierre del menú tras animación, aviso y salida por doble pulsación. App reabierta en login. Sin guardar combinaciones. Horizontal/texto ampliado/confirmaciones de descarte y recorridos por otros roles pendientes. Ver REVISION-UX-PARIDAD-20261123.md para alcance e intento prematuro de cierre del menú.

Incremento posterior dcf0a10, 0.2.3/código 5: Catálogo adaptable con desplegables Material/formulario plegable, Atrás→Inicio y doble pulsación para salir; protección al descartar cambios de guía/catálogo. Compilación final BUILD SUCCESSFUL, firma compatible verificada e instalación -r Success en Samsung. Hash y ruta en build-responsive-20261123.json. Ver REVISION-UX-PARIDAD-20261123.md. Usuario confirmó selectores 0.2.2; diseño/gestos 0.2.3 aún pendientes de login manual. Salud HTTP 200 y cuatro rechazos sin token HTTP 401 comprobados en verificacion-http-20261123.json. Sin escrituras de datos por esas comprobaciones. Solo ADMIN existente: validación funcional por rol sigue pendiente, igual OCR sin Tesseract.

Corrección posterior solicitada por el usuario: confirmó que Catálogo muestra marcas/modelos/colores desde el Samsung. Eso acredita la consulta de catálogo desde la app, sin cerrar los demás recorridos. El formulario administrativo ahora ofrece selección existente y «Añadir» para escribir un valor nuevo, con dependencia marca→modelo→color y limpieza de selecciones descendientes. Código 70ac04c, versión 0.2.2/código 4. Compilación aprobada y apksigner verificado con la misma firma; install -r Success y versión 0.2.2 comprobada en Samsung. Hash/ruta en build-catalogo-20261123.json. Sin cambios backend/BD. Validación manual de estos selectores pendiente; no se repitieron suites anteriores por esta corrección de interfaz.

Actualización de entorno: ver ENTORNO-PARIDAD-20261123.md. Respaldo V15 restaurado y copia aislada migrada V23; original permanece V15. Backend HTTP 200 en 127.0.0.1:8090. Samsung autorizado, APK 0.2.1 actualizado con firma compatible y túnel 8089→8090. Login manual pendiente; solo cuenta ADMIN existente, OCR sin Tesseract disponible. Esto actualiza la preparación descrita más abajo, sin acreditar los recorridos funcionales.

Repositorio móvil: 07Ismael02/InduMotos_AS. Directorio de trabajo: C:/Users/Ismael Logacho/Documents/PharmaMobil/proyecto-final-dam/InduMotos-Sprint2. Rama: feature/sprint2-paridad-web, desde 42445cb. Main no se modifica.

Se contrastaron main web f2981a3 y main backend 5b37b9b. Las copias Referencia-Web-Sprint2 y Referencia-Backend-Sprint2 son fuentes de lectura, no reemplazan los repositorios del equipo. El backend correcto sigue siendo Akatosh4019/BACK-END-INDUMOTOS; no backend-DAM-final.

Commits de implementación: d9cedb2 (catálogo/solicitudes), 2f06eff (fotos, borradores, OCR, finalización y edición). Seguidos por 0db7fbd (revisión OCR/recarga) y 58972fe (navegación sin consulta antigua). APK 0.2.1-sprint2, código 3, firmado con la misma clave Debug; 23 pruebas aprobadas. Hash y artefacto estable en build-paridad-20261123.json; no inferir que el servidor o el teléfono se probaron por haber compilado.

El backend nuevo usa V23; el entorno previo usaba indumotos_s2_verificacion en V15. No aplicar cambios sobre esa base de manera implícita. Preparar una copia aislada y revisar respaldo antes de la integración física. No se han creado usuarios ni cambiado contraseñas/permisos en este bloque.

Arquitectura y funciones: ver README y PARIDAD-WEB-SPRINT2.md. Domain contiene contratos/modelos; data contiene GuideRemoteRepository y RemoteRepository; presentation contiene GuidesViewModel/CatalogViewModel/OperationsViewModel; las pantallas nuevas son archivos separados. Koin proporciona las interfaces. Android usa selector de documentos y adapta imágenes; no solicita lectura general del almacenamiento.

Siguiente validación: matriz PRUEBAS-PARIDAD-SPRINT2.md con backend actualizado y cuentas operativas autorizadas. Conservar datos y clave, verificar apksigner antes de instalar. Registrar evidencias reales del nuevo build, no reutilizar capturas antiguas como nuevas. Web y Figma pertenecen a otros colaboradores; la aceptación de la regla HU-27 permanece pendiente.

Los resultados históricos del 19/11 y el Excel no certifican este incremento. Solo actualizar SCRUM S2 19-11-2026 con fecha real de actividad, sin enlaces ni cambios de formato. No agregar pestañas.
