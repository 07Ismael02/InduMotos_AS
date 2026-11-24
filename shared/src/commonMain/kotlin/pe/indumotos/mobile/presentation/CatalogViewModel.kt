package pe.indumotos.mobile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.indumotos.mobile.domain.*

data class CatalogState(val options: List<CatalogOption> = emptyList(), val busy: Boolean = false,
    val error: String? = null, val message: String? = null, val expired: Boolean = false)
class CatalogViewModel(private val repo: CatalogRepository): ViewModel() {
    private val mutable = MutableStateFlow(CatalogState())
    val state = mutable.asStateFlow()
    private var task:Job?=null
    private var epoch=0
    fun reset(){epoch++;task?.cancel();mutable.value=CatalogState()}
    private fun work(block: suspend () -> Unit) {
        if(mutable.value.busy) return
        val current=epoch
        mutable.update { it.copy(busy=true,error=null,message=null) }
        task=viewModelScope.launch {
            try { block() }
            catch(e: CancellationException) { throw e }
            catch(e: AppException) { if(current==epoch)mutable.update { it.copy(error=e.message,expired=e.error==AppError.Unauthorized) } }
            catch(e: Exception) { if(current==epoch)mutable.update { it.copy(error=e.message ?: "No se pudo consultar el catálogo.") } }
            finally { if(current==epoch)mutable.update { it.copy(busy=false) } }
        }
    }
    fun load() = work { mutable.update { it.copy(options=emptyList()) };val options=repo.catalog();mutable.update { it.copy(options=options) } }
    fun add(brand: String, model: String, color: String) = work {
        val option=CatalogOption(brand.trim(),model.trim(),color.trim())
        require(listOf(option.marca,option.modelo,option.color).all { it.isNotBlank() && it.length<=120 }) { "Completa marca, modelo y color (máximo 120 caracteres)." }
        val saved=repo.addCatalog(option)
        mutable.update { it.copy(options=(it.options+saved).distinct(),message="Combinación agregada al catálogo.") }
    }
    fun changeState(option:CatalogOption)=work {
        val saved=repo.catalogState(option.id,!option.activo)
        mutable.update{it.copy(options=it.options.map{o->if(o.id==saved.id)saved else o},message=if(saved.activo)"Combinación activada." else "Combinación desactivada; las motos existentes se conservan.")}
    }
}
