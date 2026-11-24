package pe.indumotos.mobile.data

import kotlinx.coroutines.flow.*
import pe.indumotos.mobile.domain.*

class OfflineSession(val store:OfflineStore) {
    var current:OfflineProfile?=null;private set
    private val mutable=MutableStateFlow(OfflineStatus())
    val status=mutable.asStateFlow()
    fun owner():String {
        val profile=current?:throw AppException(AppError.Unauthorized)
        if(readOnly()&&epochMillis()-profile.authenticatedMs !in 0..86_400_000L)throw AppException(AppError.Unauthorized)
        return profile.owner
    }
    suspend fun online(origin:String,access:Access){
        val normalized=origin.trim().trimEnd('/')
        val profile=OfflineProfile("$normalized|${access.usuarioId}",normalized,access,epochMillis())
        store.profile(profile);current=profile;mutable.value=OfflineStatus();refresh()
    }
    suspend fun available():OfflineProfile?=store.profile()?.takeIf{epochMillis()-it.authenticatedMs in 0..86_400_000L}
    suspend fun resume(profile:OfflineProfile):Access {
        require(available()==profile){"Conecta e inicia sesión para renovar la consulta local."}
        current=profile;mutable.value=OfflineStatus(offline=true,readOnlySession=true,message="Consulta local desbloqueada. No es una sesión del servidor.");refresh();return profile.access
    }
    fun close(){current=null;mutable.value=OfflineStatus()}
    fun readOnly()=status.value.readOnlySession
    fun offline(){mutable.update{it.copy(offline=true)}}
    fun downloaded(time:Long){mutable.update{it.copy(offline=false,lastDownloadMs=time)}}
    fun message(text:String){mutable.update{it.copy(message=text)}}
    fun syncing(value:Boolean){mutable.update{it.copy(syncing=value)}}
    suspend fun refresh(){val rows=current?.let{store.drafts(it.owner)}.orEmpty();mutable.update{it.copy(pending=rows.count{r->r.state in listOf("EDITING","PENDING")},conflicts=rows.count{r->r.state in listOf("CONFLICT","DENIED")})}}
}
