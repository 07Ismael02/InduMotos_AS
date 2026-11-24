package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable

const val READ_REQUESTS = "SOLICITUDES_CONSULTAR"
const val CREATE_REQUESTS = "SOLICITUDES_CREAR"
const val ADVANCE_REQUESTS = "SOLICITUDES_AVANZAR"
const val ASSIGN_UNITS = "UNIDADES_ASIGNAR"
const val CANCEL_REQUESTS = "SOLICITUDES_ANULAR"
const val TRANSFER_UNITS = "TRASLADOS_REGISTRAR"

@Serializable
data class RequestLine(
    val id: Long = 0,
    val marca: String,
    val modelo: String,
    val color: String,
    val cantidad: Int
)

@Serializable
data class NewRequestLine(
    val marca: String,
    val modelo: String,
    val color: String,
    val cantidad: Int
)

@Serializable
data class NewRequest(
    val destinoId: Long,
    val observacion: String,
    val lineas: List<NewRequestLine>
)

@Serializable
data class AssemblyRequest(
    val id: Long,
    val solicitante: String,
    val destinoId: Long,
    val creadaEn: String,
    val observacion: String? = null,
    val estado: String,
    val lineas: List<RequestLine>,
    val destinoNombre: String = ""
)

@Serializable
data class RequestEvent(
    val id: Long,
    val estado: String,
    val registrador: String,
    val fecha: String,
    val motivo: String? = null
)

@Serializable
data class AssignedUnit(
    val asociacionId: Long,
    val motocicletaId: Long,
    val chasis: String,
    val asignador: String,
    val fecha: String
)

@Serializable
data class EligibleUnit(
    val motocicletaId: Long,
    val chasis: String,
    val marca: String,
    val modelo: String,
    val color: String
)

@Serializable
data class TransferRecord(
    val id: Long,
    val motocicletaId: Long,
    val origenId: Long? = null,
    val destinoId: Long,
    val destinoNombre: String,
    val tipo: String,
    val registrador: String,
    val fecha: String
)

interface OperationsRepository {
    suspend fun requests(page: Int): List<AssemblyRequest>
    suspend fun request(id: Long): AssemblyRequest
    suspend fun createRequest(body: NewRequest): AssemblyRequest
    suspend fun events(id: Long): List<RequestEvent>
    suspend fun advance(id: Long, status: String, reason: String)
    suspend fun cancel(id: Long, reason: String)
    suspend fun eligible(chassis: String): List<EligibleUnit>
    suspend fun assigned(id: Long, line: Long): List<AssignedUnit>
    suspend fun assign(id: Long, line: Long, motorcycle: Long)
    suspend fun remove(id: Long, line: Long, association: Long, reason: String)
    suspend fun transfer(motorcycle: Long, destination: Long): TransferRecord
    suspend fun transfers(motorcycle: Long): List<TransferRecord>
}

object OperationRules {
    fun transitions(state: String) = when (state) {
        "PENDIENTE" -> listOf("EN_PREPARACION", "RECHAZADA")
        "EN_PREPARACION" -> listOf("ATENDIDA", "RECHAZADA")
        else -> emptyList()
    }

    fun validate(request: NewRequest) {
        require(request.destinoId > 0) { "Selecciona una sede de destino." }
        require(request.lineas.isNotEmpty()) { "Agrega al menos una línea." }
        require(request.observacion.length <= 1000) { "La observación admite hasta 1000 caracteres." }
        request.lineas.forEachIndexed { i, line ->
            require(
                listOf(
                    line.marca,
                    line.modelo,
                    line.color
                ).all { it.isNotBlank() && it.length <= 120 } && line.cantidad > 0) {
                "Línea ${i + 1}: completa marca, modelo y color (máximo 120) y usa una cantidad entera positiva."
            }
        }
    }

    fun reason(value: String) {
        require(value.isNotBlank() && value.length <= 1000) { "Ingresa un motivo de hasta 1000 caracteres." }
    }
}

fun statusLabel(value: String) = when (value) {
    "PENDIENTE" -> "Pendiente";
    "EN_PREPARACION" -> "En preparación";
    "ATENDIDA" -> "Atendida"
    "RECHAZADA" -> "Rechazada";
    "ANULADA" -> "Anulada";
    else -> value
}
