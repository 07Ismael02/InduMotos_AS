package pe.indumotos.mobile.domain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
@Serializable data class StoreSummary(val id:Long,val nombre:String,val disponibles:Long=0,val vendidas:Long=0)
@Serializable data class StockMotorcycle(val id:Long,val chasis:String,val marca:String,val modelo:String,val color:String)
@Serializable data class StorePage<T>(val items:List<T>,val pagina:Int,val totalElementos:Long){val pages get()=((totalElementos+9)/10).toInt()}
@Serializable data class StoreSale(val id:Long,val chasis:String,val referenciaPago:String?=null,val referencia:String,val importe:JsonPrimitive,val registrador:String,val fecha:String)
@Serializable data class ManualPayment(val id:Long,val motocicletaId:Long,val tiendaId:Long,val chasis:String,val metodo:String,val entidad:String,val referencia:String,val importe:JsonPrimitive,val estado:String,val motivo:String?=null,val referenciaVenta:String?=null,val ventaId:Long?=null,val registrador:String,val revisor:String?=null,val fecha:String)
interface SalesRepository {
 suspend fun stores(admin:Boolean):List<StoreSummary>
 suspend fun stock(store:Long,page:Int):StorePage<StockMotorcycle>
 suspend fun sales(store:Long,page:Int):StorePage<StoreSale>
 suspend fun payments(store:Long,page:Int):StorePage<ManualPayment>
 suspend fun register(store:Long,request:String,motorcycle:Long,method:String,entity:String,reference:String,amount:String,photo:GuidePhoto):ManualPayment
 suspend fun confirm(id:Long,receipt:String,verified:Boolean):ManualPayment
 suspend fun reject(id:Long,reason:String):ManualPayment
 suspend fun receipt(id:Long):ByteArray
 suspend fun assignStore(user:Long,store:Long)
}
