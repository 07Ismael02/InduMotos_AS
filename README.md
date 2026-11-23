# Indumotos AS — aplicativo móvil

Corrección de catálogo 0.2.2: selección de marcas/modelos/colores existentes y opción explícita para añadir texto nuevo (solo administración). Consulta de catálogo confirmada por el usuario en Samsung con 0.2.1; selectores nuevos pendientes de validación física.

Entorno del 23/11: backend V23 HTTP 200 en copia aislada y APK 0.2.1 instalado en Samsung mediante actualización compatible. Login y recorridos manuales pendientes; ver [estado comprobado](docs/ENTORNO-PARIDAD-20261123.md).

Actualizado el 23/11/2026. Trabajo activo en `feature/sprint2-paridad-web`; no integrado a main. Objetivo: paridad funcional con la web dentro del recorrido de Sprint 2 y sus prerrequisitos de recepción/inventario. PharmaMobile se conserva solo como referencia.

## Referencias verificadas

- Web: `Akatosh4019/SISTEMA-WEB-INDUMOTOS`, main `f2981a368b2315d7278920bfe7c170dad3e4af8b`.
- Backend: `Akatosh4019/BACK-END-INDUMOTOS`, main `5b37b9b812c3c08e7edd4aeac67c3216e62850c7`.
- Contraste y límites: [paridad funcional](docs/PARIDAD-WEB-SPRINT2.md).

## Arquitectura

Kotlin Multiplatform / Compose. `androidApp` es la entrada Android; `shared` contiene presentación, modelos y contratos de dominio, implementaciones REST y Koin. Pantalla → ViewModel → contrato de repositorio → API. Los flujos nuevos de catálogo y guías tienen componentes propios; la autorización efectiva permanece en backend. Android selecciona documentos con el selector del sistema, sin permisos generales de almacenamiento. La decodificación de fotos tiene adaptadores por plataforma.

## Funciones del incremento

- Catálogo maestro: administración agrega combinaciones; Comercial selecciona las opciones autorizadas, sin inventarlas a partir del inventario.
- Llegadas pendientes/confirmadas, filtro por guía y paginación real.
- Registro de llegada con 1–3 JPG/PNG; límites de tamaño y dimensiones, además de validación del servidor.
- Fotos protegidas, ampliación y arrastre como referencia durante el registro de fichas.
- Bloques de cinco motocicletas, guardar borradores en servidor, reanudar y finalizar la guía con confirmación.
- OCR de todas las fotos mediante API: solo sugerencias; exige revisión y no guarda automáticamente fichas.
- Edición con FICHAS_EDITAR; inventario, copia de chasis, solicitudes multilínea, seguimiento, asignación, retiro, anulación y traslados conservados.

## Entorno y seguridad

La API nueva requiere esquema Flyway V23. La base histórica de validación tenía V15: no lanzar la API nueva sobre ella ni sobre datos históricos sin respaldo/copia y revisión. Este bloque no aplicó migraciones ni modificó el backend o las cuentas.

URL local incorporada: `http://127.0.0.1:8089`. Para backend aislado en PC:8090, usar `adb reverse tcp:8089 tcp:8090`. Si se cambia la URL de compilación, documentar el build exacto. No abrir PostgreSQL a la red.

Se conserva applicationId `pe.indumotos.asapp` y la clave local existente. Nunca versionar claves, JWT, fotos de documentos reales sin autorización ni credenciales. No copiar sessionStorage de la web: la sesión móvil permanece en memoria. Cuentas y permisos se configuran en el entorno, no se amplían para aparentar pruebas aprobadas.

## Build y pruebas

Usar Java 21 y las herramientas existentes:

```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. La instalación sobre un teléfono requiere comprobar primero firma y versión; nunca resolver conflictos borrando datos sin autorización.

Las suites locales verifican reglas y estados; todavía no acreditan funcionamiento contra API V23 ni pruebas físicas del nuevo APK. Las capturas del 19/11 pertenecen a la versión anterior. [Verificaciones históricas](docs/VALIDACION-SPRINT2-20261119.md). [Matriz manual del incremento](docs/PRUEBAS-PARIDAD-SPRINT2.md).

## Continuidad

Leer [CONTINUIDAD](docs/CONTINUIDAD.md) y AGENTS.md. Web y Figma los trabajan otros colaboradores. Excel: solo la pestaña existente `SCRUM S2 19-11-2026`, con fecha real y sin modificar formato. No declarar pruebas físicas nuevas hasta ejecutarlas. No merge a main ni etiqueta final hasta validar el APK exacto.

Limitaciones: iOS no compilado en Windows; selector de fotos implementado en Android. No se añadió offline ni refresh token. El alcance no incluye historias de sprints posteriores. La regla de anulación pendiente sigue sujeta a aceptación de negocio.
