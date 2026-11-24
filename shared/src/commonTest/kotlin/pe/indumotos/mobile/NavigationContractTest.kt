package pe.indumotos.mobile

import kotlin.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.JsonObject
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.LogisticsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationContractTest {
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun guideModulesDoNotCallObsoleteArrivalEndpointOrInventory() = runTest {
        val repo = Fake();

        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "worker", "unused");
        advanceUntilIdle()
        vm.open(Module.FICHAS);
        advanceUntilIdle();
        assertEquals(
            Module.FICHAS,
            vm.state.value.module
        )
        vm.open(Module.ARRIVALS);
        advanceUntilIdle();
        assertEquals(
            Module.ARRIVALS,
            vm.state.value.module
        )
        assertEquals(0, repo.arrivalCalls);
        assertEquals(0, repo.inventoryCalls)
        vm.open(Module.INVENTORY);
        advanceUntilIdle();
        assertEquals(1, repo.inventoryCalls)
    }

    private class Fake : LogisticsRepository, LocationRepository {
        var inventoryCalls = 0;

        var arrivalCalls = 0
        override suspend fun login(baseUrl: String, user: String, password: String) = Access(
            1,
            user,
            "INVENTARIOS",
            listOf(Permission("INVENTARIO_CONSULTAR", true), Permission("FICHAS_REGISTRAR", true))
        )

        override suspend fun arrivals(guide: String): List<JsonObject> {
            arrivalCalls++;
            error("La consulta antigua no debe ejecutarse")
        }

        override suspend fun inventory(filter: InventoryFilter, page: Int): InventoryPage {
            inventoryCalls++;
            return InventoryPage(emptyList(), 0, 0, 0)
        }

        override suspend fun detail(module: Module, id: Long): JsonObject =
            error("No se consulta detalle")

        override suspend fun submit(path: String, body: JsonObject, method: String): JsonObject? =
            null

        override suspend fun logout() {}
        override fun clearSession() {}
        override suspend fun currentLocation(id: Long): LocationRecord? = null
        override suspend fun sites() = emptyList<Site>()
        override suspend fun registerLocation(id: Long, destinationId: Long): LocationRecord =
            error("No se registra ubicación")
    }
}
