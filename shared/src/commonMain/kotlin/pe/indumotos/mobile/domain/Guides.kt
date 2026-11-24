package pe.indumotos.mobile.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable data class Arrival(val id:Long,val numeroGuia:String,val fechaLlegada:String,
    val cantidadDeclarada:Int,val cantidadRecibida:Int,val receptorUsuario:String="",
    val observacion:String?=null,val estadoRegistro:String="PENDIENTE")
@Serializable data class ArrivalPage(val items:List<Arrival>,val pagina:Int,val totalPaginas:Int,val totalElementos:Long)
@Serializable data class ArrivalDetail(val llegada:Arrival,val fichas:List<JsonObject>,val cantidadFichas:Int,val diferenciaInformativa:Int)
@Serializable data class GuideImage(val id:Long,val orden:Int,val nombreArchivo:String,val tipoMime:String,val tamano:Int)
data class GuidePhoto(val name:String,val mime:String,val bytes:ByteArray)
data class PhotoSelection(val photos:List<GuidePhoto> = emptyList(),val error:String?=null)
@Serializable data class MotorcycleDraft(val posicion:Int,val marca:String="",val modelo:String="",val color:String="",
    val chasisCompleto:String="",val numeroMotor:String="",val dua:String="",val itemDua:String="")
@Serializable data class GuideRegistration(val terminado:Boolean,val recibidas:Int,val registradas:Int,val borradores:List<MotorcycleDraft>,val version:String?=null) {
    val pending get()=(recibidas-registradas).coerceAtLeast(0)
}
@Serializable data class OcrSuggestion(val pagina:Int=1,val marca:String="",val modelo:String="",val color:String="",
    val chasisCompleto:String="",val numeroMotor:String="",val dua:String="",val itemDua:String="")
@Serializable data class OcrResult(val fotosAnalizadas:Int,val sugerencias:List<OcrSuggestion>)
interface GuideRepository {
    suspend fun localDrafts(id:Long):List<LocalDraftView> = emptyList()
    suspend fun resolveLocal(id:Long,position:Int,keepLocal:Boolean){error("No hay borradores locales")}
    suspend fun conditionalSave(id:Long,draft:MotorcycleDraft,version:String):GuideRegistration=error("El repositorio no permite escritura condicional")
    suspend fun detail(id:Long):ArrivalDetail
    suspend fun arrivals(guide:String,state:String,page:Int):ArrivalPage
    suspend fun register(guide:String,declared:Int,received:Int,note:String,photos:List<GuidePhoto>):Arrival
    suspend fun images(id:Long):List<GuideImage>
    suspend fun image(id:Long,image:Long):ByteArray
    suspend fun registration(id:Long):GuideRegistration
    suspend fun saveDraft(id:Long,draft:MotorcycleDraft):GuideRegistration
    suspend fun finish(id:Long):GuideRegistration
    suspend fun ocr(id:Long):OcrResult
}
data class LocalDraftView(val draft:MotorcycleDraft,val state:String,val detail:String?)
object GuideRules {
    fun photo(photo:GuidePhoto) {
        require(photo.mime in listOf("image/jpeg","image/png") && photo.bytes.isNotEmpty() && photo.bytes.size<=5*1024*1024) { "Selecciona fotos JPG/PNG de hasta 5 MB." }
    }
    fun arrival(guide:String,declared:Int,received:Int,photos:List<GuidePhoto>) {
        require(guide.isNotBlank()&&guide.length<=120&&declared>0&&received>0) { "Completa la guía y cantidades enteras positivas." }
        require(photos.size in 1..3) { "Selecciona entre una y tres fotos." };photos.forEach(::photo)
    }
    fun draft(d:MotorcycleDraft,options:List<CatalogOption>) {
        require(listOf(d.marca,d.modelo,d.color,d.chasisCompleto,d.numeroMotor,d.dua,d.itemDua).all{it.isNotBlank()&&it.length<=120}) { "Completa todos los campos de la motocicleta (máximo 120 caracteres)." }
        require(options.containsCombination(d.marca,d.modelo,d.color)) { "Selecciona una combinación del catálogo." }
    }
    fun canFinish(registration:GuideRegistration,drafts:List<MotorcycleDraft>) = !registration.terminado &&
        registration.borradores.size==registration.pending && drafts.size==registration.pending &&
        drafts.all { d -> registration.borradores.any { it==d } } &&
        drafts.map{it.posicion}.sorted()==(1..registration.pending).toList()
}
