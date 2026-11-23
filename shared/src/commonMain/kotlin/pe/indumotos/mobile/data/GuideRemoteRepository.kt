package pe.indumotos.mobile.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.indumotos.mobile.domain.*

class GuideRemoteRepository(private val remote:RemoteRepository):GuideRepository {
    private val json=Json{ignoreUnknownKeys=true;encodeDefaults=true}
    override suspend fun detail(id:Long):ArrivalDetail=json.decodeFromString(remote.guideJson("llegadas/$id/detalle"))
    override suspend fun arrivals(guide:String,state:String,page:Int):ArrivalPage = json.decodeFromString(remote.guideJson("llegadas/pagina",params=mapOf("numeroGuia" to guide,"estado" to state,"pagina" to page.toString())))
    override suspend fun register(guide:String,declared:Int,received:Int,note:String,photos:List<GuidePhoto>):Arrival =
        json.decodeFromString(remote.uploadGuide(guide,declared,received,note,photos))
    override suspend fun images(id:Long):List<GuideImage> = json.decodeFromString(remote.guideJson("llegadas/$id/guias"))
    override suspend fun image(id:Long,image:Long):ByteArray = remote.guideBytes("llegadas/$id/guias/$image")
    override suspend fun registration(id:Long):GuideRegistration = json.decodeFromString(remote.guideJson("llegadas/$id/registro"))
    override suspend fun saveDraft(id:Long,draft:MotorcycleDraft):GuideRegistration = json.decodeFromString(remote.guideJson("llegadas/$id/registro/borradores/${draft.posicion}",json.encodeToString(draft),"PUT"))
    override suspend fun finish(id:Long):GuideRegistration = json.decodeFromString(remote.guideJson("llegadas/$id/registro/finalizar","{}","POST"))
    override suspend fun ocr(id:Long):OcrResult = json.decodeFromString(remote.guideJson("llegadas/$id/sugerencias-ocr"))
}
