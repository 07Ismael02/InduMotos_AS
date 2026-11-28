package pe.indumotos.mobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import pe.indumotos.mobile.data.*
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.domain.*

/** Sin conexión, un borrador debe cumplir las mismas reglas que exige el servidor. */
class OfflineDraftRulesTest {
    private val fichas = Access(
        1, "inventarios", "INVENTARIOS",
        listOf(Permission("FICHAS_REGISTRAR", true), Permission("INVENTARIO_CONSULTAR", true))
    )
    private val draft = MotorcycleDraft(1, "Honda", "Wave", "Rojo", "CH-NUEVO", "MO-NUEVO", "DUA", "1")

    private class Fixture : AutoCloseable {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { OfflineDb.Schema.create(it) }
        val store = SqlOfflineStore(OfflineDb(driver))
        val session = OfflineSession(store)
        private val guides = object : GuideRepository {
            override suspend fun registration(id: Long) = GuideRegistration(false, 2, 0, emptyList())
            override suspend fun detail(id: Long): ArrivalDetail = error("sin red")
            override suspend fun arrivals(guide: String, state: String, page: Int): ArrivalPage = error("sin red")
            override suspend fun register(
                guide: String, declared: Int, received: Int, note: String, photos: List<GuidePhoto>
            ): Arrival = error("sin red")
            override suspend fun images(id: Long): List<GuideImage> = emptyList()
            override suspend fun image(id: Long, image: Long): ByteArray = error("sin red")
            override suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration = error("sin red")
            override suspend fun finish(id: Long): GuideRegistration = error("sin red")
            override suspend fun ocr(id: Long): OcrResult = error("sin red")
        }
        val repo = OfflineGuideRepository(
            guides, guides, store, session,
            DraftSynchronizer(store, session, guides) { error("no debe sincronizar sin conexión") }
        )

        /** Simula una cuenta que descargó datos y luego reabrió el teléfono sin servidor. */
        suspend fun offlineAs(access: Access) {
            session.online("http://localhost", access)
            val profile = store.profile()!!
            session.close()
            session.resume(profile)
        }

        override fun close() = driver.close()
    }

    @Test
    fun accountWithoutFichasPermissionCannotSaveDraftOffline() = runTest {
        Fixture().use { f ->
            f.offlineAs(fichas.copy(rolCodigo = "ALMACEN", permisos = listOf(Permission("INVENTARIO_CONSULTAR", true))))
            val error = assertFailsWith<AppException> { f.repo.saveDraft(3, draft) }
            assertEquals(AppError.Forbidden, error.error)
            assertTrue(f.store.drafts(f.session.owner()).isEmpty())
        }
    }

    @Test
    fun downloadedChassisOrEngineCannotBeRepeatedOffline() = runTest {
        Fixture().use { f ->
            f.offlineAs(fichas)
            f.store.put(
                f.session.owner(), "inventory-row", "7",
                """{"id":7,"chasisCompleto":"CH-EXISTE","numeroMotor":"MO-EXISTE"}""", 1
            )
            assertFailsWith<IllegalArgumentException> { f.repo.saveDraft(3, draft.copy(chasisCompleto = " ch-existe ")) }
            assertFailsWith<IllegalArgumentException> { f.repo.saveDraft(3, draft.copy(numeroMotor = "mo-existe")) }
            assertTrue(f.store.drafts(f.session.owner()).isEmpty())
        }
    }

    @Test
    fun draftOfAnotherGuideBlocksTheSameEngineButEditingItselfIsAllowed() = runTest {
        Fixture().use { f ->
            f.offlineAs(fichas)
            f.repo.saveDraft(3, draft)
            f.repo.saveDraft(3, draft.copy(dua = "DUA-2"))
            assertFailsWith<IllegalArgumentException> {
                f.repo.saveDraft(4, draft.copy(chasisCompleto = "CH-OTRO"))
            }
            assertEquals(1, f.store.drafts(f.session.owner()).size)
        }
    }
}
