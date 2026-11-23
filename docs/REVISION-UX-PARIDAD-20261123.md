# Catálogo adaptable, navegación y comprobación de paridad — 23/11/2026

## Implementación 0.2.3 / código 5

- Catálogo con contenido centrado hasta 840 dp, listado visible y formulario secundario plegable. Campos en columna en móvil y fila cuando el formulario dispone de al menos 600 dp. Colores distribuidos en filas que se ajustan al ancho. Desplegables Material con etiqueta, tamaño del campo y opción para añadir texto nuevo.
- Atrás del sistema desde apartados vuelve a Inicio. En Inicio/login, primera pulsación muestra aviso; segunda dentro de dos segundos finaliza la Activity, sin ejecutar logout ni borrar datos. Cambiar de apartado/cuenta reinicia la confirmación.
- Menú abierto y diálogos se cierran antes de navegar. En guías con borradores locales modificados y en catálogo con combinación nueva sin guardar se solicita confirmación antes de descartarlos. Operaciones de catálogo/guías en curso consumen Atrás para no interrumpir una escritura.
- Android utiliza BackHandler de AndroidX inyectado en la UI compartida; no se intercepta onBackPressed antiguo ni se añade dependencia Android a commonMain. iOS no se declara validado.
- Referencia oficial: https://developer.android.com/guide/navigation/custom-back

## Resultados comprobados y alcance

El usuario confirmó que los selectores de 0.2.2 aparecen en Samsung; su valoración detectó problemas de distribución que motivan este incremento. Eso no acredita el diseño 0.2.3 ni sus gestos hasta ejecutarlos.

En `verificacion-http-20261123.json`: salud HTTP 200 y cuatro rechazos HTTP 401 sin token (catálogo, página de llegadas, inventario y solicitudes). No se usaron contraseñas ni se escribieron datos para estas comprobaciones. No confundir rechazo sin token con prueba 403 de rol autenticado.

## Siguiente prueba física corta

1. ADMIN entra y abre Catálogo: comprobar consulta, listado legible y «Nueva combinación» sin crear datos.
2. Abrir formulario: modelo bloqueado antes de marca, color bloqueado antes de modelo; elegir existentes y comprobar limpieza al cambiar marca. Revisar vertical/horizontal y teclado al elegir Añadir, sin guardar datos de prueba por accidente.
3. Desde catálogo sin edición pendiente, Atrás lleva a Inicio. Con datos nuevos sin guardar, comprobar aviso y Cancelar.
4. En Inicio, Atrás muestra aviso y mantiene la app; esperar más de dos segundos y repetir: no debe salir por una pulsación aislada. Dos pulsaciones dentro del intervalo deben salir.
5. Reabrir, verificar acceso y abrir menú: Atrás cierra menú antes de regresar. Confirmar cierre de diálogos antes de salir.

## Paridad con la web que aún requiere ejecución

| Flujo compartido | Cómo acreditarlo | Dependencia pendiente |
|---|---|---|
| Catálogo ADMIN | Alta, consulta posterior y duplicado rechazado; comprobar selección en consumidor | Documento/datos académicos autorizados para escritura |
| Recepción | Adjuntar 1–3 fotos válidas; obtener guía persistida; validar cantidades/archivos | Cuenta Almacén y fotos autorizadas |
| Registro por guía | Guardar parcialmente, salir, reabrir y finalizar; stock y estado concordantes | Cuenta Inventarios y guía pendiente |
| OCR | Revisar sugerencias y corregirlas; no creación automática | Tesseract no disponible; sigue pendiente |
| Ficha | Editar con permiso, copiar chasis y conservar identificadores | Cuenta autorizada y motocicleta de prueba |
| Solicitud | Crear desde catálogo, preparar/asociar unidades compatibles y completar historial | Comercial y Almacén autorizados |
| Anulación/rechazo | Motivo obligatorio, transiciones válidas y liberación de unidades cuando corresponda | Cuentas/solicitud de prueba; aceptación HU-27 sigue pendiente |
| Traslado | Consultar ubicación y registrar movimiento independiente de solicitud | Permisos y destinos autorizados |
| Seguridad y sesión | Login incorrecto/correcto, 403 de rol, expiración real y logout | Cuentas con distintos roles; no elevar ADMIN |

Implementación presente en código no equivale a recorrido aprobado. No se cambió backend, migraciones, cuentas ni datos por este ajuste visual/de navegación. Las pruebas físicas se registrarán con dispositivo, fecha real, IDs y capturas sin credenciales.
