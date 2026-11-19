package pe.indumotos.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.indumotos.mobile.domain.*

data class OperationsState(
    val busy: Boolean = false, val error: String? = null, val message: String? = null,
    val expired: Boolean = false, val page: Int = 0,
    val requests: List<AssemblyRequest> = emptyList(), val selected: AssemblyRequest? = null,
    val events: List<RequestEvent> = emptyList(), val sites: List<Site> = emptyList(),
    val line: RequestLine? = null, val assigned: List<AssignedUnit> = emptyList(),
    val eligible: List<EligibleUnit> = emptyList(), val movements: List<TransferRecord> = emptyList()
)

class OperationsViewModel(private val repo: OperationsRepository, private val locations: LocationRepository) : ViewModel() {
    private val mutable = MutableStateFlow(OperationsState())
    val state = mutable.asStateFlow()
    private var job: Job? = null
    fun reset() { job?.cancel(); mutable.value = OperationsState() }
    private fun work(writing: Boolean = false, action: suspend () -> Unit) {
        if (mutable.value.busy) return
        mutable.update { it.copy(busy = true, error = null, message = null) }
        job = viewModelScope.launch {
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: AppException) {
                mutable.update { it.copy(expired = e.error == AppError.Unauthorized,
                    error = if(writing && e.error == AppError.Network) "No se confirmó la respuesta. Actualiza y comprueba el registro antes de repetir el envío." else e.message) }
            } catch(e: Exception) { mutable.update { it.copy(error = e.message ?: "No se pudo completar la operación.") } }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    fun list(page: Int = 0) = work {
        require(page >= 0)
        val rows = repo.requests(page)
        mutable.update { it.copy(requests = rows, page = page, selected = null, line = null, events = emptyList(), eligible = emptyList(), assigned = emptyList()) }
    }
    fun sites() = work { val rows=locations.sites(); mutable.update { it.copy(sites=rows) } }
    private suspend fun refresh(id: Long) {
        val request=repo.request(id)
        val events=repo.events(id)
        mutable.update { it.copy(selected=request, events=events, eligible=emptyList()) }
    }
    fun select(id: Long) = work { mutable.update { it.copy(line=null, assigned=emptyList()) }; refresh(id) }
    fun create(body: NewRequest) = work(true) {
        OperationRules.validate(body)
        val saved=repo.createRequest(body)
        mutable.update { it.copy(selected=saved, events=emptyList(), line=null, message="Solicitud #${saved.id} creada y confirmada por el servidor.") }
    }
    fun advance(status: String, reason: String) = work(true) {
        val r=requireNotNull(mutable.value.selected)
        require(status in OperationRules.transitions(r.estado)) { "El estado ya no admite esa transición. Actualiza la solicitud." }
        if(status=="RECHAZADA") OperationRules.reason(reason)
        require(reason.length<=1000)
        repo.advance(r.id,status,reason.trim()); refresh(r.id)
        mutable.update { it.copy(message="Estado confirmado: ${statusLabel(status)}.", line=null, assigned=emptyList()) }
    }
    fun cancel(reason: String) = work(true) {
        val r=requireNotNull(mutable.value.selected)
        require(r.estado=="PENDIENTE") { "Solo se anulan solicitudes pendientes según el contrato actual." }
        OperationRules.reason(reason); repo.cancel(r.id,reason.trim());refresh(r.id)
        mutable.update { it.copy(message="Anulación confirmada; se conserva la solicitud y su historial.",line=null,assigned=emptyList()) }
    }
    fun line(line: RequestLine) = work {
        val r=requireNotNull(mutable.value.selected)
        val rows=repo.assigned(r.id,line.id)
        mutable.update { it.copy(line=line,assigned=rows,eligible=emptyList()) }
    }
    fun search(chassis: String) = work {
        require(chassis.isNotBlank()&&chassis.length<=120) { "Indica un chasis de hasta 120 caracteres." }
        val rows=repo.eligible(chassis.trim());mutable.update { it.copy(eligible=rows) }
    }
    fun assign(unit: EligibleUnit) = work(true) {
        val r=requireNotNull(mutable.value.selected);val line=requireNotNull(mutable.value.line)
        repo.assign(r.id,line.id,unit.motocicletaId)
        val rows=repo.assigned(r.id,line.id)
        mutable.update { it.copy(assigned=rows,eligible=emptyList(),message="Chasis ${unit.chasis} asociado a la línea #${line.id}.") }
    }
    fun remove(unit: AssignedUnit, reason: String) = work(true) {
        OperationRules.reason(reason)
        val r=requireNotNull(mutable.value.selected);val line=requireNotNull(mutable.value.line)
        repo.remove(r.id,line.id,unit.asociacionId,reason.trim())
        val rows=repo.assigned(r.id,line.id)
        mutable.update { it.copy(assigned=rows,eligible=emptyList(),message="Asignación retirada; el motivo queda registrado.") }
    }
    fun movements(motorcycle: Long) = work {
        require(motorcycle>0) { "Indica un ID de motocicleta válido." }
        mutable.update { it.copy(movements=emptyList()) }
        val history=repo.transfers(motorcycle);val sites=locations.sites()
        mutable.update { it.copy(movements=history,sites=sites) }
    }
    fun transfer(motorcycle: Long, destination: Long) = work(true) {
        require(motorcycle>0&&destination>0) { "Selecciona la motocicleta y el destino." }
        val saved=repo.transfer(motorcycle,destination)
        val rows=repo.transfers(motorcycle)
        mutable.update { it.copy(movements=rows,message="Traslado #${saved.id} confirmado a ${saved.destinoNombre}.") }
    }
}
