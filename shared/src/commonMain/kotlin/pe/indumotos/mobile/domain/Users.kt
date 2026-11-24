package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: Long,
    val nombreCompleto: String,
    val nombreUsuario: String,
    val rolCodigo: String,
    val estado: String,
    val tiendaId: Long? = null,
    val tiendaNombre: String? = null
)

@Serializable
data class UserPage(
    val items: List<UserAccount>,
    val pagina: Int,
    val tamano: Int,
    val totalElementos: Long
) {
    val pages get() = ((totalElementos + tamano - 1) / tamano).toInt()
}

interface UsersRepository {
    suspend fun stores(): List<StoreSummary> = emptyList()
    suspend fun assignStore(user: Long, store: Long) {
        error("Asignación de tienda no disponible")
    }

    suspend fun resetPassword(id: Long, adminPassword: String, newPassword: String) {
        error("Restablecimiento no disponible")
    }

    suspend fun users(search: String, page: Int): UserPage
    suspend fun userAccess(id: Long): Access
    suspend fun changeRole(id: Long, role: String): Access
    suspend fun permission(id: Long, code: String, decision: String?): Access
}

object AccessRules {
    fun canAllow(role: String, code: String) = role == "ADMIN" || code !in setOf(
        "USUARIOS_CREAR",
        "PERMISOS_ASIGNAR",
        "CATALOGO_GESTIONAR"
    )
}
