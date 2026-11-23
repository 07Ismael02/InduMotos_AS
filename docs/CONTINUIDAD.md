# Continuidad — 23/11/2026

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
