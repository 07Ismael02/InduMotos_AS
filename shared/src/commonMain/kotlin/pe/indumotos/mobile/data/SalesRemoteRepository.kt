package pe.indumotos.mobile.data
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
class SalesRemoteRepository(private val remote:RemoteRepository):SalesRepository {
 private val json=Json{ignoreUnknownKeys=true}
 override suspend fun stores(admin:Boolean):List<StoreSummary> = if(admin)json.decodeFromString(remote.guideJson("modos/compania",onlineOnly=true))else listOf(json.decodeFromString(remote.guideJson("modos/tienda",onlineOnly=true)))
 private suspend fun read(path:String,store:Long,page:Int)=remote.guideJson(path,params=mapOf("tiendaId" to store.toString(),"pagina" to page.toString()),onlineOnly=true)
 override suspend fun stock(store:Long,page:Int):StorePage<StockMotorcycle> = json.decodeFromString(read("modos/tienda/stock",store,page))
 override suspend fun sales(store:Long,page:Int):StorePage<StoreSale> = json.decodeFromString(read("modos/tienda/ventas",store,page))
 override suspend fun payments(store:Long,page:Int):StorePage<ManualPayment> = json.decodeFromString(read("pagos",store,page))
 override suspend fun register(store:Long,request:String,motorcycle:Long,method:String,entity:String,reference:String,amount:String,photo:GuidePhoto):ManualPayment=json.decodeFromString(remote.uploadImage("pagos",mapOf("tiendaId" to store.toString(),"solicitud" to request,"motocicletaId" to motorcycle.toString(),"metodo" to method,"entidad" to entity,"referencia" to reference,"importe" to amount),photo))
 override suspend fun confirm(id:Long,receipt:String,verified:Boolean):ManualPayment=json.decodeFromString(remote.guideJson("pagos/$id/confirmacion",buildJsonObject{put("comprobanteVenta",receipt);put("abonoVerificado",verified)}.toString(),"POST",onlineOnly=true))
 override suspend fun reject(id:Long,reason:String):ManualPayment=json.decodeFromString(remote.guideJson("pagos/$id/rechazo",buildJsonObject{put("motivo",reason)}.toString(),"POST",onlineOnly=true))
 override suspend fun receipt(id:Long)=remote.guideBytes("pagos/$id/comprobante")
 override suspend fun assignStore(user:Long,store:Long){remote.guideJson("usuarios/$user/tienda",buildJsonObject{put("tiendaId",store)}.toString(),"PUT",onlineOnly=true)}
}
