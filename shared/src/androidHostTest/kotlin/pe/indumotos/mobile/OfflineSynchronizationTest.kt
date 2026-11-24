package pe.indumotos.mobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.*
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.data.*
import pe.indumotos.mobile.domain.*

class OfflineSynchronizationTest {
    private val draft =
        MotorcycleDraft(1, "Honda", "Wave", "Rojo", "QA123456789012", "MOTORQA12345", "DUA", "1")
    private val access = Access(
        1,
        "qa",
        "INVENTARIOS",
        listOf(Permission("FICHAS_REGISTRAR", true), Permission("INVENTARIO_CONSULTAR", true))
    )
    private val json = Json { encodeDefaults = true }

    private class Fixture : AutoCloseable {
        val driver =
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { OfflineDb.Schema.create(it) }
        val store = SqlOfflineStore(OfflineDb(driver));

        val session = OfflineSession(store)
        override fun close() {
            driver.close()
        }
    }

    @Test
    fun networkLossReturnsCommittedCacheAndMarksOffline() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access)
            val cache = OfflineReads(f.session, f.store);
            assertEquals(
                "snapshot",
                cache.read("k") { "snapshot" })
            assertEquals("snapshot", cache.read("k") { throw AppException(AppError.Network) })
            assertTrue(f.session.status.value.offline)
        }
    }

    @Test
    fun authenticationAndPermissionErrorsNeverReturnCachedData() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access)
            for (error in listOf(AppError.Unauthorized, AppError.Forbidden)) {
                val cache = OfflineReads(f.session, f.store);
                cache.read("k") { "privado" }
                assertFailsWith<AppException> { cache.read("k") { throw AppException(error) } }
                assertNull(f.store.get(f.session.owner(), "http", "k"))
            }
        }
    }

    @Test
    fun unlockedReadOnlySessionCannotInvokeNetworkAndDoesNotShareAccounts() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access)
            val p = f.store.profile()!!;

            val cache = OfflineReads(
                f.session,
                f.store
            );
            cache.read("k") { "snapshot" };
            f.session.close();
            f.session.resume(p)
            assertEquals("snapshot", cache.read("k") { error("No debe acceder a red") })
            f.session.online("http://localhost", access.copy(usuarioId = 2))
            assertFailsWith<AppException> { cache.read("k") { throw AppException(AppError.Network) } }
        }
    }

    @Test
    fun httpCacheExcludesCredentialsStaffAndOcr() {
        assertFalse(OfflineReads.eligible("auth/me"));
        assertFalse(OfflineReads.eligible("usuarios"));
        assertFalse(
            OfflineReads.eligible("llegadas/1/sugerencias-ocr")
        )
        assertTrue(OfflineReads.eligible("llegadas/1/registro"))
        assertNotEquals(
            OfflineReads.key("x", mapOf("a" to "b&c=d")),
            OfflineReads.key("x", mapOf("a" to "b", "c" to "d"))
        )
    }

    @Test
    fun successfulConditionalPutAcknowledgesExactlyOneLocalRevision() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server()
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            DraftSynchronizer(f.store, f.session, server) { access }.synchronize()
            assertEquals(1, server.writes);
            assertTrue(
                f.store.drafts(f.session.owner()).isEmpty()
            );
            assertEquals(draft, server.current.borradores.single())
        }
    }

    @Test
    fun changedServerBaselineIsNotOverwritten() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server();
            server.current =
                server.current.copy(borradores = listOf(draft.copy(numeroMotor = "OTRO")))
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            DraftSynchronizer(f.store, f.session, server) { access }.synchronize()
            assertEquals(0, server.writes);
            assertEquals(
                "CONFLICT",
                f.store.drafts(f.session.owner()).single().state
            )
        }
    }

    @Test
    fun lostResponseAcknowledgesAlreadyIdenticalServerDraftWithoutRetry() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server();
            server.current = server.current.copy(borradores = listOf(draft))
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            DraftSynchronizer(f.store, f.session, server) { access }.synchronize()
            assertEquals(0, server.writes);
            assertTrue(f.store.drafts(f.session.owner()).isEmpty())
        }
    }

    @Test
    fun raceBetweenGetAndPutRetainsConflictAndNeverRetriesBlindly() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server();
            server.race = true
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            val sync = DraftSynchronizer(
                f.store,
                f.session,
                server
            ) { access };
            sync.synchronize();
            sync.synchronize()
            assertEquals(0, server.writes);
            assertEquals(
                "CONFLICT",
                f.store.drafts(f.session.owner()).single().state
            )
        }
    }

    @Test
    fun missingPermissionBlocksPendingDraftWithoutDeletingIt() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server()
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            DraftSynchronizer(
                f.store,
                f.session,
                server
            ) { access.copy(permisos = emptyList()) }.synchronize()
            assertEquals(0, server.writes);
            assertEquals(
                "DENIED",
                f.store.drafts(f.session.owner()).single().state
            )
        }
    }

    @Test
    fun networkLossKeepsPendingAndNeverFinalizesOrCreatesStock() = runTest {
        Fixture().use { f ->
            f.session.online("http://localhost", access);

            val server = Server()
            f.store.edit(f.session.owner(), 1, draft, null, "PENDING")
            DraftSynchronizer(
                f.store,
                f.session,
                server
            ) { throw AppException(AppError.Network) }.synchronize()
            assertEquals(0, server.writes);
            assertEquals(
                "PENDING",
                f.store.drafts(f.session.owner()).single().state
            )
        }
    }

    private class Server : GuideRepository {
        var current = GuideRegistration(false, 1, 0, emptyList(), "v1");

        var writes = 0;

        var race = false
        override suspend fun registration(id: Long) = current
        override suspend fun conditionalSave(
            id: Long,
            draft: MotorcycleDraft,
            version: String
        ): GuideRegistration {
            if (race || version != current.version) throw AppException(AppError.Conflict("412: cambió la versión"))
            writes++;
            current =
                current.copy(borradores = listOf(draft), version = "v2");
            return current
        }

        override suspend fun detail(id: Long): ArrivalDetail = error("No se necesita")
        override suspend fun arrivals(guide: String, state: String, page: Int): ArrivalPage =
            error("No se necesita")

        override suspend fun register(
            guide: String,
            declared: Int,
            received: Int,
            note: String,
            photos: List<GuidePhoto>
        ): Arrival = error("Nunca crear recepción al sincronizar borradores")

        override suspend fun images(id: Long) = emptyList<GuideImage>()
        override suspend fun image(id: Long, image: Long) = byteArrayOf()
        override suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration =
            error("Se exige PUT condicional")

        override suspend fun finish(id: Long): GuideRegistration =
            error("Nunca finalizar automáticamente")

        override suspend fun ocr(id: Long): OcrResult = error("No se necesita")
    }
}
