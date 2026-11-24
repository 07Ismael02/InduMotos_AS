package pe.indumotos.mobile.data

import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import pe.indumotos.mobile.domain.*

class VehicleRemoteRepository(private val remote: RemoteRepository) : VehicleRepository {
    private val json = Json {
        ignoreUnknownKeys = true;
        encodeDefaults = true
    }

    override suspend fun journey(id: Long): JsonObject =
        json.parseToJsonElement(remote.guideJson("motocicletas/$id/recorrido")).jsonObject

    override suspend fun card(id: Long): VehicleCard =
        json.decodeFromString(remote.guideJson("motocicletas/$id/ficha-ampliada"))

    override suspend fun save(id: Long, specs: VehicleSpecs): VehicleCard = json.decodeFromString(
        remote.guideJson(
            "motocicletas/$id/ficha-ampliada",
            json.encodeToString(specs),
            "PUT",
            onlineOnly = true
        )
    )

    override suspend fun photo(id: Long, photo: Long) =
        remote.guideBytes("motocicletas/$id/fotos/$photo")

    override suspend fun addPhoto(id: Long, photo: GuidePhoto): VehicleCard =
        json.decodeFromString(remote.uploadImage("motocicletas/$id/fotos", emptyMap(), photo))

    override suspend fun availability(id: Long): VehicleAvailability =
        json.decodeFromString(remote.guideJson("motocicletas/$id/disponibilidad"))

    override suspend fun changeAvailability(
        id: Long,
        state: String,
        reason: String
    ): VehicleAvailability = json.decodeFromString(
        remote.guideJson(
            "motocicletas/$id/disponibilidad",
            buildJsonObject {
                put("estado", state);
                put("motivo", reason)
            }.toString(),
            "POST",
            onlineOnly = true
        )
    )
}
