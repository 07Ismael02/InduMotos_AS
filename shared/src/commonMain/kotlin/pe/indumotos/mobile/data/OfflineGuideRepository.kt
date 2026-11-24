package pe.indumotos.mobile.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import pe.indumotos.mobile.domain.*

class OfflineGuideRepository(
    private val read: GuideRepository,
    private val server: GuideRepository,
    private val store: OfflineStore,
    private val session: OfflineSession,
    private val sync: DraftSynchronizer
) : GuideRepository by read {
    private val json = Json {
        encodeDefaults = true;
        ignoreUnknownKeys = true
    }

    override suspend fun arrivals(guide: String, state: String, page: Int): ArrivalPage {
        try {
            return read.arrivals(guide, state, page)
        } catch (e: AppException) {
            if (e.error != AppError.Network) throw e
            val snapshots =
                store.all(session.owner(), "http").filter { it.key.startsWith("llegadas/pagina|") }
                    .mapNotNull { runCatching { json.decodeFromString<ArrivalPage>(it.payload) }.getOrNull() }
            if (snapshots.isEmpty()) throw e
            val matches = snapshots.flatMap { it.items }.distinctBy { it.id }.filter {
                it.numeroGuia.contains(
                    guide.trim(),
                    true
                ) && (state.isBlank() || it.estadoRegistro == state)
            }.sortedByDescending { it.id }
            return ArrivalPage(
                matches.drop(page * 10).take(10),
                page,
                (matches.size + 9) / 10,
                matches.size.toLong()
            )
        }
    }

    override suspend fun localDrafts(id: Long) =
        store.drafts(session.owner()).filter { it.guideId == id }.map {
            LocalDraftView(
                json.decodeFromString<MotorcycleDraft>(it.payload),
                it.state,
                it.detail
            )
        }

    override suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration {
        val owner = session.owner()
        val key = OfflineReads.key("llegadas/$id/registro", emptyMap())
        val current = store.get(owner, "http", key)
            ?.let { json.decodeFromString<GuideRegistration>(it.payload) } ?: read.registration(id)
        require(!current.terminado && draft.posicion in 1..current.pending) { "La guía ya no admite este borrador." }
        val baseline = current.borradores.firstOrNull { it.posicion == draft.posicion }
            ?.let { json.encodeToString(it) }
        store.edit(owner, id, draft, baseline, "PENDING");
        session.refresh()
        if (!session.readOnly()) sync.synchronize()
        return read.registration(id)
    }

    override suspend fun warehouses(): List<Site> = server.warehouses()
    override suspend fun finishAt(id: Long, warehouse: Long): GuideRegistration {
        require(localDrafts(id).isEmpty()) { "Primero sincroniza o resuelve los borradores guardados en el teléfono." }
        if (session.status.value.offline) throw AppException(AppError.Network)
        return server.finishAt(id, warehouse)
    }

    override suspend fun finish(id: Long): GuideRegistration {
        require(localDrafts(id).isEmpty()) { "Primero sincroniza o resuelve los borradores guardados en el teléfono." }
        if (session.status.value.offline) throw AppException(AppError.Network)
        return server.finish(id)
    }

    override suspend fun resolveLocal(id: Long, position: Int, keepLocal: Boolean) {
        val row = store.drafts(session.owner())
            .firstOrNull { it.guideId == id && it.position == position } ?: return
        if (!keepLocal) {
            store.acknowledge(row, row.payload);
            session.refresh();
            return
        }
        if (session.readOnly()) throw AppException(AppError.Unauthorized)
        val current = server.registration(id)
        require(!current.terminado && position in 1..current.pending) { "La guía está finalizada o cambió. Conserva los datos locales y consulta al responsable." }
        val baseline = current.borradores.firstOrNull { it.posicion == position }
            ?.let { json.encodeToString(it) }
        store.rebase(row, baseline);
        sync.synchronize();
        session.refresh()
    }
}
