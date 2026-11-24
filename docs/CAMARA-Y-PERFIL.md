# Cámara y perfil — 24/11/2026

Rama feature/ismael-catalogo-ventas. Versión0.4.2-evolucion, código15. Backend y esquema conservados.

## Alcance

- La selección de fotografías Android permite «Tomar foto» o «Elegir de galería». Se usa la cámara del sistema, no una cámara integrada nueva. La fotografía se recibe por URI temporal de FileProvider no exportado, limitado al directorio privado de capturas. No se añaden permisos CAMERA ni de acceso general al almacenamiento.
- Captura a resolución completa, lectura acotada, reducción de tamaño, corrección de orientación EXIF y recodificación JPEG de hasta5MB. La imagen final no copia metadatos GPS. Se elimina el archivo temporal al procesar o cancelar la captura.
- En Nueva llegada se pueden acumular hasta tres fotos y quitar una sin reemplazar las restantes. La cámara también está disponible donde ya se adjuntaban fotos de unidades o comprobantes. No se cambia la autorización ni se añaden envíos offline de imágenes.
- Se conservan los formularios durante cambios de orientación mediante la configuración Android de la actividad; esto no garantiza recuperar un formulario sin guardar después de que Android termine el proceso.
- Perfil agrupa únicamente los permisos efectivos habilitados por módulos. Las acciones quedan plegadas y se abren a petición del usuario. No se muestran de entrada listas largas de códigos ni se deducen permisos a partir del nombre del rol.

## Historias y roles

Libro Sprint2_bitacoras_trabajo_completada.xlsm, hoja G4 - FAMILIA VALENCIA: HU-04 en fila37 (Almacén recibe y registra la llegada), HU-25 en fila49 y estimaciónfila80 (Inventarios prepara fichas desde la guía fotografiada, revisa y confirma). Son fotografías de la guía documental; no equivalen a fotos de la motocicleta.

El código conserva LLEGADAS_REGISTRAR para recepción; FICHAS_REGISTRAR para preparar fichas y FICHAS_EDITAR para adjuntar fotos a una unidad existente. Almacén, Inventarios y Administración acceden según sus permisos efectivos; las excepciones y la autorización final se aplican en el servidor. No se alteran cuentas ni roles para mostrar un botón.

Referencia técnica: [contrato TakePicture](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.TakePicture) y [cámara delegada mediante intents](https://developer.android.com/media/camera/camera-intents).

## Entorno y comprobaciones

Se iniciaron los contenedores existentes indumotos-distribuidas-db-1 e indumotos-evolucion-api. PostgreSQL permanece en loopback55432 y API enlazada exclusivamente a hotspot192.168.137.1:8443 y loopback8091. Salud HTTPS HTTP200 con certificado validado. Sin recrear volumen, ejecutar migraciones de nuevo, cambiar firewall ni publicar API en campus.

Build Android aprobado; 62 pruebas móviles, cero fallos/errores, incluidas tres nuevas comprobaciones de agrupación de accesos efectivos y acumulación/límite de fotos. Firma del APK comparada con la instalación existente y coincidente: SHA256 23c1a0373560f28c3d2b64103e50a0dff54bb955fd75b20ac40d52253e33b0f0. Instalación con adb install -r: Success, datos conservados. La captura física y retorno de cámara todavía deben ejecutarse; no se declaran aprobados solo porque compiló. Las comprobaciones del backend de pagos anteriores no equivalen a prueba física de Yape/Plin; sigue siendo registro académico con confirmación manual, sin integración bancaria.
