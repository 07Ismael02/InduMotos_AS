# Cámara y perfil — 24/11/2026

Rama feature/ismael-catalogo-ventas. Cámara y perfil en0.4.2-evolucion/código15; selector de origen adaptado al tema en0.4.3-evolucion/código16. Backend y esquema conservados.

## Alcance

- La selección de origen permite «Tomar foto» o «Elegir de galería» mediante un diálogo Compose Material3 que hereda la apariencia de Indumotos. Sustituye al selector Android claro detectado en la primera prueba. Se usa la cámara del sistema, no una cámara integrada nueva. La fotografía se recibe por URI temporal de FileProvider no exportado, limitado al directorio privado de capturas. No se añaden permisos CAMERA ni de acceso general al almacenamiento.
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

Build Android0.4.2 aprobado; 62 pruebas móviles, cero fallos/errores, incluidas tres nuevas comprobaciones de agrupación de accesos efectivos y acumulación/límite de fotos. Firma del APK comparada con la instalación existente y coincidente: SHA256 23c1a0373560f28c3d2b64103e50a0dff54bb955fd75b20ac40d52253e33b0f0. Instalación con adb install -r: Success, datos conservados. Las comprobaciones del backend de pagos anteriores no equivalen a prueba física de Yape/Plin; sigue siendo registro académico con confirmación manual, sin integración bancaria.

### Resultado físico posterior

Con almacen.indumotos se verificó el perfil agrupado sin la antigua lista extensa y se abrió Nueva llegada con fotos. Desde Tomar foto se abrió la cámara Samsung; el responsable fotografió una imagen académica mostrada en el monitor, confirmó la toma y regresó al formulario. Se observó miniatura, contador1/3 y opción Quitar; el directorio privado de capturas quedó vacío. No se registró la llegada ni se subió esta fotografía al servidor, no se ejecutó OCR y no se creó stock. Esta prueba acredita captura/retorno/previsualización, no lectura de una guía real ni persistencia de una nueva recepción.

Evidencias privadas fuera del repositorio: 042-perfil-modulos.png, 042-origen-fotografia.png y 042-camara-retorno.png en tmp/sprint2-auditoria/privado-paridad del espacio de trabajo. La toma del monitor contiene personas en el fondo y no se publica. APK y huella en build-042.json.

El responsable observó que el selector de origen anterior no respetaba el tema oscuro; se reemplazó por un diálogo compartido dentro de IndumotosTheme. Cámara y galería quedan como operaciones Android separadas. La toma de la prueba permanece sin registrar; no se afirma que un formulario sin guardar sobreviva a instalar otro APK. La comprobación adicional de cancelación se interrumpió por pérdida de ADB, sin llegar a ejecutarse y sin cambios de datos. Instalación y revisión visual de0.4.3 deben confirmarse con el dispositivo disponible.
