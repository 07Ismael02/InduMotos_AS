# Pendientes para cierre Android — 23/11/2026

No confundir módulos implementados con recorridos físicos completos. Estado base: cuentas/perfiles0.3.3 y ajuste visual0.3.4. No hay un nuevo módulo de negocio identificado como ausente en este último bloque; quedan mejoras y validaciones concretas.

| Prioridad | Trabajo | Estado real |
|---|---|---|
| 1 | Ordenar login y ocultar la lista offline tras un selector | Implementado en0.3.4; registrar instalación y capturas en LOGIN-034.md |
| 2 | Revisar diseño consistente del resto de pantallas | Pendiente revisión de formularios largos, jerarquía de botones, teclado, horizontal, texto ampliado y tema claro; no afirmar que todo está adaptado |
| 3 | Recorrido físico completo por roles | Cerrar recepción con selector Android, revisión OCR, borradores/finalización, edición y logística; parte de API y pantallas acreditada, recorrido integral del Samsung aún no |
| 4 | Conflicto offline en dos clientes | Cubierto por pruebas automáticas/HTTP; demostración completa en Android pendiente. Consulta y conservación/sincronización de borrador ya observadas |
| 5 | Acceso online sin depender del cable | No desplegado. Offline funciona sin servidor para lo descargado; sincronizar/operar online requiere actualmente USB y backend del PC. Una URL HTTPS accesible es un trabajo de despliegue distinto |
| 6 | Preparar entrega exacta | Firma release, artefacto final, evidencia/matriz y Excel con resultados reales; no confundir APK Debug con release |
| 7 | Aceptación de reglas e integración | HU-27 pendiente de aceptación; revisión final antes de merge/tag. iOS requiere macOS/Xcode y no está validado |

Solo cuentas previamente autenticadas, con datos propios descargados y perfil vigente, se ofrecen sin conexión. No se debe mostrar toda la lista del servidor ni copiar datos entre cuentas para llenar el selector. Cerrar sesión revoca el perfil de esa cuenta; volver a habilitarlo exige login online.
