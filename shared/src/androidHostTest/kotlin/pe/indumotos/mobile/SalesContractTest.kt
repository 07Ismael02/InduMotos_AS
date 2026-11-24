package pe.indumotos.mobile
import kotlin.test.*
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString
import pe.indumotos.mobile.domain.*
class SalesContractTest {
 @Test fun amountsDecodeWithoutFloatingPointConversion(){
  val p=Json{ignoreUnknownKeys=true}.decodeFromString<ManualPayment>("""{"id":1,"motocicletaId":2,"tiendaId":3,"chasis":"PRUEBA1","metodo":"YAPE","entidad":"YAPE","referencia":"Prueba1","importe":1234567890.25,"estado":"PENDIENTE","registrador":"comercial.indumotos","fecha":"2026-11-24T05:00:00Z"}""")
  assertEquals("1234567890.25",p.importe.jsonPrimitive.content);assertEquals("PENDIENTE",p.estado)
 }
 @Test fun inactiveCatalogIsNotOfferedInDependentSelectors(){
  val rows=listOf(CatalogOption("Honda","Wave","Rojo",1,true),CatalogOption("Suzuki","GN125","Azul",2,false))
  assertEquals(listOf("Honda"),rows.brands());assertFalse(rows.containsCombination("Suzuki","GN125","Azul"));assertTrue(rows.models("Suzuki").isEmpty())
 }
}
