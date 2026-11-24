package pe.indumotos.mobile.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.db.Local_draft
import pe.indumotos.mobile.domain.*

class SqlOfflineStore(private val db:OfflineDb):OfflineStore {
    private val q=db.offlineQueries
    private val mutex=Mutex()
    private val json=Json{encodeDefaults=true;ignoreUnknownKeys=true}
    private suspend fun <T> io(block:()->T):T=withContext(Dispatchers.Default){mutex.withLock{block()}}
    private fun Local_draft.domain()=LocalDraft(owner,guide_id,position.toInt(),payload,baseline,revision,state,detail)
    override suspend fun get(owner:String,resource:String,key:String)=io{q.getCache(owner,resource,key).executeAsOneOrNull()?.let{CachedValue(it.payload,it.saved_ms)}}
    override suspend fun all(owner:String,resource:String)=io{q.allCache(owner,resource).executeAsList().map{CachedValue(it.payload,it.saved_ms)}}
    override suspend fun put(owner:String,resource:String,key:String,payload:String,time:Long)=io{q.putCache(owner,resource,key,payload,time);Unit}
    override suspend fun remove(owner:String,resource:String,key:String)=io{q.dropCache(owner,resource,key);Unit}
    override suspend fun image(owner:String,guide:Long,image:Long)=io{q.getImage(owner,guide,image).executeAsOneOrNull()}
    override suspend fun image(owner:String,guide:Long,image:Long,bytes:ByteArray)=io{q.putImage(owner,guide,image,bytes);Unit}
    override suspend fun drafts(owner:String)=io{q.allDrafts(owner).executeAsList().map{it.domain()}}
    override suspend fun edit(owner:String,guide:Long,draft:MotorcycleDraft,baseline:String?,state:String):LocalDraft=io {
        db.transactionWithResult {
            val old=q.getDraft(owner,guide,draft.posicion.toLong()).executeAsOneOrNull()
            val value=LocalDraft(owner,guide,draft.posicion,json.encodeToString(draft),if(old!=null)old.baseline else baseline,(old?.revision?:0)+1,state,null)
            q.putDraft(owner,guide,value.position.toLong(),value.payload,value.baseline,value.revision,value.state,null)
            value
        }
    }
    override suspend fun mark(draft:LocalDraft,state:String,detail:String)=io {
        val current=q.getDraft(draft.owner,draft.guideId,draft.position.toLong()).executeAsOneOrNull()
        if(current?.revision==draft.revision)q.putDraft(draft.owner,draft.guideId,draft.position.toLong(),current.payload,current.baseline,current.revision,state,detail)
    }
    override suspend fun acknowledge(draft:LocalDraft,serverPayload:String)=io {
        db.transaction {
            val current=q.getDraft(draft.owner,draft.guideId,draft.position.toLong()).executeAsOneOrNull()
            if(current?.revision==draft.revision)q.ackDraft(draft.owner,draft.guideId,draft.position.toLong(),draft.revision)
            else if(current!=null&&current.baseline==draft.baseline)q.putDraft(current.owner,current.guide_id,current.position,current.payload,serverPayload,current.revision,current.state,current.detail)
        }
    }
    override suspend fun profile():OfflineProfile?=io{q.getProfile().executeAsOneOrNull()?.let{OfflineProfile(it.owner,it.origin,json.decodeFromString(it.access_json),it.authenticated_ms)}}
    override suspend fun profile(value:OfflineProfile?)=io{if(value==null)q.dropProfile() else q.putProfile(value.owner,value.origin,json.encodeToString(value.access),value.authenticatedMs);Unit}
}
