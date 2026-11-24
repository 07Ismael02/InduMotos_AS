package pe.indumotos.mobile.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import pe.indumotos.mobile.domain.*

class DraftSynchronizer(private val store:OfflineStore,private val session:OfflineSession,
    private val server:GuideRepository,private val authorize:suspend()->Access) {
    private val mutex=Mutex()
    private val json=Json{encodeDefaults=true;ignoreUnknownKeys=true}
    suspend fun synchronize() {
        val owner=session.owner()
        if(!mutex.tryLock())return
        try {
            val access=authorize()
            if(session.owner()!=owner)throw CancellationException("Cambió la cuenta")
            session.syncing(true)
            val allowed=listOf("FICHAS_REGISTRAR","INVENTARIO_CONSULTAR").all{code->access.permisos.any{it.codigo==code&&it.permitido}}
            for(row in store.drafts(owner).filter{it.state=="PENDING"}) {
                if(!allowed){store.mark(row,"DENIED","Tu cuenta ya no tiene permiso para registrar fichas.");continue}
                try {
                    val current=server.registration(row.guideId)
                    val draft=json.decodeFromString<MotorcycleDraft>(row.payload)
                    if(current.terminado||draft.posicion !in 1..current.pending){store.mark(row,"CONFLICT","La guía fue finalizada o cambió su cantidad pendiente.");continue}
                    val found=current.borradores.firstOrNull{it.posicion==row.position}
                    val payload=found?.let{json.encodeToString(it)}
                    cache(owner,row.guideId,current)
                    if(payload==row.payload){store.acknowledge(row,row.payload);cache(owner,row.guideId,current);continue}
                    if(payload!=row.baseline){store.mark(row,"CONFLICT","Otro dispositivo cambió este borrador. Revisa ambas versiones.");continue}
                    val version=current.version
                    if(version==null){store.mark(row,"DENIED","Actualiza el backend: falta la versión segura de borradores.");continue}
                    val saved=server.conditionalSave(row.guideId,draft,version)
                    val acknowledged=saved.borradores.firstOrNull{it.posicion==row.position}?:throw IllegalStateException("El servidor no confirmó el borrador.")
                    cache(owner,row.guideId,saved);store.acknowledge(row,json.encodeToString(acknowledged))
                }catch(e:CancellationException){throw e}
                catch(e:AppException){when(e.error){
                    AppError.Network->{session.offline();break}
                    AppError.Unauthorized->throw e
                    AppError.Forbidden->store.mark(row,"DENIED",e.message.orEmpty())
                    is AppError.Conflict,is AppError.Validation->store.mark(row,"CONFLICT",e.message.orEmpty())
                    else->throw e
                }}
            }
        } catch(e:AppException){if(e.error==AppError.Network)session.offline() else throw e}
        finally {session.refresh();session.syncing(false);mutex.unlock()}
    }
    private suspend fun cache(owner:String,id:Long,value:GuideRegistration){store.put(owner,"http",OfflineReads.key("llegadas/$id/registro",emptyMap()),json.encodeToString(value),epochMillis())}
}
