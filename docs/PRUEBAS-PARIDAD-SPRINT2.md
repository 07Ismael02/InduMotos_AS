# Validación manual del incremento — pendiente

Actualización: APK vigente 0.2.3. Login ADMIN, catálogo/selección y navegación básica observados en Samsung; nueve comprobaciones y capturas en evidencias/paridad-20261123. Los ocho recorridos completos de esta tabla no están aprobados. Antes de concluir equivalencia, cerrar además los faltantes de implementación identificados en PARIDAD-WEB-SPRINT2.md; lo indicado abajo sobre 0.2.1 es el plan inicial.

Ejecutar sobre el APK 0.2.1-sprint2 identificado por hash/certificado y API main 5b37b9b con V23 en copia aislada. No se ejecutaron estos recorridos en este bloque. Se necesitan cuentas reales autorizadas: Administración, Almacén, Inventarios y Comercial; no crear cuentas de demostración ni ampliar roles sin acuerdo. Elegir únicamente datos académicos identificados o documentos con autorización.

| Caso | Procedimiento / resultado que comprobar | Evidencia pendiente |
|---|---|---|
| 1. Acceso | Login incorrecto/correcto; nombre y rol; expiración auténtica y logout; retorno a login y limpieza de fotos/datos entre cuentas. | Capturas, HTTP 401 cuando corresponda, fecha y usuario sin contraseña/token. |
| 2. Catálogo | ADMIN agrega combinación válida; rechazar duplicado. Otros roles solo seleccionan y servidor rechaza escritura sin permiso. | Capturas por rol y 201/403/409 observados; no inferir código si no se capturó. |
| 3. Recepción | Almacén elige 1–3 fotos válidas, informa guía y cantidades. Verificar rechazo local/servidor de archivo inválido o excedido. No repetir POST de respuesta incierta sin consultar. | Guía real de prueba, ID devuelto, captura, confirmación en consulta. |
| 4. Consulta de guías | Pendientes/confirmadas; búsqueda por guía, paginación de 10, fotos con siguiente/anterior y ampliación. Sin buscar por ID manual. | Capturas y datos concordantes con API. |
| 5. Borradores | Inventarios elige guía pendiente, completa bloques de cinco, guarda una parte, vuelve y reabre desde servidor. Datos sin guardar se distinguen de guardados; advertencia de recarga. | Reapertura real con los mismos campos y posiciones; fotos visibles. |
| 6. OCR | Leer todas las fotos; revisar sugerencias, aplicar solo a bloque no guardado y confirmar revisión. Corregir errores sin crear stock automáticamente. Si lector no está disponible, conservar registro manual. | Fotos analizadas, sugerencias, corrección humana; sin presentar datos propuestos como verdaderos. |
| 7. Cierre / edición | No finalizar con bloques pendientes/alterados. Finalizar una sola vez, confirmar estado CONFIRMADA y stock. Editar ficha con permiso; preservar ID/recepción; comprobar duplicado de chasis y rechazo sin privilegio. | IDs, cantidades y estado consultados, resultados HTTP/BD reales. |
| 8. Logística S2 | Comercial crea solicitud desde catálogo/destino; Almacén prepara, asocia chasis compatibles, retira con motivo o atiende/rechaza; consulta historial, anulación pendiente y traslado separado. Revisión de cantidades, exclusividad, motivos y estados. | Capturas de este APK y IDs reales. No reutilizar el flujo anterior como prueba nueva. |

Registrar dispositivo, Android, fecha real, cuenta/rol, pasos, esperado, observado y defecto. Las capturas no deben contener contraseñas, JWT, credenciales ni datos de clientes sin autorización. No borrar datos ante un conflicto de firma. Los estados del Excel cambian solo con resultados comprobados.
