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
        // Llegadas no pertenece a Inventarios: el módulo abierto no cambia.
        vm.open(Module.ARRIVALS);
        advanceUntilIdle();
        assertEquals(
            Module.FICHAS,
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
        assertEquals(
            listOf(Module.INVENTORY, Module.TRANSFERS, Module.REQUESTS),
            Module.entries.filter(vm::canRead)
        )
        assertEquals("Consultar ubicación y recorrido de cada moto", FieldAccess.description(Module.TRANSFERS, vm::allowed))
        assertEquals("Seguir solicitudes y autorizar anulaciones", FieldAccess.description(Module.REQUESTS, vm::allowed))
    }

    @Test
    fun warehouseStartsWithTheGuideArrival() = runTest {
        val repo = Fake(
            "ALMACEN",
            listOf(
                Permission("LLEGADAS_REGISTRAR", true),
                Permission("FICHAS_REGISTRAR", true),
                Permission("INVENTARIO_CONSULTAR", true),
                Permission(READ_LOCATIONS, true),
                Permission(READ_REQUESTS, true)
            )
        )
        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "almacen", "unused")
        advanceUntilIdle()
        // Aunque tuviera FICHAS_REGISTRAR, registrar fichas es tarea de Inventarios.
        assertEquals(
            listOf(Module.ARRIVALS, Module.INVENTORY, Module.TRANSFERS, Module.REQUESTS),
            Module.entries.filter(vm::canRead)
        )
    }

    @Test
    fun eachRoleSeesOnlyItsOwnWorkEvenWithBroadPermissions() = runTest {
        val everything = listOf(
            "LLEGADAS_REGISTRAR", "FICHAS_REGISTRAR", "INVENTARIO_CONSULTAR",
            READ_LOCATIONS, READ_REQUESTS
        ).map { Permission(it, true) }
        val expected = mapOf(
            "INVENTARIOS" to listOf(Module.FICHAS, Module.INVENTORY),
            "COMERCIAL" to listOf(Module.REQUESTS),
            "ENSAMBLAJE" to listOf(Module.REQUESTS),
            "FACTURACION" to listOf(Module.INVENTORY),
            "DESCONOCIDO" to emptyList()
        )
        expected.forEach { (role, modules) ->
            val repo = Fake(role, everything)
            val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
            vm.login("http://localhost", role.lowercase(), "unused")
            advanceUntilIdle()
            assertEquals(modules, Module.entries.filter(vm::canRead), role)
        }
    }

    @Test
    fun roleModuleStillNeedsTheServerPermission() = runTest {
        val repo = Fake("FACTURACION", emptyList())
        val vm = LogisticsViewModel(repo, LogisticsUseCase(repo), repo)
        vm.login("http://localhost", "facturacion", "unused")
        advanceUntilIdle()
        assertTrue(Module.entries.none(vm::canRead))
    }

    @Test
    fun administrationHasNoFieldModulesEvenWithEveryPermission() = runTest {
        val repo = Fake(
            "ADMIN",
            listOf(
                "USUARIOS_CREAR", "PERMISOS_ASIGNAR", "INVENTARIO_CONSULTAR",
                READ_REQUESTS, READ_LOCATIONS, "LLEGADAS_REGISTRAR"
            ).map { Permission(it, true) }
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
