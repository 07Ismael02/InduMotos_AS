package pe.indumotos.mobile

import kotlin.test.*
import pe.indumotos.mobile.domain.*

class CatalogContractTest {
    @Test
    fun selectorsCannotMixModelsFromAnotherBrand() {
        val options = listOf(
            CatalogOption("Honda", "Wave", "Rojo"),
            CatalogOption("Yamaha", "FZ", "Azul"),
            CatalogOption("Honda", "Wave", "Negro")
        )
        assertEquals(listOf("Honda", "Yamaha"), options.brands())
        assertEquals(listOf("Wave"), options.models("Honda"))
        assertEquals(listOf("Negro", "Rojo"), options.colors("Honda", "Wave"))
        assertFalse(options.containsCombination("Honda", "FZ", "Azul"))
    }
}
