# Indumotos AS — instrucciones de continuidad

Leer README.md, docs/CONTINUIDAD.md y docs/SPRINT2.md antes de modificar código. El último estado fechado está allí; verificar siempre Git y el entorno actual.

- Backend correcto: Akatosh4019/BACK-END-INDUMOTOS; base observada feature/roberto-sprint1-backend. No sustituirlo por backend-DAM-final. Coordinar cambios concurrentes del equipo.
- main es la referencia estable y predeterminada. Funciones en feature; no usar develop antiguo por inercia, no force push ni merge de funciones sin revisión/verificación y autorización. Preservar cambios locales.
- PharmaMobile es referencia de comportamiento/arquitectura y permanece intacto; no copiar cuentas, secretos, datos farmacéuticos ni applicationId.
- El usuario eligió mantener admin.indumotos en la base nueva y rechazó crear un usuario de demostración. No inventar contraseñas; no publicar .env, JWT, claves ni keystores. No resetear cuentas por cambiar variables del bootstrap.
- Cumplir el alcance del Sprint 2 de las fuentes; no inventar reglas, aprobaciones, reuniones ni evidencias. Distinguir implementado de comprobado y pendientes.
- Usar wrappers y herramientas existentes; no instalaciones, compilaciones o pruebas repetidas sin motivo. Cambios nuevos sí requieren verificaciones pertinentes.
- Nunca borrar datos locales, desinstalar ante una firma incompatible, recrear volúmenes o reescribir migraciones para ocultar problemas. Conservar identificadores y respaldos.
- La configuración del backend corresponde al entorno, no al trabajador. Evitar controles técnicos en el login final; temas y marca deben verse legibles en el dispositivo.
- Antes de terminar cada bloque, actualizar README/CONTINUIDAD con fecha, rama/commit, verificación real y siguiente acción. No marcar Sprint 2 terminado solo por la urgencia del 19/11/2026.
