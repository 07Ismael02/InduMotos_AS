package pe.indumotos.mobile.domain
import kotlinx.serialization.json.*
class LogisticsUseCase(private val repository:LogisticsRepository){
 suspend fun save(form:Form,values:Map<String,String>):JsonObject?{
  form.fields.forEach{f->val value=values[f.key].orEmpty();require(!f.required||value.isNotBlank()){ "Completa ${f.label}" };if(f.numeric)require(Rules.positive(value)){"${f.label}: usa un entero positivo"};if(f.secret)require(value.length in 12..100){"La contraseña debe tener entre 12 y 100 caracteres"};require(value.length<=f.maxLength){"${f.label}: máximo ${f.maxLength} caracteres"}}
  return repository.submit(form.path,buildJsonObject{form.fields.forEach{f->val v=values[f.key].orEmpty();if(f.numeric)put(f.key,v.toLong())else put(f.key,if(f.secret)v else v.trim())}},form.method)
 }
}
data class Field(val key:String,val label:String,val numeric:Boolean=false,val options:List<String> = emptyList(),val secret:Boolean=false,val required:Boolean=true,val maxLength:Int=120,val labels:Map<String,String> = emptyMap())
data class Form(val title:String,val path:String,val fields:List<Field>,val method:String="POST",val sensitive:Boolean=false)
