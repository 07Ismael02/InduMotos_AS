package pe.indumotos.mobile
import kotlin.test.*
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.catalogOptions
class CatalogOptionsTest {
 @Test fun preservesRelationships() {
  val rows=Json.decodeFromString<List<JsonObject>>("""[{"marca":"Honda","modelo":"CB","color":"Rojo"},{"marca":"Honda","modelo":"CB","color":"rojo"},{"marca":"Honda","modelo":"XR","color":"Blanco"},{"marca":"Yamaha","modelo":"FZ","color":"Azul"}]""")
  assertEquals(listOf("Honda","Yamaha"),catalogOptions(rows,"marca"))
  assertEquals(listOf("CB","XR"),catalogOptions(rows,"modelo","Honda"))
  assertEquals(listOf("Rojo"),catalogOptions(rows,"color","Honda","CB"))
  assertTrue(catalogOptions(rows,"modelo","No registrada").isEmpty())
 }
}
