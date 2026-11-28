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

    @Test
    fun managerSeesFollowUpModulesGrantedByTheServer() = runTest {
        val repo = Fake(
            "GERENTE",
            listOf(
                Permission(READ_REQUESTS, true),
                Permission(CANCEL_REQUESTS, true),
                Permission("INVENTARIO_CONSULTAR", true),
                Permission(READ_LOCATIONS, true)
            )
        )
        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "gerente", "unused")
        advanceUntilIdle()
        assertEquals("Gerencia", roleLabel(vm.state.value.access!!.rolCodigo))
        val visible = Module.entries.filter(vm::canRead)
        assertTrue(visible.containsAll(listOf(Module.INVENTORY, Module.TRANSFERS, Module.REQUESTS)))
    }

    @Test
    fun warehouseStartsWithTheGuideArrival() = runTest {
        val repo = Fake(
            "ALMACEN",
            listOf(
                Permission("LLEGADAS_REGISTRAR", true),
                Permission("FICHAS_REGISTRAR", true),
                Permission("INVENTARIO_CONSULTAR", true),
                Permission(READ_LOCATIONS, true)
            )
        )
        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "almacen", "unused")
        advanceUntilIdle()
        assertEquals(
            listOf(Module.ARRIVALS, Module.FICHAS, Module.INVENTORY, Module.TRANSFERS),
            Module.entries.filter(vm::canRead)
        )
    }

    @Test
    fun accountAdministrationAloneOpensNoMobileModule() = runTest {
        val repo = Fake(
            "ADMIN",
            listOf(Permission("USUARIOS_CREAR", true), Permission("PERMISOS_ASIGNAR", true))
        )
        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "admin", "unused")
        advanceUntilIdle()
        assertTrue(Module.entries.none(vm::canRead))
    }

    private class Fake(
        private val role: String = "INVENTARIOS",
        private val permissions: List<Permission> = listOf(
            Permission("INVENTARIO_CONSULTAR", true),
            Permission("FICHAS_REGISTRAR", true)
        )
    ) : LogisticsRepository, LocationRepository {
        var inventoryCalls = 0;

        var arrivalCalls = 0
        override suspend fun login(baseUrl: String, user: String, password: String) =
            Access(1, user, role, permissions)

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
