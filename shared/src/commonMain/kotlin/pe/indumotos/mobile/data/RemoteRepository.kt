package pe.indumotos.mobile.data
import pe.indumotos.mobile.domain.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.CancellationException

class RemoteRepository(private val http:HttpClient):LogisticsRepository{
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
 override fun clearSession(){token=""}
}
