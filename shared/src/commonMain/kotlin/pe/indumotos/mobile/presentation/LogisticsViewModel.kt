package pe.indumotos.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

data class UiState(
    val access: Access? = null,
    val module: Module? = null,
    val rows: List<JsonObject> = emptyList(),
    val page: Int = 0,
    val pages: Int = 0,
    val total: Long = 0,
    val filter: InventoryFilter = InventoryFilter(),
    val busy: Boolean = false,
    val editing: Boolean = false,
    val editOptions: List<CatalogOption> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    val form: Form? = null,
    val detail: JsonObject? = null,
    val offlineProfiles: List<OfflineProfile> = emptyList()
)

class LogisticsViewModel(
    private val repo: LogisticsRepository,
    private val save: LogisticsUseCase,
    private val locations: LocationRepository,
    private val catalog: CatalogRepository? = null,
    private val guides: GuideRepository? = null,
    private val offline: OfflineWork? = null
) : ViewModel() {
    private val mutableLocation = MutableStateFlow<MotorcycleLocation?>(null)
    val location = mutableLocation.asStateFlow()
    val offlineStatus = offline?.status ?: MutableStateFlow(OfflineStatus()).asStateFlow()
    private val mutable = MutableStateFlow(UiState());

    val state = mutable.asStateFlow()
    private fun work(block: suspend () -> Unit) {
        if (mutable.value.busy) return;
        mutable.update {
            it.copy(
                busy = true,
                error = null,
                message = null
            )
        };
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                if (e.error == AppError.Unauthorized) {
                    repo.clearSession();
                    mutable.value =
                        UiState(error = if (mutable.value.access == null) "Usuario o contraseña incorrectos, o cuenta inactiva." else "Tu sesión venció. Inicia sesión nuevamente.")
                } else mutable.update { it.copy(error = e.message) }
            } catch (e: Exception) {
                mutable.update { it.copy(error = e.message ?: "No se pudo completar la operación") }
            } finally {
                mutable.update { it.copy(busy = false) }
            }
        }
    }

    fun login(url: String, user: String, password: String) = work {
        val a = repo.login(url, user, password);
        mutable.value = UiState(access = a, busy = true)
    }

    fun findOffline(origin: String) = work {
        val p = offline?.availableProfiles().orEmpty()
            .filter { it.origin == origin.trimEnd('/') };
        mutable.update { it.copy(offlineProfiles = p) }
    }

    fun openOffline(profile: OfflineProfile) = work {
        val access = requireNotNull(offline).resumeReadOnly(profile);
        mutable.value = UiState(
            access = access,
            busy = true,
            message = "Consulta de datos guardados en este teléfono."
        )
    }

    fun offlineError(message: String) {
        mutable.update { it.copy(error = message) }
    }

    fun synchronize() = work { offline?.synchronize() }
    fun downloadOffline() = work { offline?.downloadInventory() }
    fun reconnect() {
        if (!state.value.busy) {
            repo.clearSession();
            mutable.value =
                UiState(message = "Inicia sesión online para sincronizar tus borradores.")
        }
    }

    fun refreshConnection() {
        if (state.value.busy || offlineStatus.value.syncing || offlineStatus.value.readOnlySession || state.value.access == null) return;
        viewModelScope.launch {
            try {
                offline?.synchronize()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                if (e.error == AppError.Unauthorized) expireSession()
            }
        }
    }

    fun open(module: Module?) {
        if (mutable.value.busy) return;
        if (module != null && !canRead(module)) return;
        mutable.update {
            it.copy(
                module = module,
                editing = false,
                editOptions = emptyList(),
                rows = emptyList(),
                detail = null,
                error = null,
                message = null,
                filter = InventoryFilter(),
                page = 0,
                pages = 0,
                total = 0
            )
        };
        if (module == Module.INVENTORY) work { loadRecent() }
    }

    private suspend fun loadRecent() {
        val first = repo.inventory(InventoryFilter(), 0);

        val last = if (first.totalPaginas > 1) repo.inventory(
            InventoryFilter(),
            first.totalPaginas - 1
        ) else first;
        mutable.update {
            it.copy(
                rows = last.items.reversed(),
                page = last.pagina,
                pages = last.totalPaginas,
                total = last.totalElementos
            )
        }
    }

    fun arrivals(guide: String) = work {
        val rows = repo.arrivals(guide);
        mutable.update {
            it.copy(
                rows = rows,
                detail = null
            )
        }
    }

    private suspend fun load(page: Int) {
        val result = repo.inventory(
            mutable.value.filter,
            page
        );
        mutable.update {
            it.copy(
                rows = result.items,
                page = result.pagina,
                pages = result.totalPaginas,
                total = result.totalElementos
            )
        }
    }

    fun search(filter: InventoryFilter) = work {
        val chasis =
            filter.chasis.trim();
        require(chasis.isBlank() || chasis.length >= 4) { "Ingresa el chasis completo o los últimos cuatro dígitos" };
        require(
            chasis.length != 4 || chasis.all { it.isDigit() }) { "El chasis corto debe tener cuatro dígitos" };
        mutable.update {
            it.copy(
                filter = filter,
                rows = emptyList(),
                detail = null,
                page = 0,
                pages = 0,
                total = 0
            )
        };
        load(0)
    }

    fun page(page: Int) {
        if (page < 0 || page >= mutable.value.pages) return;
        work { load(page) }
    }

    fun query(id: String) = work {
        require(Rules.positive(id)) { "Ingresa un ID válido" };

        val module = mutable.value.module ?: return@work;
        mutable.update { it.copy(detail = null) };

        val result = repo.detail(module, id.toLong());
        mutable.update { it.copy(detail = result) }
    }

    private suspend fun withArrival(record: JsonObject): JsonObject {
        val id = record["llegadaId"]?.jsonPrimitive?.longOrNull
        return if (id != null && guides != null) JsonObject(
            record + ("llegada" to Json.encodeToJsonElement(
                guides.detail(id).llegada
            ))
        ) else record
    }

    fun inventoryDetail(id: String) = work {
        mutableLocation.value = null;

        val result = withArrival(repo.detail(Module.FICHAS, id.toLong()));
        mutable.update {
            it.copy(
                detail = result
            )
        }
    }

    fun beginEdit() = work {
        require(allowed("FICHAS_EDITAR"));

        val options = requireNotNull(catalog).catalog();
        mutable.update {
            it.copy(
                editing = true,
                editOptions = options
            )
        }
    }

    fun cancelEdit() {
        if (!mutable.value.busy) mutable.update { it.copy(editing = false) }
    }

    fun updateMotorcycle(draft: MotorcycleDraft) = work {
        require(allowed("FICHAS_EDITAR"));
        GuideRules.draft(draft, mutable.value.editOptions)
        val id = requireNotNull(mutable.value.detail?.get("id")?.jsonPrimitive?.longOrNull)
        val body = buildJsonObject {
            put("marca", draft.marca);
            put("modelo", draft.modelo);
            put(
                "color",
                draft.color
            );
            put("chasisCompleto", draft.chasisCompleto);
            put(
                "numeroMotor",
                draft.numeroMotor
            );
            put("dua", draft.dua);
            put("itemDua", draft.itemDua)
        }
        val saved = repo.submit("motocicletas/$id", body, "PUT")
        mutable.update {
            it.copy(
                detail = saved,
                editing = false,
                message = "Ficha actualizada y confirmada por el servidor."
            )
        }
        if (saved != null) {
            val enriched = withArrival(saved);
            mutable.update { it.copy(detail = enriched) }
        }
    }

    fun closeDetail() {
        if (!mutable.value.busy) {
            mutableLocation.value = null;
            mutable.update {
                it.copy(
                    detail = null,
                    error = null,
                    message = null
                )
            }
        }
    }

    fun loadLocation(id: Long) {
        if (!allowed(READ_LOCATIONS)) return
        work {
            val current = locations.currentLocation(id)
            val sites =
                if (current == null && allowed(WRITE_LOCATION)) locations.sites() else emptyList()
            mutableLocation.value = MotorcycleLocation(current, sites)
        }
    }

    fun registerLocation(id: Long, destination: Site) {
        if (!allowed(READ_LOCATIONS) || !allowed(WRITE_LOCATION)) return
        val current = mutableLocation.value ?: return
        if (current.current != null || destination !in current.destinations) return
        work {
            val saved = locations.registerLocation(id, destination.id)
            mutableLocation.value = current.copy(current = saved)
            mutable.update { it.copy(message = "Ubicación registrada.") }
        }
    }

    fun form(form: Form?) {
        if (mutable.value.busy) return
        if (form != null && form.fields.any { it.key == "llegadaId" }) {
            work {
                val rows = repo.arrivals("")
                require(rows.isNotEmpty()) { "No hay recepciones disponibles. Registra primero una llegada con una cuenta autorizada." }
                val labels =
                    rows.associate { it["id"]!!.jsonPrimitive.content to ("Guía " + it["numeroGuia"]!!.jsonPrimitive.content + " · " + limaTimestamp(it["fechaLlegada"]!!.jsonPrimitive.content)) }
                mutable.update {
                    it.copy(form = form.copy(fields = form.fields.map { f ->
                        if (f.key == "llegadaId") f.copy(
                            label = "Recepción / guía",
                            options = labels.keys.toList(),
                            labels = labels
                        ) else f
                    }))
                }
            }
        } else mutable.update { it.copy(form = form, error = null, message = null) }
    }

    fun submit(values: Map<String, String>) {
        val form = mutable.value.form ?: return;
        work {
            val response = save.save(form, values);

            val id = response?.get("id")?.jsonPrimitive?.contentOrNull;
            mutable.update {
                it.copy(
                    form = null,
                    message = if (id != null) "Registro guardado con ID #$id." else "Operación confirmada por el servidor.",
                    detail = null
                )
            };
            if (mutable.value.module in listOf(
                    Module.INVENTORY,
                    Module.FICHAS
                )
            ) loadRecent() else if (mutable.value.module == Module.ARRIVALS && allowed("INVENTARIO_CONSULTAR")) {
                val rows = repo.arrivals("");
                mutable.update { it.copy(rows = rows) }
            }
        }
    }

    fun logout() =
        work {
            repo.logout();
            mutable.value = UiState(message = "Sesión cerrada correctamente.")
        }

    fun expireSession() {
        repo.clearSession();
        mutableLocation.value = null;
        mutable.value =
            UiState(error = "Tu sesión venció. Inicia sesión nuevamente.")
    }

    fun allowed(permission: String) =
        state.value.access?.permisos?.any { it.codigo == permission && it.permitido } == true

    fun canRead(module: Module): Boolean {
        if (offlineStatus.value.offline && module !in listOf(
                Module.INVENTORY,
                Module.ARRIVALS,
                Module.FICHAS
            )
        ) return false
        return when (module) {
            Module.ARRIVALS -> allowed("LLEGADAS_REGISTRAR") || allowed("INVENTARIO_CONSULTAR");
            Module.FICHAS -> allowed(
                "FICHAS_REGISTRAR"
            ) || allowed("INVENTARIO_CONSULTAR");
            else -> allowed(module.permission)
        }
    }
}
