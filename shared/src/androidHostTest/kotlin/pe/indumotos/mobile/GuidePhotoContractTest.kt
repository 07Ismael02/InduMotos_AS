package pe.indumotos.mobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sun.net.httpserver.HttpServer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import java.net.InetSocketAddress
import kotlin.test.*
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.data.*

class GuidePhotoContractTest {
    private class Fixture : AutoCloseable {
        val driver =
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { OfflineDb.Schema.create(it) }
        val store = SqlOfflineStore(OfflineDb(driver));

        val session = OfflineSession(store);

        val client = createHttpClient(OkHttp.create());

        val remote = RemoteRepository(client, session, store)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0);

        val origin = "http://127.0.0.1:${server.address.port}";

        var finishBody = ""

        init {
            server.createContext("/api/v1") { e ->
                val path = e.requestURI.path.removePrefix("/api/v1/");

                val content = e.requestBody.readBytes().decodeToString()
                val text = when (path) {
                    "auth/login" -> """{"accessToken":"token-prueba","expiresIn":3600}"""
                    "auth/me" -> """{"usuarioId":1,"nombreUsuario":"inventarios.prueba","rolCodigo":"INVENTARIOS","permisos":[]}"""
                    "ubicaciones" -> """[{"id":1,"codigo":"ALM1","nombre":"Almacén","tipo":"ALMACEN","activo":true},{"id":2,"codigo":"ALM2","nombre":"Inactivo","tipo":"ALMACEN","activo":false}]"""
                    "llegadas/10/registro/finalizar" -> {
                        finishBody =
                            content;
                        """{"terminado":true,"recibidas":1,"registradas":1,"borradores":[]}"""
                    }

                    "llegadas/10/guias/1" -> "imagen-guia"
                    "motocicletas/10/fotos/1" -> "imagen-moto"
                    else -> "{}"
                };

                val bytes = text.encodeToByteArray();
                e.responseHeaders.add(
                    "Content-Type",
                    "application/json"
                );
                e.sendResponseHeaders(
                    200,
                    bytes.size.toLong()
                );
                e.responseBody.use { it.write(bytes) }
            };
            server.start()
        }

        override fun close() {
            client.close();
            server.stop(0);
            driver.close()
        }
    }

    @Test
    fun finalizationCarriesSelectedWarehouse() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "inventarios.prueba", "clave-prueba");

            val repo = GuideRemoteRepository(f.remote)
            assertEquals(listOf(1L), repo.warehouses().map { it.id });
            assertTrue(
                repo.finishAt(
                    10,
                    1
                ).terminado
            )
            assertEquals(
                1,
                Json.parseToJsonElement(f.finishBody).jsonObject["almacenId"]!!.jsonPrimitive.int
            )
        }
    }

    @Test
    fun offlineLogoutKeepsAccountAvailableWithoutServerSession() = runTest {
        Fixture().use { f ->
            val access = f.remote.login(f.origin, "inventarios.prueba", "clave-prueba")
            val profile = f.session.current!!
            f.session.resume(profile);
            f.remote.logout()
            assertNull(f.session.current)
            assertEquals(listOf(profile), f.session.availableProfiles())
            assertEquals(access, f.session.resume(profile));
            assertTrue(f.session.readOnly())
        }
    }

    @Test
    fun onlineLogoutKeepsProtectedOfflineProfile() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "inventarios.prueba", "clave-prueba")
            val profile = f.session.current!!;
            f.remote.logout()
            assertNull(f.session.current);
            assertEquals(
                listOf(profile),
                f.session.availableProfiles()
            )
        }
    }

    @Test
    fun guideAndMotorcyclePhotosHaveIndependentOfflineKeys() = runTest {
        Fixture().use { f ->
            f.remote.login(f.origin, "inventarios.prueba", "clave-prueba")
            assertEquals("imagen-guia", f.remote.guideBytes("llegadas/10/guias/1").decodeToString())
            assertEquals(
                "imagen-moto",
                f.remote.guideBytes("motocicletas/10/fotos/1").decodeToString()
            )
            f.server.stop(0);
            f.session.offline()
            assertEquals("imagen-guia", f.remote.guideBytes("llegadas/10/guias/1").decodeToString())
            assertEquals(
                "imagen-moto",
                f.remote.guideBytes("motocicletas/10/fotos/1").decodeToString()
            )
        }
    }
}
