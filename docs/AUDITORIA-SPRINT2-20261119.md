# Corte verificable del Sprint 2 — 19/11/2026, Lima

## Fuentes y método

Se inspeccionaron las seis ramas publicadas del aplicativo y sus relaciones de historial. No se cambiaron ramas del equipo, código de aplicación, esquemas ni datos. Las verificaciones descritas en commits anteriores son antecedentes documentados, no pruebas ejecutadas de nuevo en esta auditoría.

Libro actualizado: [Sprint2_bitacoras_trabajo_completada.xlsm](https://drive.google.com/file/d/1jRkQPhNMqwj6kwnerQ0GqXnOAAqBi2wf/view). Respaldo de la última edición recibida: [copia previa](https://drive.google.com/file/d/1jYH000NKI-DCWAKuQ1BJuJ_oz7n2a-Bq/view). Se incorporó una edición concurrente de fechas/horas antes de reemplazar el archivo; sus valores se conservaron.

Las hojas históricas del 08 al 14/11 siguen en el libro, identificadas como plan/simulación no acreditada como ejecución. No se backdatearon commits ni se inventaron reuniones, tiempos o pruebas. Las nuevas hojas son CORTE REAL 19-11-2026, RAMAS AUDITADAS y REGISTRO REAL. Las 50 tareas tienen estado actual, evidencia o limitación y siguiente acción. Se mantienen los dibujos y gráficos originales.

## Ramas móviles inspeccionadas

| Rama | HEAD observado | Estado |
|---|---|---|
| main | 3d9334a | Base inicial + continuidad; no incorpora login nuevo ni HU-05 |
| develop | 1c900da | Base antigua |
| docs/continuidad-sprint2 | 3d9334a | Documentación del primer relevo |
| feature/integrador-13 | 8fd7618 | Primer APK y evidencias Samsung del 17/11 |
| feature/login-identidad | b645455 | Login/tema/configuración por entorno y Mi cuenta |
| codex/feature/sprint2-ubicacion | df9b61c | Más avanzada: lo anterior + consulta/registro inicial de ubicación Android |

Los últimos commits inspeccionados son reales del 19/11. La base más reciente para continuar funcionalidad es codex/feature/sprint2-ubicacion; no partir del develop antiguo ni sustituir los avances ajenos. Esta rama documental se crea desde df9b61c y no integra funciones en main.

## Backend y evidencia

El backend correcto es Akatosh4019/BACK-END-INDUMOTOS. La rama sprint-2 está en fbfaab5e6c50b36efb4809c9d212273cef7718ed y contiene V8–V15, controladores de ubicación, solicitudes, seguimiento, asignaciones y anulación. También existen las ramas de Sprint 1 y una rama de entorno local; no confundir sus despliegues.

`docs/SPRINT 2/README.md` del backend informa 63 pruebas sin fallos y arranque HTTP 200/401 con migraciones; también declara pendientes empaquetado completo, gateway, escritura HTTP integral y capturas. El informe de Angular menciona seis pruebas, pero el repositorio web no fue objeto de auditoría directa en este pedido: no se certifica su entrega completa.

La continuidad móvil documenta registro HU-05 por HTTP y desde Android con movimiento ID 2, además de corrección 61d3694 para ofrecer solo ubicación inicial. La observación visual de la ficha del APK corregido todavía figuraba pendiente. Un registro de ubicación inicial no equivale al traslado HU-09.

## Resultado de la matriz de 50 tareas

To Do: 17. Doing: 32. Done: 1 (HU-05-T04, API con registro/lectura documentados). La clasificación es estricta según el resultado completo de cada tarea: Doing incluye implementación parcial o criterios/evidencias pendientes, no significa que todo ese código falte.

La presencia de migraciones no acredita por sí sola ER actualizado y conservación del conjunto anterior. Código de seguridad y pruebas de unidad no acreditan el recorrido HTTP completo. Un enlace genérico a Figma no acredita los prototipos de cada HU, y no se encontraron modelos UML del Sprint 2 en los repositorios inspeccionados. Por eso no se trasladan los Done previstos del calendario a una declaración de entrega aprobada.

## Continuación priorizada

1. Terminar la observación del ajuste HU-05 y conservar su registro ya probado.
2. Android HU-06: solicitud con varias líneas modelo/color/cantidad y destino; consumir el controlador real de sprint-2 y consultar el identificador devuelto.
3. Android HU-07/HU-08: seguimiento y asignación/retirada de chasis; respetar transición, capacidad y exclusividad del backend.
4. Android HU-09: traslado a TIENDA/DESPACHO activos, distinto del alta de ubicación inicial.
5. Android HU-27: confirmación y motivo; el backend actual admite solo PENDIENTE. Mantener pendiente la aprobación de la regla por Product Owner.
6. Recorrido transversal, permisos, regresión, evidencia y empaquetado/gateway. No declarar Sprint cerrado antes de comprobar los criterios.

Durante esta revisión no se modificó ni se probó código nuevo del aplicativo. Las siguientes implementaciones requieren una feature y verificaciones pertinentes; el Excel no debe adelantarse al resultado real.
