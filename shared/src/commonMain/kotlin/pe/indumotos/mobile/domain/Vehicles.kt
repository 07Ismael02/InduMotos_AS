package pe.indumotos.mobile.domain
import kotlinx.serialization.Serializable
@Serializable data class VehicleSpecs(val anio:Int?=null,val cilindrada:Int?=null,val transmision:String?=null,val combustible:String?=null,val descripcion:String?=null,val version:Long=0)
@Serializable data class VehiclePhoto(val id:Long,val mime:String,val fecha:String)
@Serializable data class VehicleCard(val datos:VehicleSpecs=VehicleSpecs(),val fotos:List<VehiclePhoto> = emptyList())
@Serializable data class VehicleAvailability(val id:Long,val chasis:String,val marca:String,val modelo:String,val color:String,val ubicacion:String?=null,val estado:String,val motivo:String,val actualizadoEn:String?=null,val registradoPor:String?=null,val puedeOfrecerse:Boolean=false)
interface VehicleRepository {
 suspend fun card(id:Long):VehicleCard
 suspend fun save(id:Long,specs:VehicleSpecs):VehicleCard
 suspend fun photo(id:Long,photo:Long):ByteArray
 suspend fun addPhoto(id:Long,photo:GuidePhoto):VehicleCard
 suspend fun availability(id:Long):VehicleAvailability
 suspend fun changeAvailability(id:Long,state:String,reason:String):VehicleAvailability
}
