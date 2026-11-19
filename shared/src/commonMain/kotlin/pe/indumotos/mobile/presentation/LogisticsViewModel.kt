package pe.indumotos.mobile.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

data class UiState(val access:Access?=null,val module:Module?=null,val rows:List<JsonObject> = emptyList(),val page:Int=0,val pages:Int=0,val total:Long=0,val filter:InventoryFilter=InventoryFilter(),val busy:Boolean=false,val error:String?=null,val message:String?=null,val form:Form?=null,val detail:JsonObject?=null)
class LogisticsViewModel(
    private val repo: LogisticsRepository,
    private val save: LogisticsUseCase,
    private val locations: LocationRepository
):ViewModel(){
 private val mutableLocation = MutableStateFlow<MotorcycleLocation?>(null)
 val location = mutableLocation.asStateFlow()
 private val mutable=MutableStateFlow(UiState());val state=mutable.asStateFlow()
 private fun work(block:suspend()->Unit){if(mutable.value.busy)return;mutable.update{it.copy(busy=true,error=null,message=null)};viewModelScope.launch{try{block()}catch(e:CancellationException){throw e}catch(e:AppException){if(e.error==AppError.Unauthorized){repo.clearSession();mutable.value=UiState(error=if(mutable.value.access==null)"Usuario o contraseña incorrectos, o cuenta inactiva." else "Tu sesión venció. Inicia sesión nuevamente.")}else mutable.update{it.copy(error=e.message)}}catch(e:Exception){mutable.update{it.copy(error=e.message?:"No se pudo completar la operación")}}finally{mutable.update{it.copy(busy=false)}}}}
 fun login(url:String,user:String,password:String)=work{val a=repo.login(url,user,password);mutable.value=UiState(access=a,busy=true)}
 fun open(module:Module?){if(mutable.value.busy)return;if(module!=null&&!canRead(module))return;mutable.update{it.copy(module=module,rows=emptyList(),detail=null,error=null,message=null,filter=InventoryFilter(),page=0,pages=0,total=0)};if(module==Module.INVENTORY)work{load(0)}}
 private suspend fun load(page:Int){val result=repo.inventory(mutable.value.filter,page);mutable.update{it.copy(rows=result.items,page=result.pagina,pages=result.totalPaginas,total=result.totalElementos)}}
 fun search(filter:InventoryFilter)=work{val chasis=filter.chasis.trim();require(chasis.isBlank()||chasis.length>=4){"Ingresa el chasis completo o los últimos cuatro dígitos"};require(chasis.length!=4||chasis.all{it.isDigit()}){"El chasis corto debe tener cuatro dígitos"};mutable.update{it.copy(filter=filter,rows=emptyList(),detail=null,page=0,pages=0,total=0)};load(0)}
 fun page(page:Int){if(page<0||page>=mutable.value.pages)return;work{load(page)}}
 fun query(id:String)=work{require(Rules.positive(id)){"Ingresa un ID válido"};val module=mutable.value.module?:return@work;mutable.update{it.copy(detail=null)};val result=repo.detail(module,id.toLong());mutable.update{it.copy(detail=result)}}
 fun inventoryDetail(id:String)=work{mutableLocation.value=null;val result=repo.detail(Module.FICHAS,id.toLong());mutable.update{it.copy(detail=result)}}
 fun closeDetail(){if(!mutable.value.busy){mutableLocation.value=null;mutable.update{it.copy(detail=null,error=null,message=null)}}}
 fun loadLocation(id: Long) {
     if (!allowed(READ_LOCATIONS)) return
     work {
         val current = locations.currentLocation(id)
         val sites = if (current == null && allowed(WRITE_LOCATION)) locations.sites() else emptyList()
         mutableLocation.value = MotorcycleLocation(current, sites)
     }
 }
 fun registerLocation(id: Long, destination: Site) {
     if (!allowed(READ_LOCATIONS) || !allowed(WRITE_LOCATION)) return
     val current = mutableLocation.value ?: return
     if (current.current != null || destination !in current.destinations) return
     work {
         val saved = locations.registerLocation(id, destination.id)
         mutableLocation.value = current.copy(current = saved)
         mutable.update { it.copy(message = "Ubicación registrada.") }
     }
 }
 fun form(form:Form?){if(!mutable.value.busy)mutable.update{it.copy(form=form,error=null,message=null)}}
 fun submit(values:Map<String,String>){val form=mutable.value.form?:return;work{val response=save.save(form,values);val id=response?.get("id")?.jsonPrimitive?.contentOrNull;mutable.update{it.copy(form=null,message=if(id!=null)"Registro guardado con ID #$id." else "Operación confirmada por el servidor.",detail=null)};if(mutable.value.module==Module.INVENTORY)load(mutable.value.page)}}
 fun logout()=work{repo.logout();mutable.value=UiState(message="Sesión cerrada correctamente.")}
 fun allowed(permission:String)=state.value.access?.permisos?.any{it.codigo==permission&&it.permitido}==true
 fun canRead(module:Module)=when(module){Module.ARRIVALS->allowed("LLEGADAS_REGISTRAR")||allowed("INVENTARIO_CONSULTAR");Module.FICHAS->allowed("FICHAS_REGISTRAR")||allowed("INVENTARIO_CONSULTAR");else->allowed(module.permission)}
}
