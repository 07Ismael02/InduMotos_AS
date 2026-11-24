# Guías offline — 24/11/2026

Corrección0.4.4/código17, rama feature/ismael-catalogo-ventas. La pantalla Llegadas se usaba solo como consulta, incluso con permisos efectivos FICHAS_REGISTRAR e INVENTARIO_CONSULTAR. Se permite ahora completar borradores desde esa entrada según ambos permisos, sin conceder capacidades por el mero nombre ADMIN. Cuentas sin permiso conservan consulta.

Sin conexión, la acción muestra Recargar datos locales; no promete descargar información del servidor. Se indica que los borradores se guardan en el teléfono y requieren login online para sincronizar. Finalizar stock, nuevas recepciones con fotos, traslados, pagos y administración de usuarios no adquieren colas offline con esta corrección. No se afirma equivalencia total entre modo offline y online ni se utiliza numeración negativa para entidades definitivas; los borradores se identifican por guía y posición.

Se conservan SQLDelight, cola existente con revisión de conflictos/If-Match, validación de permisos al sincronizar y separación por cuenta/servidor. Backend, migraciones y datos históricos sin cambios. Compilación, resultados e instalación se registran al finalizar. La petición de colas para todas las operaciones queda pendiente de diseño y validación posterior a la defensa; no se improvisa una sincronización de pagos/permisos sin control.
