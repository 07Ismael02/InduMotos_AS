package pe.indumotos.mobile.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

/** Consulta del catálogo; su gestión se hace en la web. */
class CatalogRemoteRepository(private val remote: RemoteRepository) : CatalogRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun catalog(): List<CatalogOption> =
        json.decodeFromString(remote.guideJson("catalogo-motocicletas"))

    override suspend fun requestOptions(): List<CatalogOption> =
        json.decodeFromString(remote.guideJson("solicitudes/opciones-motocicleta"))
}
