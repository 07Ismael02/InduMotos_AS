package pe.indumotos.mobile.domain

/**
 * Módulos de campo que el aplicativo ofrece a cada rol.
 *
 * El servidor concede permisos amplios porque la web los usa todos; el móvil
 * solo muestra lo que cada puesto hace en el almacén o la tienda. Un módulo
 * aparece si pertenece al rol y, además, el servidor concede su permiso.
 * Administración gestiona personal y permisos desde la web: no tiene módulos.
 */
object FieldAccess {
    private val byRole: Map<String, Set<Module>> = mapOf(
        "ALMACEN" to setOf(Module.ARRIVALS, Module.INVENTORY, Module.TRANSFERS, Module.REQUESTS),
        "INVENTARIOS" to setOf(Module.FICHAS, Module.INVENTORY),
        "COMERCIAL" to setOf(Module.REQUESTS),
        "ENSAMBLAJE" to setOf(Module.REQUESTS),
        "FACTURACION" to setOf(Module.INVENTORY),
        "GERENTE" to setOf(Module.INVENTORY, Module.TRANSFERS, Module.REQUESTS)
    )

    fun belongsToRole(role: String, module: Module): Boolean =
        module in byRole[role].orEmpty()

    /** Describe el módulo según lo que la cuenta realmente puede hacer en él. */
    fun description(module: Module, allowed: (String) -> Boolean): String = when (module) {
        Module.ARRIVALS ->
            if (allowed(module.permission)) module.description else "Consultar las guías recibidas"
        Module.TRANSFERS ->
            if (allowed(TRANSFER_UNITS)) module.description else "Consultar ubicación y recorrido de cada moto"
        Module.REQUESTS -> when {
            allowed(CREATE_REQUESTS) || allowed(ADVANCE_REQUESTS) || allowed(ASSIGN_UNITS) -> module.description
            allowed(CANCEL_REQUESTS) -> "Seguir solicitudes y autorizar anulaciones"
            else -> "Consultar el avance de las solicitudes"
        }
        else -> module.description
    }
}
