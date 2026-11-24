package pe.indumotos.mobile.domain

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

data class CachedValue(val payload:String,val savedMs:Long)
@Serializable data class OfflineProfile(val owner:String,val origin:String,val access:Access,val authenticatedMs:Long)
data class LocalDraft(val owner:String,val guideId:Long,val position:Int,val payload:String,val baseline:String?,val revision:Long,val state:String,val detail:String?)
data class OfflineStatus(val offline:Boolean=false,val readOnlySession:Boolean=false,val pending:Int=0,val conflicts:Int=0,val lastDownloadMs:Long?=null,val syncing:Boolean=false,val message:String?=null)
interface OfflineStore {
    suspend fun get(owner:String,resource:String,key:String):CachedValue?
    suspend fun all(owner:String,resource:String):List<CachedValue>
    suspend fun put(owner:String,resource:String,key:String,payload:String,time:Long)
    suspend fun remove(owner:String,resource:String,key:String)
    suspend fun image(owner:String,guide:Long,image:Long):ByteArray?
    suspend fun image(owner:String,guide:Long,image:Long,bytes:ByteArray)
    suspend fun drafts(owner:String):List<LocalDraft>
    suspend fun edit(owner:String,guide:Long,draft:MotorcycleDraft,baseline:String?,state:String):LocalDraft
    suspend fun mark(draft:LocalDraft,state:String,detail:String)
    suspend fun acknowledge(draft:LocalDraft,serverPayload:String)
    suspend fun profile():OfflineProfile?
    suspend fun profile(value:OfflineProfile?)
}
expect fun epochMillis():Long
interface OfflineWork {
    val status:StateFlow<OfflineStatus>
    suspend fun availableProfile():OfflineProfile?
    suspend fun resumeReadOnly(profile:OfflineProfile):Access
    suspend fun downloadInventory()
    suspend fun synchronize()
}
