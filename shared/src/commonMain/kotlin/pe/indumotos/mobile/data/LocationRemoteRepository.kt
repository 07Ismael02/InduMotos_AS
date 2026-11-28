package pe.indumotos.mobile.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

/** Ubicación conocida de cada motocicleta (HU-05). */
class LocationRemoteRepository(private val remote: RemoteRepository) : LocationRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun currentLocation(id: Long): LocationRecord? =
        remote.guideJson("motocicletas/$id/ubicacion").takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<LocationRecord?>(it) }

    override suspend fun sites(): List<Site> = json.decodeFromString(remote.guideJson("ubicaciones"))

    override suspend fun registerLocation(id: Long, destinationId: Long): LocationRecord {
        val payload = buildJsonObject { put("destinoId", destinationId) }
        val response = try {
            remote.guideJson("motocicletas/$id/ubicacion", payload.toString(), "POST")
        } catch (exception: AppException) {
            if (exception.error is AppError.Conflict) {
                throw AppException(
                    AppError.Conflict(
                        "La motocicleta ya tiene ubicación. Actualiza la consulta para ver el destino guardado."
                    )
                )
            }
            throw exception
        }
        return json.decodeFromString(response)
    }
}
