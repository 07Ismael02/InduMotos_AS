package pe.indumotos.mobile.data

import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

class UsersRemoteRepository(private val remote:RemoteRepository):UsersRepository {
    private val json=Json{ignoreUnknownKeys=true}
    override suspend fun users(search:String,page:Int):UserPage=json.decodeFromString(remote.guideJson("usuarios",params=mapOf("buscar" to search.trim(),"pagina" to page.toString(),"tamano" to "10")))
    override suspend fun userAccess(id:Long):Access=json.decodeFromString(remote.guideJson("usuarios/$id/permisos"))
    override suspend fun changeRole(id:Long,role:String):Access=json.decodeFromString(remote.guideJson("usuarios/$id/rol",buildJsonObject{put("rolCodigo",role)}.toString(),"PUT"))
    override suspend fun permission(id:Long,code:String,decision:String?):Access=json.decodeFromString(remote.guideJson("usuarios/$id/permisos/$code",decision?.let{buildJsonObject{put("decision",it)}.toString()},if(decision==null)"DELETE" else "PUT"))
    override suspend fun resetPassword(id:Long,adminPassword:String,newPassword:String){
        remote.guideJson("usuarios/$id/restablecer-clave",buildJsonObject{put("claveAdministrador",adminPassword);put("nuevaClave",newPassword);put("identidadVerificada",true)}.toString(),"POST",onlineOnly=true)
    }
}
