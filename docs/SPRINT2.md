> **Actualización 23/11/2026:** rama feature/sprint2-paridad-web basada en la implementación existente, contrastada con web/main f2981a3 y backend/main 5b37b9b. Ver PARIDAD-WEB-SPRINT2.md y PRUEBAS-PARIDAD-SPRINT2.md. Los pendientes y entornos descritos a continuación son antecedentes del 19/11, no el estado vigente. No se declara cierre integral.

> **Corte vigente 19/11/2026:** solicitudes, unidades, seguimiento, anulación y traslados implementados en `feature/sprint2-recorrido`; 14 pruebas móviles y 58 comprobaciones HTTP/persistencia aprobadas. Validación visual nueva en Samsung pendiente. Backend real `sprint-2` (`fbfaab5`), copia de datos aislada, puerto 8090. [Estado y evidencia actuales](VALIDACION-SPRINT2-20261119.md). Las referencias de entorno anteriores que siguen abajo son antecedentes y no describen el servidor actual.

# Sprint 2 — plan de ejecución para el 19/11/2026

**Estado: pendiente de implementación/integración y comprobación.** La solicitud del usuario fija prioridad de entrega para hoy; este documento no afirma Sprint Planning, Daily, pruebas, Review ni cierre celebrados.

## Revisión de fuentes del 19/11/2026 en esta PC

