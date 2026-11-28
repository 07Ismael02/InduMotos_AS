package pe.indumotos.mobile.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

/** Solicitudes, seguimiento, asignación por chasis, anulación y traslados (HU-06 a HU-09, HU-27). */
class OperationsRemoteRepository(private val remote: RemoteRepository) : OperationsRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun requests(page: Int): List<AssemblyRequest> = json.decodeFromString(
        remote.guideJson(
            "solicitudes",
            params = mapOf("pagina" to page.toString(), "tamano" to "20")
        )
    )

    override suspend fun request(id: Long): AssemblyRequest =
        json.decodeFromString(remote.guideJson("solicitudes/$id"))

    override suspend fun createRequest(body: NewRequest): AssemblyRequest =
        json.decodeFromString(remote.guideJson("solicitudes", json.encodeToString(body), "POST"))

    override suspend fun events(id: Long): List<RequestEvent> =
        json.decodeFromString(remote.guideJson("solicitudes/$id/seguimiento"))

    override suspend fun advance(id: Long, status: String, reason: String) {
        remote.guideJson(
            "solicitudes/$id/seguimiento",
            buildJsonObject {
                put("estado", status);
                put("motivo", reason)
            }.toString(),
            "POST"
        )
    }

    override suspend fun cancel(id: Long, reason: String) {
        remote.guideJson(
            "solicitudes/$id/anulacion",
            buildJsonObject { put("motivo", reason) }.toString(),
            "POST"
        )
    }

    override suspend fun eligible(chassis: String): List<EligibleUnit> = json.decodeFromString(
        remote.guideJson(
            "motocicletas/elegibles",
            params = mapOf("chasis" to chassis)
        )
    )

    override suspend fun assigned(id: Long, line: Long): List<AssignedUnit> =
        json.decodeFromString(remote.guideJson("solicitudes/$id/lineas/$line/unidades"))

    override suspend fun assign(id: Long, line: Long, motorcycle: Long) {
        remote.guideJson(
            "solicitudes/$id/lineas/$line/unidades",
            buildJsonObject { put("motocicletaId", motorcycle) }.toString(),
            "POST"
        )
    }

    override suspend fun remove(id: Long, line: Long, association: Long, reason: String) {
        remote.guideJson(
            "solicitudes/$id/lineas/$line/unidades/$association",
            buildJsonObject { put("motivo", reason) }.toString(),
            "DELETE"
        )
    }

    override suspend fun transfer(motorcycle: Long, destination: Long): TransferRecord =
        json.decodeFromString(
            remote.guideJson(
                "motocicletas/$motorcycle/traslados",
                buildJsonObject { put("destinoId", destination) }.toString(),
                "POST"
            )
        )

    override suspend fun transfers(motorcycle: Long): List<TransferRecord> =
        json.decodeFromString(remote.guideJson("motocicletas/$motorcycle/ubicacion/historial"))
}
