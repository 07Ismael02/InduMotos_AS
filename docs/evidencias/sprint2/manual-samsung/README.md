# Evidencia manual Samsung — 19/11/2026 (Lima)

Capturas originales aportadas por el usuario durante la ejecución real. No fueron recreadas ni modificadas. Horas 16:32–16:55 visibles; timestamps backend en UTC (Lima = UTC−5).

- 01: solicitud #5 creada, una línea Yamaha FZ azul.
- 02–03: preparación y búsqueda de unidad elegible para línea #6.
- 04: moto #3 asignada, 1 de 1.
- 05: solicitud #5 atendida y tres eventos de estado.
- 06: traslado #5 de moto #3 desde almacén #1 a tienda #2, historial conservado.
- 07–08: solicitud #7 creada con Honda Wave rojo (1) y Yamaha FZ azul (2).
- 09: formulario con selectores y dos líneas.
- 10: solicitud multilínea anulada con motivo e historial. Identificada mediante consulta SQL posterior como #8; la captura no muestra su encabezado.

Consulta SQL de solo lectura posterior: #5 ATENDIDA, #7 PENDIENTE, #8 ANULADA. No se modificaron datos para hacer coincidir resultados.

Defectos observados: regreso poco visible, secuencia de selectores no bloqueada, acciones destructivas con aspecto primario, destino conservado tras traslado e historial inicialmente sin cargar. Corregidos en código y nuevo APK instalado; falta comprobar visualmente la corrección en Samsung. No son defectos del backend ni justifican repetir todas las pruebas API.