Se encontró y extrajo el [plan del Sprint 2 del 08 al 12 de noviembre](https://drive.google.com/file/d/1cuTASr-QSXTDw6eHbrCDHctd_iIsBrTf/view), modificado el 12/11 a las 19:09 (Lima). Es posterior al libro de octubre citado abajo. Ver [revisión y matriz de trazabilidad](REVISION-2026-11-19.md).

**Corrección de HU-27:** la hoja `G4 - FAMILIA VALENCIA`, fila 82, ya registra 4 puntos. Las condiciones exactas de anulación siguen pendientes en la bitácora del 12/11. Los estados del avance también siguen sujetos a validación. Las seis tareas Android T07 y pruebas T08 figuran en el plan; los prototipos registrados no acreditan una integración implementada. El contrato remoto actual no pudo contrastarse: acceso al backend bloqueado en esta PC.

## Fuentes y alcance anteriores (antecedente)

Fuente más reciente consultada: [Ing SW1 G2, épicas/HU, revisión 29/10](https://drive.google.com/file/d/1QS8WveG1IuCraO6FeTW5GrLiUMklg81O/view), bloque «SPRINT 2 · Solicitudes, ensamblaje y movimientos». Ese libro distribuye 27 HU en cinco sprints propuestos; no reemplazarlo por el escenario antiguo de tres iteraciones sin explicar la diferencia. [Plan anterior de tres iteraciones](https://drive.google.com/file/d/1qVrCCCsXBtTuXYttMXf31suUVWzUwrKv/view) es antecedente, no el único alcance vigente.

| HU | Función del Sprint 2 | Qué debe poder demostrarse | Estado al entregar contexto |
|---|---|---|---|
| HU-05 | Registrar ubicación conocida de una motocicleta | Seleccionar una unidad real y ubicación válida; consultar después el dato actualizado y el responsable/evento acordado | Pendiente; API no presente en base Sprint 1 inspeccionada |
| HU-06 | Crear solicitud comercial | Registrar modelos, colores y cantidades; validación; devolver identificador y estado reales | Pendiente |
| HU-07 | Registrar avance y resultado de solicitud | Consultar y aplicar transiciones acordadas con motivo/actor; rechazar acciones inválidas o sin permiso | Pendiente; no inventar máquina de estados aprobada |
| HU-08 | Asociar motocicletas concretas al ensamblaje | Vincular chasis existentes a una solicitud válida y comprobar la asociación persistida | Pendiente |
| HU-09 | Registrar traslado a tienda o despacho | Registrar origen/destino y unidad identificada; confirmar ubicación e historial acordado sin perder el registro anterior | Pendiente |
| HU-27 | Anular solicitud enviada por error o innecesaria | Conservar solicitud, motivo y autorización; impedir anulación fuera de la regla acordada | 4 puntos en el libro de noviembre; regla si el trabajo empezó y contrato siguen pendientes |

## Decisiones operativas que no se deben inventar

Permisos por cargo para nuevas operaciones; estados y transiciones; significado de ubicación registrable; cuándo un traslado se considera confirmado; regla de anulación si la preparación empezó; condiciones para asociar chasis repetidos o una unidad a más de una solicitud. Revisar contratos y material del equipo antes de consultar al usuario. Si falta una regla imprescindible, identificarla y continuar con tareas independientes; no afirmar que quedó aprobada.

## Orden mínimo para producir un incremento real

1. **Base segura y coordinación:** fetch de ambos repos, verificar cambios del equipo y crear feature/sprint-2-integracion desde la main actual del móvil. Crear una feature propia en backend desde la rama acordada, preservando la de Roberto. No incorporar el viejo backend-DAM-final ni sus migraciones por copia ciega.
2. **Cerrar bloqueos del Sprint 1:** identidad/login/conexión ya pedidos, sin rediseño arquitectónico ni autorregistro. Validar que la sesión y los permisos existentes siguen funcionando; registrar claramente los flujos todavía no comprobados.
3. **Contrato antes de pantallas:** comparar API real con las seis HU y escribir rutas, JSON, roles, errores 400/401/403/404/409 y transacciones. Las rutas nuevas propuestas no son endpoints disponibles hasta implementarlas y verificarlas.
4. **Persistencia y servicio:** inspeccionar esquema actual y Flyway. Si hace falta evolucionar el modelo, añadir migraciones nuevas, revisables y compatibles; jamás reescribir V1–V7 ni recrear volúmenes. Conservar unidades e identificadores y respaldar datos existentes antes de una operación que pueda afectarlos.
5. **Recorrido vertical:** ubicaciones → solicitud comercial → consulta/atención → unidades asociadas → traslado. Asegurar controles en backend y reflejarlos en el menú Android; la ocultación de un botón no sustituye 403.
6. **Anulación:** implementar HU-27 con la regla aclarada; sin borrado de solicitud/historial. Si la decisión sigue ausente, dejar el caso preciso pendiente y no bloquear el resto fingiendo una solución.
7. **APK e integración:** compilar solo tras cambios relevantes, verificar firma/applicationId, instalar sin borrar datos y comprobar el recorrido con cuentas autorizadas. Usar USB/ADB Reverse en la universidad; no abrir firewall ni PostgreSQL a la red.
8. **Evidencias y trazabilidad:** documentación actualizada, commits coherentes, matriz por HU con implementado/comprobado/pendiente, resultados reales del servidor/base y capturas auténticas. Integrar a main solo el incremento revisado y comprobado, con autorización para la integración.

## Lista de salida para afirmar «Sprint 2 funcional»

- Cada HU comprometida tiene contrato, implementación y verificación identificable; HU-27 no se marca cerrada con una regla de anulación desconocida.
- Login y consulta/registro del Sprint 1 no sufrieron regresiones en lo comprobado; se registran limitaciones de lo no probado.
- Los datos creados y modificados se consultan desde backend/base y desde el aplicativo; no respuestas simuladas ni solo pantallas estáticas.
- Validación y rechazo por permisos comprobados en las operaciones nuevas; datos y secretos protegidos.
- APK exacto identificado por versión, commit, hash y certificado; ningún conflicto de firma se resuelve borrando datos sin autorización.
- README/bitácora actualizados y pendientes explícitos. Una fecha límite no convierte una tarea sin ejecutar en prueba aprobada.

## Fuera de este corte

Ventas, facturación completa, OCR, postventa, notificaciones, sincronización offline general, firma iOS y funciones de sprints posteriores, salvo nueva instrucción explícita y análisis de impacto. No extender el Sprint 2 para resolver todo el backlog.
