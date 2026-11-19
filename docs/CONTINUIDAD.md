# Continuidad del aplicativo — 19/11/2026

## Último trabajo real

El incremento publicado terminó en `8fd7618`; el código que generó el APK comprobado corresponde a `3de365b`. Se subieron develop y feature/integrador-13; posteriormente se creó main con esa base y se configuró como predeterminada. El backend correcto es BACK-END-INDUMOTOS, feature/roberto-sprint1-backend, d4acdd0; no se cambió su código.

Se creó localmente feature/login-identidad para los siguientes pedidos del usuario. **No se implementaron aún** los cambios visuales: la conversación se detuvo para aclarar cuentas y backend. Solo se descargó la tipografía Audiowide y su licencia como material de trabajo; no constituyen una nueva identidad final ni un cambio validado. No hay que buscar un APK con esas correcciones porque no se generó.

## Pedidos abiertos, en orden

1. Mantener main estable y separar funcionalidades en ramas; no partir automáticamente de develop, que conserva una base antigua.
2. Icono de launcher reconocible: el logo completo sobre negro queda diminuto en la cuadrícula de apps. Diseñar una variante legible, respetando la identidad Indumotos y validando el icono en el teléfono.
3. Login profesional con tema claro y oscuro reales: quitar el rectángulo negro del logo en modo claro; asegurar contraste del nombre/logotipo y evitar letras blancas ilegibles sobre fondo claro. La instrucción de color del usuario expresa adaptar la marca al tema; no resolverla con una combinación sin contraste. Conservar el logo original y revisar las variantes visualmente.
4. Retirar Configuración de conexión del flujo del trabajador. La URL se define por entorno/configuración de compilación y no la configura cada usuario. Actualmente la UI aún contiene el campo y default localhost:8089.
5. Mantener admin.indumotos en la base nueva; **no crear una cuenta de demostración**. No restablecer a Roberto del proyecto anterior. Si se cambia la contraseña actual, hacerlo solo mediante entrada privada y procedimiento seguro para la cuenta identificada. La contraseña no se ha cambiado; no documentar una nueva inventada.
6. Sprint 2: ubicación y traslados, solicitudes y ensamblaje; revisar HU-27 y reglas pendientes. El usuario requiere avanzar para hoy, 19/11. Ver SPRINT2.md.

## Límites del contexto transferible

Git contiene código, documentación y capturas; no contiene PostgreSQL, el estado de Docker, el proceso Spring, las credenciales, la clave Debug privada ni las sesiones. La nueva IA debe verificar el entorno de esa PC antes de afirmar que algo está funcionando. No repetir instalaciones de la recuperación de PharmaMobile.

No modificar la aplicación de farmacia ni copiar sus credenciales, datos, applicationId o migraciones. Reutilizar como referencia: menú lateral, identidad y rol, navegación según permisos, separación UI/casos de uso/repositorio, errores y verificaciones auténticas. SQLDelight/offline de farmacia no está en Indumotos y requiere contrato y alcance propios antes de adaptarse.

## Próxima acción concreta del nuevo chat

Leer README, este archivo, SPRINT2 y el backend. Inspeccionar ramas remotas actuales; coordinar el contrato del Sprint 2 con la base de Roberto. Producir una tabla corta de endpoints/permisos disponibles y faltantes, resolver lo comprobablemente bloqueante del login y avanzar al recorrido vertical de solicitud → atención → chasis asociado → traslado. Preguntar solo por secretos privados o decisiones operativas que no estén en las fuentes, mientras se continúa el resto.

## Cómo registrar cada avance

- Fecha/hora y rama/commit.
- Problema concreto y archivos afectados.
- Resultado de compilación, prueba o interacción realmente ejecutada, con ruta de evidencia.
- Diferenciar código terminado, dispositivo comprobado y pendientes.
- Actualizar contrato cuando cambie la API; no sustituir endpoints reales por respuestas simuladas para aparentar integración.
- Dejar instrucciones de arranque reproducibles y sin rutas personales obligatorias ni secretos.
