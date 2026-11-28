package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Arrival(
    val id: Long, val numeroGuia: String, val fechaLlegada: String,
    val cantidadDeclarada: Int, val cantidadRecibida: Int, val receptorUsuario: String = "",
    val observacion: String? = null, val estadoRegistro: String = "PENDIENTE"
)

@Serializable
data class ArrivalPage(
    val items: List<Arrival>,
    val pagina: Int,
    val totalPaginas: Int,
    val totalElementos: Long
)

@Serializable
data class ArrivalDetail(
    val llegada: Arrival,
    val fichas: List<JsonObject>,
    val cantidadFichas: Int,
    val diferenciaInformativa: Int
)

@Serializable
data class GuideImage(
    val id: Long,
    val orden: Int,
    val nombreArchivo: String,
    val tipoMime: String,
    val tamano: Int
)

data class GuidePhoto(val name: String, val mime: String, val bytes: ByteArray)
data class PhotoSelection(val photos: List<GuidePhoto> = emptyList(), val error: String? = null)

fun Access.canRegisterGuideDrafts() = listOf(
    "FICHAS_REGISTRAR",
    "INVENTARIO_CONSULTAR"
).all { code -> permisos.any { it.codigo == code && it.permitido } }

@Serializable
data class MotorcycleDraft(
    val posicion: Int,
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val chasisCompleto: String = "",
    val numeroMotor: String = "",
    val dua: String = "",
    val itemDua: String = ""
)

@Serializable
data class GuideRegistration(
    val terminado: Boolean,
    val recibidas: Int,
    val registradas: Int,
    val borradores: List<MotorcycleDraft>,
    val version: String? = null
) {
    val pending get() = (recibidas - registradas).coerceAtLeast(0)
}

@Serializable
data class OcrSuggestion(
    val pagina: Int = 1,
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val chasisCompleto: String = "",
    val numeroMotor: String = "",
    val dua: String = "",
    val itemDua: String = ""
)

@Serializable
data class OcrResult(val fotosAnalizadas: Int, val sugerencias: List<OcrSuggestion>)
interface GuideRepository {
    suspend fun localDrafts(id: Long): List<LocalDraftView> = emptyList()
    suspend fun resolveLocal(id: Long, position: Int, keepLocal: Boolean) {
        error("No hay borradores locales")
    }

    suspend fun conditionalSave(
        id: Long,
        draft: MotorcycleDraft,
        version: String
    ): GuideRegistration = error("El repositorio no permite escritura condicional")

    suspend fun detail(id: Long): ArrivalDetail
    suspend fun arrivals(guide: String, state: String, page: Int): ArrivalPage
    suspend fun register(
        guide: String,
        declared: Int,
        received: Int,
        note: String,
        photos: List<GuidePhoto>
    ): Arrival

    suspend fun images(id: Long): List<GuideImage>
    suspend fun image(id: Long, image: Long): ByteArray
    suspend fun registration(id: Long): GuideRegistration
    suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration
    suspend fun warehouses(): List<Site> = emptyList()
    suspend fun finishAt(id: Long, warehouse: Long): GuideRegistration = finish(id)
    suspend fun finish(id: Long): GuideRegistration
    suspend fun ocr(id: Long): OcrResult
}

data class LocalDraftView(val draft: MotorcycleDraft, val state: String, val detail: String?)
object GuideRules {
    fun addPhotos(current: List<GuidePhoto>, incoming: List<GuidePhoto>): List<GuidePhoto> {
        incoming.forEach(::photo)
        val result = (current + incoming).fold(emptyList<GuidePhoto>()) { list, item ->
            if (list.any {
                    it.bytes.contentEquals(item.bytes)
                }) list else list + item
        }
        require(result.size <= 3) { "Puedes adjuntar hasta tres fotografías. Quita una antes de añadir otra." }
        return result
    }

    fun photo(photo: GuidePhoto) {
        require(
            photo.mime in listOf(
                "image/jpeg",
                "image/png"
            ) && photo.bytes.isNotEmpty() && photo.bytes.size <= 5 * 1024 * 1024
        ) { "Selecciona fotos JPG/PNG de hasta 5 MB." }
    }

    // Guía de remisión SUNAT: serie de 4 caracteres (T001, EG07, 0001), guion y correlativo.
    private val guideNumberPattern = Regex("""([A-Z0-9]{4})-(\d{1,8})""")
    const val GUIDE_NUMBER_MAX = 13

    /** Devuelve la guía como SERIE-00000000 o rechaza un formato distinto al de SUNAT. */
    fun guideNumber(input: String): String {
        val match = guideNumberPattern.matchEntire(input.trim().uppercase())
            ?: throw IllegalArgumentException("Ingresa la guía como serie-número, por ejemplo T001-00012345.")
        val (series, number) = match.destructured
        require(number.trimStart('0').isNotEmpty()) { "El número de la guía debe ser mayor que cero." }
        return "$series-${number.padStart(8, '0')}"
    }

    /** Al escribir: serie de hasta 4 letras o dígitos, un guion y luego solo dígitos. */
    fun guideNumberTyping(input: String): String {
        val text = input.uppercase()
        val dash = text.indexOf('-')
        val series = (if (dash < 0) text else text.substring(0, dash))
            .filter { it in 'A'..'Z' || it.isDigit() }.take(4)
        if (dash < 0) return series
        val number = text.substring(dash + 1).filter { it.isDigit() }.take(8)
        return "$series-$number".take(GUIDE_NUMBER_MAX)
    }

    fun arrival(guide: String, declared: Int, received: Int, photos: List<GuidePhoto>) {
        guideNumber(guide)
        require(declared > 0 && received > 0) { "Ingresa cantidades enteras positivas." }
        require(photos.size in 1..3) { "Selecciona entre una y tres fotos." };
        photos.forEach(::photo)
    }

    /** Una guía no puede repetir chasis ni número de motor entre sus motocicletas. */
    fun uniqueInGuide(d: MotorcycleDraft, others: List<MotorcycleDraft>) {
        fun key(value: String) = value.trim().uppercase()
        val rest = others.filter { it.posicion != d.posicion }
        require(rest.none { it.chasisCompleto.isNotBlank() && key(it.chasisCompleto) == key(d.chasisCompleto) }) {
            "El chasis ${d.chasisCompleto.trim()} ya está en otra moto de esta guía."
        }
        require(rest.none { it.numeroMotor.isNotBlank() && key(it.numeroMotor) == key(d.numeroMotor) }) {
            "El número de motor ${d.numeroMotor.trim()} ya está en otra moto de esta guía."
        }
    }

    fun draft(d: MotorcycleDraft, options: List<CatalogOption>) {
        require(
            listOf(
                d.marca,
                d.modelo,
                d.color,
                d.chasisCompleto,
                d.numeroMotor,
                d.dua,
                d.itemDua
            ).all { it.isNotBlank() && it.length <= 120 }) { "Completa todos los campos de la motocicleta (máximo 120 caracteres)." }
        require(
            options.containsCombination(
                d.marca,
                d.modelo,
                d.color
            )
        ) { "Selecciona una combinación del catálogo." }
    }

    fun canFinish(registration: GuideRegistration, drafts: List<MotorcycleDraft>) =
        !registration.terminado &&
                registration.borradores.size == registration.pending && drafts.size == registration.pending &&
                drafts.all { d -> registration.borradores.any { it == d } } &&
                drafts.map { it.posicion }.sorted() == (1..registration.pending).toList()
}
