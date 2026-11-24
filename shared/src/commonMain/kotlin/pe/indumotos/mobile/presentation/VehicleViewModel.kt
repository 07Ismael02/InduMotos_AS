package pe.indumotos.mobile.presentation
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.indumotos.mobile.domain.*
data class VehicleUi(val journey:kotlinx.serialization.json.JsonObject?=null,val card:VehicleCard?=null,val availability:VehicleAvailability?=null,val image:ByteArray?=null,val index:Int=0,val busy:Boolean=false,val error:String?=null,val expired:Boolean=false,val message:String?=null)
class VehicleViewModel(private val repo:VehicleRepository):ViewModel(){
 private val mutable=MutableStateFlow(VehicleUi());val state=mutable.asStateFlow();private var id=0L;private var job:Job?=null
 fun reset(){job?.cancel();mutable.value=VehicleUi()}
 private fun work(action:suspend()->Unit){if(mutable.value.busy)return;mutable.update{it.copy(busy=true,error=null,message=null)};job=viewModelScope.launch{try{action()}catch(e:CancellationException){throw e}catch(e:AppException){mutable.update{it.copy(error=e.message,expired=e.error==AppError.Unauthorized)}}catch(e:Exception){mutable.update{it.copy(error=e.message?:"No se pudo completar la operación")}}finally{mutable.update{it.copy(busy=false)}}}}
 fun load(value:Long,readState:Boolean)=work{id=value;val card=repo.card(id);mutable.update{it.copy(card=card)};if(card.fotos.isNotEmpty()){val bytes=repo.photo(id,card.fotos.first().id);mutable.update{it.copy(image=bytes,index=0)}};if(readState){val availability=repo.availability(id);mutable.update{it.copy(availability=availability)}}}
 fun photo(index:Int)=work{val photos=mutable.value.card?.fotos.orEmpty();require(index in photos.indices);val bytes=repo.photo(id,photos[index].id);mutable.update{it.copy(image=bytes,index=index)}}
 fun save(specs:VehicleSpecs)=work{val saved=repo.save(id,specs);mutable.update{it.copy(card=saved,message="Características guardadas.")}}
 fun upload(photo:GuidePhoto)=work{GuideRules.photo(photo);val saved=repo.addPhoto(id,photo);val index=saved.fotos.lastIndex;val bytes=repo.photo(id,saved.fotos[index].id);mutable.update{it.copy(card=saved,image=bytes,index=index,message="Fotografía adjunta.")}}
 fun change(state:String,reason:String)=work{require(reason.isNotBlank());val value=repo.changeAvailability(id,state,reason.trim());mutable.update{it.copy(availability=value,message="Estado actualizado con historial.")}}
 fun journey()=work{val value=repo.journey(id);mutable.update{it.copy(journey=value)}}
}
