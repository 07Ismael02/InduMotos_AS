package pe.indumotos.mobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sun.net.httpserver.HttpServer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import java.net.InetSocketAddress
import kotlin.test.*
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.data.*
import pe.indumotos.mobile.domain.*

class OfflineHttpIntegrationTest {
    private class Fixture : AutoCloseable {
        val driver =
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { OfflineDb.Schema.create(it) }
        val store = SqlOfflineStore(OfflineDb(driver));

        val session = OfflineSession(store)
        val client = createHttpClient(OkHttp.create());

        val remote = RemoteRepository(client, session, store)
        var userId = 1L
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val origin = "http://127.0.0.1:${server.address.port}"

        init {
            server.createContext("/api/v1") { e ->
                val body = when (e.requestURI.path.removePrefix("/api/v1/")) {
                    "auth/login" -> """{"accessToken":"token-ficticio-solo-test","expiresIn":900}"""
                    "auth/me" -> """{"usuarioId":$userId,"nombreUsuario":"qa","rolCodigo":"INVENTARIOS","permisos":[{"codigo":"INVENTARIO_CONSULTAR","permitido":true}]}"""
                    "motocicletas" -> """{"items":[{"id":7,"marca":"Honda","modelo":"Wave","color":"Rojo","chasisVista":"****1234"}],"pagina":0,"totalPaginas":1,"totalElementos":1}"""
                    "motocicletas/7" -> """{"id":7,"llegadaId":2,"marca":"Honda","modelo":"Wave","color":"Rojo","chasisCompleto":"QA-CHASIS-COMPLETO-1234","numeroMotor":"QA-MOTOR","dua":"QA-DUA","itemDua":"1"}"""
                    else -> "{}"
                }.toByteArray()
                e.responseHeaders.add(
                    "Content-Type",
                    "application/json"
                );
                e.sendResponseHeaders(200, body.size.toLong());
                e.responseBody.use {
                    it.write(
                        body
                    )
                }
            }
            server.start()
        }

        override fun close() {
            server.stop(0);
            client.close();
            driver.close()
        }
    }

    @Test
    fun downloadedInventoryAndFullChassisSearchWorkAfterRealSocketDisconnect() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "qa", "solo-test");
            f.remote.inventory(
                InventoryFilter(),
                0
            );
            f.remote.detail(Module.FICHAS, 7)
            f.server.stop(0)
            val rows = f.remote.inventory(InventoryFilter(chasis = "QA-CHASIS-COMPLETO-1234"), 0)
            assertEquals(
                7,
                rows.items.single()["id"]!!.jsonPrimitive.int
            );
            assertTrue(f.session.status.value.offline)
            assertEquals(1, f.remote.inventory(InventoryFilter(chasis = "1234"), 0).items.size)
            assertTrue(f.remote.inventory(InventoryFilter(marca = "Yamaha"), 0).items.isEmpty())
            assertEquals(
                "QA-MOTOR",
                f.remote.detail(Module.FICHAS, 7)["numeroMotor"]!!.jsonPrimitive.content
            )
        }
    }

    @Test
    fun switchingUserCannotReadPreviousUsersDownloadedInventory() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "qa", "solo-test");
            f.remote.inventory(InventoryFilter(), 0)
            f.userId = 2;
            f.remote.login(f.origin, "qa2", "solo-test");
            f.server.stop(0)
            assertFailsWith<AppException> { f.remote.inventory(InventoryFilter(), 0) }
        }
    }

    @Test
    fun readOnlyReopenUsesCacheWithoutRecreatingBearerToken() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "qa", "solo-test");
            f.remote.inventory(InventoryFilter(), 0)
            val profile =
                f.store.profile()!!;
            f.remote.clearSession();
            f.server.stop(0);
            f.remote.restoreOrigin(
                f.origin
            );
            f.session.resume(profile)
            assertEquals(1, f.remote.inventory(InventoryFilter(), 0).items.size)
            assertFailsWith<AppException> {
                f.remote.submit(
                    "usuarios",
                    buildJsonObject { put("nombreUsuario", "no-enviar") })
            }
            assertFalse(Json.encodeToString(profile).contains("token-ficticio-solo-test"))
        }
    }
}
