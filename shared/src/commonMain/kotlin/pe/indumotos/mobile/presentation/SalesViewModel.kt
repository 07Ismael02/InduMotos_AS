package pe.indumotos.mobile.presentation
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.indumotos.mobile.domain.*
data class SalesUi(val stores:List<StoreSummary> = emptyList(),val store:StoreSummary?=null,val stock:StorePage<StockMotorcycle>?=null,val payments:StorePage<ManualPayment>?=null,val sales:StorePage<StoreSale>?=null,val receipt:ByteArray?=null,val busy:Boolean=false,val error:String?=null,val message:String?=null,val expired:Boolean=false,val registered:Boolean=false)
class SalesViewModel(private val repo:SalesRepository):ViewModel(){
 private val mutable=MutableStateFlow(SalesUi());val state=mutable.asStateFlow();private var job:Job?=null
 fun reset(){job?.cancel();mutable.value=SalesUi()}
 private fun work(writing:Boolean=false,action:suspend()->Unit){if(mutable.value.busy)return;mutable.update{it.copy(busy=true,error=null,message=null,registered=false)};job=viewModelScope.launch{try{action()}catch(e:CancellationException){throw e}catch(e:AppException){mutable.update{it.copy(error=if(writing&&e.error==AppError.Network)"No se confirmó la respuesta. Actualiza los pagos antes de repetir; la solicitud conserva su identificador."else e.message,expired=e.error==AppError.Unauthorized)}}catch(e:Exception){mutable.update{it.copy(error=e.message?:"No se completó la operación")}}finally{mutable.update{it.copy(busy=false)}}}}
 fun load(admin:Boolean)=work{val stores=repo.stores(admin);mutable.update{it.copy(stores=stores)};stores.firstOrNull()?.let{loadStore(it)}}
 private suspend fun loadStore(store:StoreSummary){mutable.update{it.copy(store=store,stock=null,payments=null,sales=null)};refreshRows()}
 private suspend fun refreshRows(){val id=requireNotNull(mutable.value.store).id;val stock=repo.stock(id,0);val payments=repo.payments(id,0);val sales=repo.sales(id,0);mutable.update{it.copy(stock=stock,payments=payments,sales=sales)}}
 fun select(store:StoreSummary)=work{loadStore(store)}
 fun refresh()=work{refreshRows()}
 fun page(tab:Int,page:Int)=work{require(page>=0);val id=requireNotNull(mutable.value.store).id;when(tab){0->{val data=repo.stock(id,page);mutable.update{it.copy(stock=data)}};1->{val data=repo.payments(id,page);mutable.update{it.copy(payments=data)}};else->{val data=repo.sales(id,page);mutable.update{it.copy(sales=data)}}}}
 fun register(request:String,motorcycle:Long,method:String,entity:String,reference:String,amount:String,photo:GuidePhoto)=work(true){GuideRules.photo(photo);require(Regex("[0-9]{1,10}(?:\\.[0-9]{1,2})?").matches(amount)&&amount.any{it in '1'..'9'}){"Importe positivo, con hasta dos decimales"};val id=requireNotNull(mutable.value.store).id;repo.register(id,request,motorcycle,method,entity,reference,amount,photo);refreshRows();mutable.update{it.copy(registered=true,message="Pago pendiente. Facturación debe comprobar el abono real; todavía no es una venta.")}}
 fun confirm(payment:ManualPayment,receipt:String,verified:Boolean)=work(true){repo.confirm(payment.id,receipt,verified);refreshRows();mutable.update{it.copy(message="Pago confirmado manualmente y venta registrada.")}}
 fun reject(payment:ManualPayment,reason:String)=work(true){repo.reject(payment.id,reason);refreshRows();mutable.update{it.copy(message="Pago rechazado; revisa el motivo.")}}
 fun receipt(payment:ManualPayment)=work{val bytes=repo.receipt(payment.id);mutable.update{it.copy(receipt=bytes)}}
 fun closeReceipt(){mutable.update{it.copy(receipt=null)}}
}
