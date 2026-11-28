package pe.indumotos.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.indumotos.mobile.domain.*

data class GuidesState(
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val expired: Boolean = false,
    val page: ArrivalPage? = null,
    val query: String = "",
    val filter: String = "PENDIENTE",
    val selected: Arrival? = null,
    val registration: GuideRegistration? = null,
    val detail: ArrivalDetail? = null,
    val drafts: List<MotorcycleDraft> = emptyList(),
    val options: List<CatalogOption> = emptyList(),
    val images: List<GuideImage> = emptyList(),
    val imageIndex: Int = 0,
    val imageBytes: ByteArray? = null,
    val warehouses: List<Site> = emptyList(),
    val ocr: OcrResult? = null,
    val ocrReviewed: Boolean = true,
    val local: List<LocalDraftView> = emptyList()
)

class GuidesViewModel(private val repo: GuideRepository, private val catalog: CatalogRepository) :
    ViewModel() {
    private val mutable = MutableStateFlow(GuidesState());

    val state = mutable.asStateFlow()
    private var task: Job? = null;

    private var epoch = 0
    fun reset() {
        epoch++;
        task?.cancel();
        mutable.value = GuidesState()
    }

    private fun work(writing: Boolean = false, block: suspend () -> Unit) {
        if (mutable.value.busy) return
        val current = epoch;
        mutable.update { it.copy(busy = true, error = null, message = null) }
        task = viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                if (current == epoch) mutable.update {
                    it.copy(
                        expired = e.error == AppError.Unauthorized,
                        error = if (writing && e.error == AppError.Network) "No se confirmó la respuesta. Actualiza la guía antes de repetir el envío." else e.message
                    )
                }
            } catch (e: Exception) {
                if (current == epoch) mutable.update {
                    it.copy(
                        error = e.message ?: "No se pudo completar la operación."
                    )
                }
            } finally {
                if (current == epoch) mutable.update { it.copy(busy = false) }
            }
        }
    }

    fun list(query: String = "", filter: String = "PENDIENTE", page: Int = 0) = work {
        require(page >= 0);

        val result = repo.arrivals(query.trim(), filter, page)
        mutable.update {
            it.copy(
                page = result,
                query = query,
                filter = filter,
                selected = null,
                registration = null,
                detail = null,
                drafts = emptyList(),
                images = emptyList(),
                imageBytes = null,
                ocr = null,
                local = emptyList()
            )
        }
    }

    fun register(
        guide: String,
        declared: Int,
        received: Int,
        note: String,
        photos: List<GuidePhoto>
    ) = work(true) {
        GuideRules.arrival(guide, declared, received, photos);
        require(note.length <= 1000)
        val saved = repo.register(GuideRules.guideNumber(guide), declared, received, note.trim(), photos)
        // Commit the success before any optional read: a failed reload must never suggest repeating POST.
        mutable.update {
            it.copy(
                selected = saved,
                registration = null,
                images = emptyList(),
                imageBytes = null,
                message = "Guía ${saved.numeroGuia} registrada."
            )
        }
    }

    fun select(arrival: Arrival, editing: Boolean) = work {
        mutable.update {
            it.copy(
                selected = arrival,
                registration = null,
                detail = null,
                drafts = emptyList(),
                images = emptyList(),
                imageBytes = null,
                ocr = null
            )
        }
        val detail = repo.detail(arrival.id)
        val registration = repo.registration(arrival.id)
        val images = repo.images(arrival.id)
        val options = if (editing) catalog.catalog() else emptyList()
        val local = repo.localDrafts(arrival.id)
        val drafts = (1..registration.pending).map { position ->
            local.firstOrNull { it.draft.posicion == position }?.draft
                ?: registration.borradores.firstOrNull { it.posicion == position }
                ?: MotorcycleDraft(position)
        }
        mutable.update {
            it.copy(
                registration = registration,
                detail = detail,
                drafts = drafts,
                images = images,
                options = options,
                imageIndex = 0,
                ocrReviewed = true,
                local = local
            )
        }
        if (images.isNotEmpty()) {
            val bytes = repo.image(arrival.id, images.first().id);
            mutable.update {
                it.copy(
                    imageBytes = bytes
                )
            }
        }
    }

    fun image(index: Int) = work {
        val s = mutable.value;

        val arrival = requireNotNull(s.selected);

        val image = s.images.getOrNull(index) ?: return@work
        mutable.update { it.copy(imageBytes = null, imageIndex = index) }
        val bytes = repo.image(arrival.id, image.id);
        mutable.update { it.copy(imageBytes = bytes) }
    }

    fun edit(draft: MotorcycleDraft) {
        if (!mutable.value.busy) mutable.update { it.copy(drafts = it.drafts.map { d -> if (d.posicion == draft.posicion) draft else d }) }
    }

    fun save(draft: MotorcycleDraft) = work(true) {
        val s = mutable.value;

        val arrival = requireNotNull(s.selected);
        GuideRules.draft(draft, s.options)
        GuideRules.uniqueInGuide(draft, s.drafts)
        require(s.ocrReviewed) { "Revisa las sugerencias OCR antes de guardar." }
        val normalized = draft.copy(
            chasisCompleto = draft.chasisCompleto.trim(),
            numeroMotor = draft.numeroMotor.trim(),
            dua = draft.dua.trim(),
            itemDua = draft.itemDua.trim()
        )
        try {
            val result = repo.saveDraft(arrival.id, normalized)
            val local = repo.localDrafts(arrival.id)
            mutable.update {
                it.copy(
                    registration = result,
                    local = local,
                    drafts = it.drafts.map { d -> if (d.posicion == draft.posicion) normalized else d },
                    message = if (local.any { row -> row.draft.posicion == draft.posicion }) "Borrador ${draft.posicion} guardado en el teléfono; revisa su estado de envío." else "Borrador ${draft.posicion} confirmado en el servidor."
                )
            }
        } finally {
            val local = repo.localDrafts(arrival.id);
            mutable.update { it.copy(local = local) }
        }
    }

    fun refreshLocalState() {
        if (mutable.value.selected == null) return;
        work {
            val id = requireNotNull(mutable.value.selected).id;

            val local = repo.localDrafts(id);

            val registration = repo.registration(id)
            mutable.update { it.copy(local = local, registration = registration) }
        }
    }

    fun resolveLocal(position: Int, keep: Boolean) = work {
        val id = requireNotNull(mutable.value.selected).id;
        repo.resolveLocal(id, position, keep)
        val local = repo.localDrafts(id);

        val registration = repo.registration(id)
        mutable.update {
            it.copy(
                local = local,
                registration = registration,
                drafts = it.drafts.map { d ->
                    if (d.posicion == position) local.firstOrNull { r -> r.draft.posicion == position }?.draft
                        ?: registration.borradores.firstOrNull { r -> r.posicion == position }
                        ?: MotorcycleDraft(position) else d
                })
        }
    }

    fun warehouses() =
        work {
            val rows = repo.warehouses();
            mutable.update { it.copy(warehouses = rows) }
        }

    fun finish(warehouse: Long? = null) = work(true) {
        val s = mutable.value;
        require(
            s.local.isEmpty() && s.ocrReviewed && GuideRules.canFinish(
                requireNotNull(s.registration),
                s.drafts
            )
        ) { "Sincroniza todos los bloques y revisa los datos antes de finalizar." }
        val result = if (warehouse != null) repo.finishAt(
            requireNotNull(s.selected).id,
            warehouse
        ) else repo.finish(requireNotNull(s.selected).id)
        mutable.update {
            it.copy(
                registration = result,
                drafts = emptyList(),
                selected = it.selected?.copy(estadoRegistro = "CONFIRMADA"),
                message = "Registro finalizado. La guía queda confirmada."
            )
        }
        val detail =
            repo.detail(requireNotNull(s.selected).id);
        mutable.update { it.copy(detail = detail) }
    }

    fun ocr() = work {
        val suggestions = repo.ocr(requireNotNull(mutable.value.selected).id)
        mutable.update {
            it.copy(
                ocr = suggestions,
                ocrReviewed = false,
                message = "OCR: revisa cada campo con las fotos; no se guardó ninguna motocicleta."
            )
        }
    }

    fun useSuggestion(index: Int, position: Int) {
        val s = mutable.value;

        val o = s.ocr?.sugerencias?.getOrNull(index) ?: return
        val existing = s.drafts.firstOrNull { it.posicion == position } ?: return
        // Applying a suggestion only edits an unsaved block; the user must confirm review separately.
        if (s.registration?.borradores?.any { it.posicion == position } == true) return
        edit(
            existing.copy(
                marca = o.marca,
                modelo = o.modelo,
                color = o.color,
                chasisCompleto = o.chasisCompleto,
                numeroMotor = o.numeroMotor,
                dua = o.dua,
                itemDua = o.itemDua
            )
        )
        mutable.update { it.copy(ocrReviewed = false) }
    }

    fun reviewed(value: Boolean) {
        mutable.update { it.copy(ocrReviewed = value) }
    }
}
