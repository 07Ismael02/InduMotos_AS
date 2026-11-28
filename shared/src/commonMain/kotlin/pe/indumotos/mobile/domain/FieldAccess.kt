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
}
