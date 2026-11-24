package pe.indumotos.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.indumotos.mobile.domain.*

data class UsersState(val stores:List<StoreSummary> = emptyList(),val page:UserPage?=null,val search:String="",val selected:UserAccount?=null,val access:Access?=null,val busy:Boolean=false,val error:String?=null,val message:String?=null,val expired:Boolean=false)
class UsersViewModel(private val repo:UsersRepository):ViewModel() {
    private val mutable=MutableStateFlow(UsersState());val state=mutable.asStateFlow()
    private var task:Job?=null;private var epoch=0
    fun reset(){epoch++;task?.cancel();mutable.value=UsersState()}
    private fun work(block:suspend()->Unit) {
        if(mutable.value.busy)return
        val current=epoch;mutable.update{it.copy(busy=true,error=null,message=null)}
        task=viewModelScope.launch {
            try{block()}
            catch(e:CancellationException){throw e}
            catch(e:AppException){if(epoch==current)mutable.update{it.copy(error=e.message,expired=e.error==AppError.Unauthorized)}}
            catch(e:Exception){if(epoch==current)mutable.update{it.copy(error=e.message?:"No se pudo completar la operación.")}}
            finally{if(epoch==current)mutable.update{it.copy(busy=false)}}
        }
    }
    fun list(search:String="",page:Int=0)=work {
        require(search.length<=180&&page>=0)
        val result=repo.users(search,page);mutable.update{it.copy(page=result,search=search)}
    }
    fun select(user:UserAccount)=work {
        mutable.update{it.copy(selected=user,access=null)}
        val access=repo.userAccess(user.id);mutable.update{it.copy(access=access)}
    }
    fun close(){if(!mutable.value.busy)mutable.update{it.copy(selected=null,access=null,error=null,message=null)}}
    fun role(role:String)=work {
        val id=requireNotNull(mutable.value.selected).id
        val access=repo.changeRole(id,role)
        mutable.update{it.copy(access=access,selected=it.selected?.copy(rolCodigo=access.rolCodigo),page=it.page?.copy(items=it.page.items.map{u->if(u.id==id)u.copy(rolCodigo=access.rolCodigo)else u}),message="Rol actualizado.")}
    }
    fun permission(code:String,decision:String?)=work {
        val s=mutable.value;val access=requireNotNull(s.access)
        require(access.permisos.any{it.codigo==code}){"Permiso desconocido."}
        require(decision in listOf(null,"PERMITIR","DENEGAR"))
        require(decision!="PERMITIR"||AccessRules.canAllow(access.rolCodigo,code)){"Este permiso está reservado a Administración."}
        val updated=repo.permission(requireNotNull(s.selected).id,code,decision)
        mutable.update{it.copy(access=updated,message=if(decision==null)"Excepción retirada." else "Excepción actualizada.")}
    }
    fun resetPassword(user:UserAccount,adminPassword:String,newPassword:String)=work {
        require(newPassword.length in 12..100){"La nueva contraseña debe tener entre12 y100 caracteres."}
        repo.resetPassword(user.id,adminPassword,newPassword)
        mutable.update{it.copy(message="Contraseña restablecida para ${user.nombreUsuario}. Sus sesiones anteriores ya no son válidas.")}
    }
    fun stores()=work{val stores=repo.stores();mutable.update{it.copy(stores=stores)}}
    fun assignStore(user:UserAccount,store:StoreSummary)=work{
        repo.assignStore(user.id,store.id)
        mutable.update{it.copy(page=it.page?.copy(items=it.page.items.map{u->if(u.id==user.id)u.copy(tiendaId=store.id,tiendaNombre=store.nombre)else u}),message="Tienda asignada a ${user.nombreUsuario}.")}
    }
}
