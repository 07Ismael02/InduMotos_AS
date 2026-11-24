# Cierre de implementación y estado verificable — 23/11/2026

Rama feature/sprint2-paridad-web. No se fusionó main. Backend original main 5b37b9b sin cambios de código; web f2981a3 usada como referencia. PharmaMobile permanece intacto.

## Faltantes implementados

- Listado de usuarios con búsqueda por nombre/usuario, paginación de diez y creación mediante el formulario existente.
- Accesos seleccionando cuenta por nombre, consulta inicialmente sin edición, cambio de rol y excepciones confirmadas. Se muestran todos los permisos devueltos por la API, incluidos catálogo y operaciones Sprint 2. No se permite conceder permisos administrativos a roles operativos.
- Detalle de guía con fichas vinculadas, apertura de ficha y copia de chasis. La ficha incluye la guía, recepción y cantidades relacionadas.
- Inventario con búsqueda automática diferida y selección directa de páginas. Se retiraron las pantallas muertas de consulta manual por ID.
- Conservados recepción con fotos, borradores, finalización, OCR revisado, edición, solicitudes/estados, asignación/retiro, motivos, anulación y traslados.

Código principal 824be6c; ajustes finales f24119d. Versión 0.2.4-sprint2, código 6. La compilación inicial de este incremento aprobó 29 pruebas; el resultado final y hash exacto se registran en build-cierre-20261123.json.

## API: 110 comprobaciones aprobadas

Evidencia en evidencias/cierre-20261123/resultado-recorrido-api.json. Ejecución real contra la copia indumotos_s2_paridad_20261123: login por cinco roles; listado/búsqueda de cuentas; cambios de rol y excepciones sobre cuentas QA; restricciones administrativas; catálogo y duplicados; recepción con dos imágenes; descarga idéntica al original; OCR con dos fotos y dos sugerencias; persistencia/reapertura de borradores sin crear stock; finalización, fichas vinculadas y protección contra doble finalización; edición y chasis duplicado; solicitudes, preparación, cupo, retiro con motivo, atención, anulación, rechazo, traslados e historial; logout y rechazo del token revocado.

El primer intento esperaba 201 donde POST seguimiento devuelve 200; se corrigió la expectativa según el controlador y se continuó sin repetir esa escritura. No se modificó el backend para acomodar las pruebas.

Las imágenes son documentos sintéticos etiquetados «QA / SIN VALIDEZ COMERCIAL», no guías reales de una empresa. Las pruebas no certifican precisión OCR sobre documentos comerciales. Las cuentas QA tienen claves aleatorias privadas y existen solo en la copia aislada. Se conservaron admin.indumotos y las demás cuentas previas; no se restablecieron contraseñas ni se ampliaron sus permisos. Los registros académicos se mantienen identificados para trazabilidad; no se eliminaron.

## Samsung: comprobado y pendiente

0.2.4 fue instalado mediante install -r con el mismo applicationId y certificado. Se observó login QA ADMIN/ALMACEN, listado y búsqueda de personal, accesos seleccionados por nombre, catálogo actualizado, guía confirmada con sus dos fotos y dos fichas vinculadas, apertura/copia de chasis y logout real hacia login. Capturas originales locales de este bloque están conservadas fuera del repositorio y no se publican sin completar su revisión.

La recepción por el selector nativo quedó en formulario sin envío confirmado. La última llamada ADB falló y el usuario solicitó cerrar/publicar de inmediato. No se considera probado en Android el recorrido íntegro de recepción, OCR, guardado/finalización, edición y logística, aunque sus endpoints y casos de uso sí se ejecutaron por API y sus pantallas están implementadas. Tampoco se acredita texto ampliado/horizontal/tema claro de todos los módulos ni iOS. La regla académica HU-27 conserva su aceptación pendiente.

Los cambios finales posteriores a la instalación incluyen limpieza de código muerto, protección de búsqueda de cuatro caracteres, etiqueta accesible Volver y retirada de un texto repetido. El APK final puede requerir actualización en el teléfono si ADB no está disponible; consultar el manifiesto final.

## Entorno para continuar

- API ejecutada ahora en contenedor indumotos-paridad-api, imagen local indumotos-paridad-ocr:5b37b9b; mismo JAR, runtime Java 21 y Tesseract español. Sin instalación de programas en Windows.
- PostgreSQL existente indumotos-distribuidas-db-1; se añadió una red Docker privada para comunicar API y base. Puertos publicados exclusivamente en 127.0.0.1:8090 y 127.0.0.1:55432. Sin cambios de firewall ni Oracle.
- No usar el lanzador Java anterior para este entorno. Para contenedores ya creados: scripts/Iniciar-Entorno-Paridad.ps1 -Serial RFGL11XYCTD. App 127.0.0.1:8089; túnel USB 8089→8090.
- Secretos, manifiesto del respaldo y credenciales QA permanecen fuera del repositorio en tmp/sprint2-auditoria/privado-paridad, con permisos restringidos. No compartir el respaldo ni runtime-ocr.env: contienen información sensible. No se publican usuarios/contraseñas de acceso.
- El respaldo V15 continúa conservado; original indumotos_s2_verificacion no se migró. No ejecutar down -v ni recrear volúmenes.

El Excel no se modificó en este bloque. No se declara aceptación del negocio ni paridad física integral solo por pasar las pruebas de API.
