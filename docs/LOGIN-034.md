# Login ordenado — 23/11/2026

Versión0.3.4/código11, código adff08e, rama feature/ismael-offline-cuentas. Formulario con ancho máximo440dp y desplazamiento; un botón secundario Sin conexión reemplaza la lista de cuentas del login. Abre un diálogo desplazable con rol, usuario, selección única y Abrir datos/Volver. Estado sin cuentas explica cómo descargar. El desbloqueo Android y las reglas de vigencia/aislamiento siguen iguales.

Compilación aprobada, firma SHA256 coincidente e instalación -r Success en Samsung. Tres comprobaciones reales: login sin lista de usuarios, selector con cuatro perfiles y selección de Facturación/Volver sin iniciar sesión. Capturas revisadas en evidencias/login-034. Se observó además Inicio en consulta local con admin.indumotos durante la revisión; no se automatizó ni capturó el PIN. No se repitió la suite53 del incremento anterior por este cambio visual. Horizontal, texto ampliado y estado vacío no se validaron físicamente en este bloque.

No mostrar todos los usuarios del servidor: solo perfiles vigentes con datos propios descargados. Comercial/Ensamblaje no aparecen por tener una cuenta; necesitan lecturas offline implementadas y autorizadas. No se amplían permisos ni se copian datos para llenar el selector.

Manifiesto: build-login-20261123.json. Pendientes actuales: PENDIENTES-ENTREGA.md. Main y backend sin cambios en este bloque; no se declara entrega release ni cierre integral.
