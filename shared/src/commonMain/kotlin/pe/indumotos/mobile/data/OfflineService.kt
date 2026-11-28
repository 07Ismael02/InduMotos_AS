package pe.indumotos.mobile.data

import pe.indumotos.mobile.domain.*
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class OfflineService(
    private val remote: RemoteRepository,
    private val guides: GuideRepository,
    private val session: OfflineSession,
    private val sync: DraftSynchronizer,
    private val vehicles: VehicleRepository? = null,
    private val catalog: CatalogRepository = CatalogRemoteRepository(remote)
) : OfflineWork {
    override val status = session.status
    override suspend fun availableProfiles(): List<OfflineProfile> =
        session.availableProfiles().filter { session.store.all(it.owner, "http").isNotEmpty() }

    override suspend fun resumeReadOnly(profile: OfflineProfile): Access {
        remote.restoreOrigin(profile.origin);
        return session.resume(profile)
    }

    override suspend fun synchronize() {
        sync.synchronize()
    }

    override suspend fun downloadInventory() {
        if (session.readOnly()) throw AppException(AppError.Unauthorized)
        val current = requireNotNull(session.current);

        val access = remote.profile();
        session.online(current.origin, access)
        require(access.permisos.any { it.codigo == "INVENTARIO_CONSULTAR" && it.permitido }) { "No tienes permiso de inventario." }
        session.syncing(true)
        try {
            if (access.permisos.any {
                    it.codigo in listOf(
                        "FICHAS_REGISTRAR",
                        "FICHAS_EDITAR"
                    ) && it.permitido
                }) catalog.catalog()
            var page = 0
            do {
                val result = remote.inventory(InventoryFilter(), page)
                if (session.status.value.offline) throw AppException(AppError.Network)
                for (row in result.items) {
                    val id = row["id"]!!.jsonPrimitive.long;
                    remote.detail(Module.FICHAS, id)
                    vehicles?.let { v ->
                        val card = v.card(id);
                        for (photo in card.fotos) v.photo(
                            id,
                            photo.id
                        );
                        if (access.permisos.any { it.codigo == "DISPONIBILIDAD_CONSULTAR" && it.permitido }) v.availability(
                            id
                        );
                        if (access.permisos.any { it.codigo == READ_LOCATIONS && it.permitido }) v.journey(
                            id
                        )
                    }
                    if (session.status.value.offline) throw AppException(AppError.Network)
                }
                page++
            } while (page < result.totalPaginas)
            for (filter in listOf("PENDIENTE", "CONFIRMADA")) {
                page = 0
                do {
                    val result = guides.arrivals("", filter, page)
                    if (session.status.value.offline) throw AppException(AppError.Network)
                    for (arrival in result.items) {
                        guides.detail(arrival.id);
                        guides.registration(arrival.id)
                        for (image in guides.images(arrival.id)) {
                            guides.image(
                                arrival.id,
                                image.id
                            );
                            if (session.status.value.offline) throw AppException(AppError.Network)
                        }
                    }
                    page++
                } while (page < result.totalPaginas)
            }
            session.message("Descarga local completada para esta cuenta. La disponibilidad actual se confirma online.")
        } finally {
            session.syncing(false);
            session.refresh()
        }
    }
}
