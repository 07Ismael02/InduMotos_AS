package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoginRequest(val nombreUsuario: String, val clave: String)

@Serializable
data class LoginResponse(val accessToken: String, val expiresIn: Long)

@Serializable
data class Access(
    val usuarioId: Long,
    val nombreUsuario: String,
    val rolCodigo: String,
    val permisos: List<Permission>
)

@Serializable
data class Permission(
    val codigo: String,
    val permitido: Boolean,
    val porRol: Boolean = false,
    val excepcion: String? = null
)

@Serializable
data class InventoryPage(
    val items: List<JsonObject>,
    val pagina: Int,
    val totalPaginas: Int,
    val totalElementos: Long
)

data class InventoryFilter(
    val chasis: String = "",
    val marca: String = "",
    val modelo: String = "",
    val color: String = ""
)

enum class Module(val title: String, val permission: String, val description: String) {
    CATALOG("Catálogo", MANAGE_CATALOG, "Administrar marcas, modelos y colores"),
    REQUESTS("Solicitudes de ensamblaje", READ_REQUESTS, "Crear y atender solicitudes por líneas"),
    TRANSFERS("Traslados", READ_LOCATIONS, "Consultar recorrido y trasladar motocicletas"),
    INVENTORY("Inventario", "INVENTARIO_CONSULTAR", "Buscar y filtrar motocicletas"),
    ARRIVALS("Llegadas", "LLEGADAS_REGISTRAR", "Registrar y consultar recepciones"),
    FICHAS(
        "Fichas de motocicletas",
        "FICHAS_REGISTRAR",
        "Registrar desde guía y reanudar borradores"
    )
}

interface LogisticsRepository {
    suspend fun arrivals(guide: String): List<JsonObject> =
        error("Consulta de llegadas no disponible")

    suspend fun login(baseUrl: String, user: String, password: String): Access
    suspend fun inventory(filter: InventoryFilter, page: Int): InventoryPage
    suspend fun detail(module: Module, id: Long): JsonObject
    suspend fun submit(path: String, body: JsonObject, method: String = "POST"): JsonObject?
    suspend fun logout()
    fun clearSession()
}

object Rules {
    fun positive(value: String) = value.toLongOrNull()?.let { it > 0 } == true
    fun required(value: String) = value.isNotBlank()
    fun server(value: String) = value.startsWith("http://") || value.startsWith("https://")
}

fun roleLabel(code: String) = when (code) {
    "ADMIN" -> "Administración";
    "ALMACEN" -> "Almacén";
    "INVENTARIOS" -> "Inventarios";
    "COMERCIAL" -> "Comercial";
    "FACTURACION" -> "Facturación";
    "ENSAMBLAJE" -> "Ensamblaje";
    else -> code
}

val PermissionLabels = linkedMapOf(
    "USUARIOS_CREAR" to "Crear cuentas",
    "PERMISOS_ASIGNAR" to "Administrar permisos",
    "LLEGADAS_REGISTRAR" to "Registrar llegadas",
    "FICHAS_REGISTRAR" to "Registrar fichas",
    "FICHAS_EDITAR" to "Editar fichas",
    "INVENTARIO_CONSULTAR" to "Consultar inventario",
    "CATALOGO_GESTIONAR" to "Gestionar catálogo",
    "UBICACIONES_CONSULTAR" to "Consultar ubicaciones",
    "UBICACIONES_REGISTRAR" to "Registrar ubicación inicial",
    "TRASLADOS_REGISTRAR" to "Registrar traslados",
    "SOLICITUDES_CREAR" to "Crear solicitudes",
    "SOLICITUDES_CONSULTAR" to "Consultar solicitudes",
    "SOLICITUDES_AVANZAR" to "Actualizar solicitudes",
    "SOLICITUDES_ANULAR" to "Anular solicitudes",
    "UNIDADES_ASIGNAR" to "Asignar y retirar motocicletas"
)
