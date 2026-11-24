package pe.indumotos.mobile.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

class GuideRemoteRepository(
    private val remote: RemoteRepository,
    private val onlineOnly: Boolean = false
) : GuideRepository {
    private val json = Json {
        ignoreUnknownKeys = true;
        encodeDefaults = true
    }

    private suspend fun request(
        path: String,
        body: String? = null,
        method: String = "GET",
        params: Map<String, String> = emptyMap()
    ) = remote.guideJson(path, body, method, params, onlineOnly)

    override suspend fun detail(id: Long): ArrivalDetail =
        json.decodeFromString(request("llegadas/$id/detalle"))

    override suspend fun arrivals(guide: String, state: String, page: Int): ArrivalPage =
        json.decodeFromString(
            request(
                "llegadas/pagina",
                params = mapOf(
                    "numeroGuia" to guide,
                    "estado" to state,
                    "pagina" to page.toString()
                )
            )
        )

    override suspend fun register(
        guide: String,
        declared: Int,
        received: Int,
        note: String,
        photos: List<GuidePhoto>
    ): Arrival =
        json.decodeFromString(remote.uploadGuide(guide, declared, received, note, photos))

    override suspend fun images(id: Long): List<GuideImage> =
        json.decodeFromString(request("llegadas/$id/guias"))

    override suspend fun image(id: Long, image: Long): ByteArray =
        remote.guideBytes("llegadas/$id/guias/$image")

    override suspend fun registration(id: Long): GuideRegistration =
        json.decodeFromString(request("llegadas/$id/registro"))

    override suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration =
        json.decodeFromString(
            request(
                "llegadas/$id/registro/borradores/${draft.posicion}",
                json.encodeToString(draft),
                "PUT"
            )
        )

    override suspend fun conditionalSave(
        id: Long,
        draft: MotorcycleDraft,
        version: String
    ): GuideRegistration = json.decodeFromString(
        remote.guideJson(
            "llegadas/$id/registro/borradores/${draft.posicion}",
            json.encodeToString(draft),
            "PUT",
            onlineOnly = true,
            version = version
        )
    )

    override suspend fun finish(id: Long): GuideRegistration =
        json.decodeFromString(request("llegadas/$id/registro/finalizar", "{}", "POST"))

    override suspend fun ocr(id: Long): OcrResult =
        json.decodeFromString(request("llegadas/$id/sugerencias-ocr"))

    override suspend fun warehouses(): List<Site> =
        json.decodeFromString<List<Site>>(remote.guideJson("ubicaciones", onlineOnly = true))
            .filter { it.tipo == "ALMACEN" && it.activo }

    override suspend fun finishAt(id: Long, warehouse: Long): GuideRegistration =
        json.decodeFromString(
            request(
                "llegadas/$id/registro/finalizar",
                buildJsonObject { put("almacenId", warehouse) }.toString(),
                "POST"
            )
        )
}
