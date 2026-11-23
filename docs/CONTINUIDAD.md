# Continuidad — 23/11/2026

Repositorio móvil: 07Ismael02/InduMotos_AS. Directorio de trabajo: C:/Users/Ismael Logacho/Documents/PharmaMobil/proyecto-final-dam/InduMotos-Sprint2. Rama: feature/sprint2-paridad-web, desde 42445cb. Main no se modifica.

Se contrastaron main web f2981a3 y main backend 5b37b9b. Las copias Referencia-Web-Sprint2 y Referencia-Backend-Sprint2 son fuentes de lectura, no reemplazan los repositorios del equipo. El backend correcto sigue siendo Akatosh4019/BACK-END-INDUMOTOS; no backend-DAM-final.

Commits de implementación: d9cedb2 (catálogo/solicitudes), 2f06eff (fotos, borradores, OCR, finalización y edición). Seguidos por 0db7fbd (revisión OCR/recarga) y 58972fe (navegación sin consulta antigua). APK 0.2.1-sprint2, código 3, firmado con la misma clave Debug; 23 pruebas aprobadas. Hash y artefacto estable en build-paridad-20261123.json; no inferir que el servidor o el teléfono se probaron por haber compilado.

El backend nuevo usa V23; el entorno previo usaba indumotos_s2_verificacion en V15. No aplicar cambios sobre esa base de manera implícita. Preparar una copia aislada y revisar respaldo antes de la integración física. No se han creado usuarios ni cambiado contraseñas/permisos en este bloque.

Arquitectura y funciones: ver README y PARIDAD-WEB-SPRINT2.md. Domain contiene contratos/modelos; data contiene GuideRemoteRepository y RemoteRepository; presentation contiene GuidesViewModel/CatalogViewModel/OperationsViewModel; las pantallas nuevas son archivos separados. Koin proporciona las interfaces. Android usa selector de documentos y adapta imágenes; no solicita lectura general del almacenamiento.

Siguiente validación: matriz PRUEBAS-PARIDAD-SPRINT2.md con backend actualizado y cuentas operativas autorizadas. Conservar datos y clave, verificar apksigner antes de instalar. Registrar evidencias reales del nuevo build, no reutilizar capturas antiguas como nuevas. Web y Figma pertenecen a otros colaboradores; la aceptación de la regla HU-27 permanece pendiente.

Los resultados históricos del 19/11 y el Excel no certifican este incremento. Solo actualizar SCRUM S2 19-11-2026 con fecha real de actividad, sin enlaces ni cambios de formato. No agregar pestañas.
