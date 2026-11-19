package pe.indumotos.mobile.data
import pe.indumotos.mobile.domain.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.CancellationException

class RemoteRepository(private val http:HttpClient):LogisticsRepository, LocationRepository, OperationsRepository{
 private val json=Json{ignoreUnknownKeys=true;encodeDefaults=true}
 private var base="";private var token=""
 private suspend fun request(path:String,body:String?=null,method:String="GET",params:Map<String,String> = emptyMap()):String{
  val response=try{http.request(base+path){this.method=HttpMethod.parse(method);contentType(ContentType.Application.Json);if(token.isNotEmpty())bearerAuth(token);params.forEach{(k,v)->parameter(k,v)};if(body!=null)setBody(body)}}catch(e:CancellationException){throw e}catch(e:Exception){throw AppException(AppError.Network)}
  val text=response.bodyAsText()
  if(response.status.value !in 200..299){
   if(response.status.value==401)token=""
   val message=runCatching{val o=json.parseToJsonElement(text).jsonObject;o["error"]?.jsonPrimitive?.content?:o["detail"]?.jsonPrimitive?.content}.getOrNull()
   val fields=runCatching{json.parseToJsonElement(text).jsonObject["errores"]?.jsonObject?.mapValues{it.value.jsonPrimitive.content}}.getOrNull().orEmpty()
   throw AppException(apiError(response.status.value,message,fields))
  };return text
 }
 override suspend fun login(baseUrl:String,user:String,password:String):Access{
  require(Rules.server(baseUrl)){"Ingresa una dirección HTTP o HTTPS"};base=baseUrl.trim().trimEnd('/')+"/api/v1/";token=""
  val auth=json.decodeFromString<LoginResponse>(request("auth/login",json.encodeToString(LoginRequest(user.trim(),password)),"POST"));token=auth.accessToken
  try{return json.decodeFromString<Access>(request("auth/me"))}catch(e:CancellationException){token="";throw e}catch(e:Exception){token="";throw e}
 }
 override suspend fun inventory(filter:InventoryFilter,page:Int):InventoryPage = json.decodeFromString(request("motocicletas",params=mapOf("chasis" to filter.chasis,"marca" to filter.marca,"modelo" to filter.modelo,"color" to filter.color,"pagina" to page.toString(),"tamano" to "20").filterValues{it.isNotBlank()}))
 override suspend fun detail(module:Module,id:Long):JsonObject{
  val path=when(module){Module.ARRIVALS->"llegadas/$id/detalle";Module.PERMISSIONS->"usuarios/$id/permisos";else->"motocicletas/$id"}
  return json.parseToJsonElement(request(path)).jsonObject
 }
 override suspend fun submit(path:String,body:JsonObject,method:String):JsonObject?{
  val text=request(path,body.toString(),method);return if(text.isBlank())null else json.parseToJsonElement(text).jsonObject
 }
 override suspend fun logout(){request("auth/logout","{}","POST");clearSession()}
 override suspend fun currentLocation(id: Long): LocationRecord? =
     request("motocicletas/$id/ubicacion").takeIf { it.isNotBlank() }
         ?.let { json.decodeFromString<LocationRecord?>(it) }

 override suspend fun sites(): List<Site> = json.decodeFromString(request("ubicaciones"))

 override suspend fun registerLocation(id: Long, destinationId: Long): LocationRecord {
     val payload = buildJsonObject { put("destinoId", destinationId) }
     val response = try {
         request("motocicletas/$id/ubicacion", payload.toString(), "POST")
     } catch (exception: AppException) {
         if (exception.error is AppError.Conflict) {
             throw AppException(AppError.Conflict(
                 "La motocicleta ya tiene ubicación. Actualiza la consulta para ver el destino guardado."
             ))
         }
         throw exception
     }
     return json.decodeFromString(response)
 }

 override suspend fun requests(page: Int): List<AssemblyRequest> = json.decodeFromString(request("solicitudes",params=mapOf("pagina" to page.toString(),"tamano" to "20")))
 override suspend fun request(id: Long): AssemblyRequest = json.decodeFromString(request("solicitudes/$id"))
 override suspend fun createRequest(body: NewRequest): AssemblyRequest = json.decodeFromString(request("solicitudes",json.encodeToString(body),"POST"))
 override suspend fun events(id: Long): List<RequestEvent> = json.decodeFromString(request("solicitudes/$id/seguimiento"))
 override suspend fun advance(id: Long,status: String,reason: String) { request("solicitudes/$id/seguimiento",buildJsonObject { put("estado",status);put("motivo",reason) }.toString(),"POST") }
 override suspend fun cancel(id: Long,reason: String) { request("solicitudes/$id/anulacion",buildJsonObject { put("motivo",reason) }.toString(),"POST") }
 override suspend fun eligible(chassis: String): List<EligibleUnit> = json.decodeFromString(request("motocicletas/elegibles",params=mapOf("chasis" to chassis)))
 override suspend fun assigned(id: Long,line: Long): List<AssignedUnit> = json.decodeFromString(request("solicitudes/$id/lineas/$line/unidades"))
 override suspend fun assign(id: Long,line: Long,motorcycle: Long) { request("solicitudes/$id/lineas/$line/unidades",buildJsonObject { put("motocicletaId",motorcycle) }.toString(),"POST") }
 override suspend fun remove(id: Long,line: Long,association: Long,reason: String) { request("solicitudes/$id/lineas/$line/unidades/$association",buildJsonObject { put("motivo",reason) }.toString(),"DELETE") }
 override suspend fun transfer(motorcycle: Long,destination: Long): TransferRecord = json.decodeFromString(request("motocicletas/$motorcycle/traslados",buildJsonObject { put("destinoId",destination) }.toString(),"POST"))
 override suspend fun transfers(motorcycle: Long): List<TransferRecord> = json.decodeFromString(request("motocicletas/$motorcycle/ubicacion/historial"))
 override fun clearSession(){token=""}
}
