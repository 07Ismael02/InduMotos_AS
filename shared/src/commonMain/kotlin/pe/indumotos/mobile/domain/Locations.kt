package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable

const val READ_LOCATIONS = "UBICACIONES_CONSULTAR"
const val WRITE_LOCATION = "UBICACIONES_REGISTRAR"

@Serializable
data class Site(val id: Long, val codigo: String, val nombre: String, val tipo: String = "")

@Serializable
data class LocationRecord(
    val destinoId: Long,
    val destinoNombre: String,
    val registrador: String,
    val fecha: String
)

data class MotorcycleLocation(val current: LocationRecord?, val destinations: List<Site>)

interface LocationRepository {
    suspend fun currentLocation(id: Long): LocationRecord?
    suspend fun sites(): List<Site>
    suspend fun registerLocation(id: Long, destinationId: Long): LocationRecord
}
