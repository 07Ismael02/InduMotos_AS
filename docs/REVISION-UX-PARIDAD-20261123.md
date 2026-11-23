# Catálogo adaptable, navegación y comprobación de paridad — 23/11/2026

## Implementación 0.2.3 / código 5

- Catálogo con contenido centrado hasta 840 dp, listado visible y formulario secundario plegable. Campos en columna en móvil y fila cuando el formulario dispone de al menos 600 dp. Colores distribuidos en filas que se ajustan al ancho. Desplegables Material con etiqueta, tamaño del campo y opción para añadir texto nuevo.
- Atrás del sistema desde apartados vuelve a Inicio. En Inicio/login, primera pulsación muestra aviso; segunda dentro de dos segundos finaliza la Activity, sin ejecutar logout ni borrar datos. Cambiar de apartado/cuenta reinicia la confirmación.
- Menú abierto y diálogos se cierran antes de navegar. En guías con borradores locales modificados y en catálogo con combinación nueva sin guardar se solicita confirmación antes de descartarlos. Operaciones de catálogo/guías en curso consumen Atrás para no interrumpir una escritura.
- Android utiliza BackHandler de AndroidX inyectado en la UI compartida; no se intercepta onBackPressed antiguo ni se añade dependencia Android a commonMain. iOS no se declara validado.
- Referencia oficial: https://developer.android.com/guide/navigation/custom-back

## Resultados comprobados y alcance

Actualización 18:32 (Lima): APK 0.2.3 compilado, firma comprobada e instalado -r sin limpiar datos. Después del login manual del usuario se ejecutaron nueve comprobaciones de selección/navegación en Samsung: marca habilita modelo manteniendo color bloqueado; modelo habilita color; selección Honda/Wave/Rojo sin guardar; Atrás desde Catálogo vuelve a Inicio; menú mantiene Activity y cierra hacia Inicio; primera pulsación muestra aviso sin salir; una pulsación después del intervalo no sale; segunda dentro del intervalo sí sale. Se reabrió la app, quedando en login.

Informe y capturas originales revisadas: [evidencias](evidencias/paridad-20261123/resultado-ux-023.json), [Catálogo](evidencias/paridad-20261123/catalogo-023.png), [aviso de salida](evidencias/paridad-20261123/aviso-salida-023.png). No se publicaron las capturas que contenían una notificación personal o la animación del menú. El primer intento automatizado pulsó Atrás antes de terminar de abrir el menú y no lo cerró; se observó el menú abierto y se comprobó el cierre tras esperar la animación. No se presenta ese intento como prueba de cierre aprobada.

Diseño observado en vertical/tema oscuro, sin recortes en el listado y con formulario en columna. Horizontal, pantallas grandes, texto ampliado, otros temas y confirmaciones de descarte siguen pendientes; no declarar responsive universalmente verificado. No hubo escrituras en el catálogo ni operaciones comerciales.

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
