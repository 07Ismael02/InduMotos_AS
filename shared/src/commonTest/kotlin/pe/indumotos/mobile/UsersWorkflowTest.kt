package pe.indumotos.mobile

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class UsersWorkflowTest {
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private val user = UserAccount(9, "Trabajador QA", "qa.usuario", "ALMACEN", "ACTIVO")

    @Test
    fun searchAndPageUseServerContract() = runTest {
        val repo = Fake();

        val vm = UsersViewModel(repo);
        vm.list("qa", 1);
        advanceUntilIdle()
        assertEquals("qa" to 1, repo.query);
        assertEquals(2, vm.state.value.page!!.pages)
    }

    @Test
    fun allEffectivePermissionsRemainVisible() = runTest {
        val vm = UsersViewModel(Fake());
        vm.select(user);
        advanceUntilIdle()
        assertTrue(vm.state.value.access!!.permisos.any { it.codigo == "SOLICITUDES_AVANZAR" })
        assertTrue(vm.state.value.access!!.permisos.any { it.codigo == "CATALOGO_GESTIONAR" })
    }

    @Test
    fun operationalRoleCannotReceiveReservedPermissions() = runTest {
        val repo = Fake();

        val vm = UsersViewModel(repo);
        vm.select(user);
        advanceUntilIdle()
        vm.permission("CATALOGO_GESTIONAR", "PERMITIR");
        advanceUntilIdle()
        assertEquals(0, repo.writes);
        assertNotNull(vm.state.value.error)
    }

    @Test
    fun roleAndExceptionChangesKeepSelectionAndListConsistent() = runTest {
        val repo = Fake();

        val vm =
            UsersViewModel(repo);
        vm.list();
        advanceUntilIdle();
        vm.select(user);
        advanceUntilIdle()
        vm.role("ADMIN");
        advanceUntilIdle();
        assertEquals(
            "ADMIN",
            vm.state.value.page!!.items.first().rolCodigo
        )
        vm.permission("CATALOGO_GESTIONAR", "PERMITIR");
        advanceUntilIdle();
        assertEquals(
            2,
            repo.writes
        )
        assertTrue(vm.state.value.access!!.permisos.first { it.codigo == "CATALOGO_GESTIONAR" }.permitido)
        vm.permission(
            "CATALOGO_GESTIONAR",
            null
        );
        advanceUntilIdle();
        assertNull(vm.state.value.access!!.permisos.first { it.codigo == "CATALOGO_GESTIONAR" }.excepcion)
    }

    @Test
    fun resetCancelsAccountScopedRead() = runTest {
        val repo = Fake();
        repo.gate = CompletableDeferred();

        val vm =
            UsersViewModel(repo);
        vm.select(user);
        runCurrent();
        vm.reset();
        repo.gate!!.complete(
            Unit
        );
        advanceUntilIdle()
        assertNull(vm.state.value.selected);
        assertNull(vm.state.value.access)
    }

    @Test
    fun unauthorizedReadRequestsLoginAgain() = runTest {
        val repo = Fake();
        repo.unauthorized = true;

        val vm =
            UsersViewModel(repo);
        vm.list();
        advanceUntilIdle();
        assertTrue(vm.state.value.expired)
    }

    private inner class Fake : UsersRepository {
        var query: Pair<String, Int>? = null;

        var writes = 0;

        var gate: CompletableDeferred<Unit>? = null;

        var unauthorized = false
        var access = Access(
            9,
            user.nombreUsuario,
            user.rolCodigo,
            listOf(Permission("SOLICITUDES_AVANZAR", true), Permission("CATALOGO_GESTIONAR", false))
        )

        override suspend fun users(search: String, page: Int): UserPage {
            if (unauthorized) throw AppException(AppError.Unauthorized);
            query =
                search to page;
            return UserPage(listOf(user), page, 10, 11)
        }

        override suspend fun userAccess(id: Long): Access {
            gate?.await();
            return access
        }

        override suspend fun changeRole(id: Long, role: String): Access {
            writes++;
            access = access.copy(rolCodigo = role);
            return access
        }

        override suspend fun permission(id: Long, code: String, decision: String?): Access {
            writes++;
            access = access.copy(permisos = access.permisos.map {
                if (it.codigo == code) it.copy(
                    permitido = decision == "PERMITIR",
                    excepcion = decision
                ) else it
            });
            return access
        }
    }
}
