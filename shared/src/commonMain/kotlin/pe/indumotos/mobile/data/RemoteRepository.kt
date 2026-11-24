package pe.indumotos.mobile.data

import pe.indumotos.mobile.domain.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.CancellationException

class RemoteRepository(
    private val http: HttpClient,
    private val offline: OfflineSession? = null,
    private val store: OfflineStore? = null
) : LogisticsRepository, LocationRepository, OperationsRepository, CatalogRepository {
    private val json = Json {
        ignoreUnknownKeys = true;
        encodeDefaults = true
    }
    private var base = "";

    private var token = ""
    private suspend fun request(
        path: String,
        body: String? = null,
        method: String = "GET",
        params: Map<String, String> = emptyMap(),
        onlineOnly: Boolean = false,
        version: String? = null
    ): String {
        val sentToken = token
        suspend fun network(): String {
            if (offline?.readOnly() == true) throw AppException(AppError.Unauthorized)
            val response = try {
                http.request(base + path) {
                    this.method =
                        HttpMethod.parse(method);
                    if (path.endsWith("sugerencias-ocr")) timeout {
                        requestTimeoutMillis = 120000
                    } else if (method == "GET") timeout {
                        connectTimeoutMillis = 2500;
                        requestTimeoutMillis = 5000
                    };
                    contentType(ContentType.Application.Json);
                    if (sentToken.isNotEmpty()) bearerAuth(
                        sentToken
                    );
                    if (version != null) header(
                        HttpHeaders.IfMatch,
                        "\"$version\""
                    );
                    params.forEach { (k, v) -> parameter(k, v) };
                    if (body != null) setBody(body)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw AppException(AppError.Network)
            }
            return checkedText(response, sentToken)
        }
        return if (!onlineOnly && method == "GET" && OfflineReads.eligible(path) && offline != null && store != null) OfflineReads(
            offline,
            store
        ).read(OfflineReads.key(path, params)) { network() } else network()
    }

    private suspend fun checkedText(response: HttpResponse, sentToken: String = token): String {
        val text = response.bodyAsText()
        if (response.status.value !in 200..299) {
            if (response.status.value == 401 && token == sentToken) {
                token = "";
                offline?.current?.let { store?.removeProfile(it.owner) }
            }
            val message = runCatching {
                val o = json.parseToJsonElement(text).jsonObject;
                o["error"]?.jsonPrimitive?.content
                    ?: o["detail"]?.jsonPrimitive?.content
            }.getOrNull()
            val fields =
                runCatching { json.parseToJsonElement(text).jsonObject["errores"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } }.getOrNull()
                    .orEmpty()
            throw AppException(apiError(response.status.value, message, fields))
        };
        return text
    }

    internal suspend fun guideJson(
        path: String,
        body: String? = null,
        method: String = "GET",
        params: Map<String, String> = emptyMap(),
        onlineOnly: Boolean = false,
        version: String? = null
    ): String = request(path, body, method, params, onlineOnly, version)

    internal fun restoreOrigin(origin: String) {
        base = origin.trimEnd('/') + "/api/v1/";
        token = ""
    }

    internal suspend fun profile(): Access =
        json.decodeFromString(request("auth/me", onlineOnly = true))

    internal suspend fun guideBytes(path: String): ByteArray {
        val parts = path.split('/');

        val owner = offline?.current?.owner
        val guide = parts.getOrNull(1)?.toLongOrNull()
            ?.let { if (parts.firstOrNull() == "motocicletas") -it else it };

        val image = parts.getOrNull(3)?.toLongOrNull()
        if (offline?.status?.value?.offline == true && owner != null && guide != null && image != null) {
            offline.owner();
            return store?.image(owner, guide, image)
                ?: throw AppException(AppError.Network)
        }
        try {
            val response = try {
                http.get(base + path) { bearerAuth(token) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw AppException(AppError.Network)
            }
            if (response.status.value !in 200..299) checkedText(response)
            val bytes: ByteArray =
                response.body();
            if (owner != null && guide != null && image != null) store?.image(
                owner,
                guide,
                image,
                bytes
            )
            return if (owner != null && guide != null && image != null) store?.image(
                owner,
                guide,
                image
            ) ?: bytes else bytes
        } catch (e: AppException) {
            if (e.error == AppError.Network && owner != null && guide != null && image != null) {
                offline?.offline();
                return store?.image(owner, guide, image) ?: throw e
            };
            throw e
        }
    }

    internal suspend fun uploadImage(
        path: String,
        fields: Map<String, String>,
        photo: GuidePhoto
    ): String {
        if (offline?.status?.value?.offline == true || offline?.readOnly() == true) throw AppException(
            AppError.Network
        )
        val response = try {
            http.submitFormWithBinaryData(url = base + path, formData = formData {
                fields.forEach { (k, v) -> append(k, v) }
                append(
                    "imagen",
                    photo.bytes,
                    Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            photo.mime
                        );
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=imagen." + if (photo.mime == "image/png") "png" else "jpg"
                        )
                    })
            }) { bearerAuth(token) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppException(AppError.Network)
        }
        return checkedText(response)
    }

    internal suspend fun uploadGuide(
        guide: String,
        declared: Int,
        received: Int,
        note: String,
        photos: List<GuidePhoto>
    ): String {
        if (offline?.readOnly() == true) throw AppException(AppError.Unauthorized)
        val response = try {
            http.submitFormWithBinaryData(url = base + "llegadas/con-guia", formData = formData {
                append("numeroGuia", guide);
                append(
                    "cantidadDeclarada",
                    declared.toString()
                );
                append("cantidadRecibida", received.toString());
                append("observacion", note)
                photos.forEachIndexed { i, photo ->
                    append(
                        "imagenes",
                        photo.bytes,
                        Headers.build {
                            append(
                                HttpHeaders.ContentType,
                                photo.mime
                            );
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=guia-${i + 1}." + if (photo.mime == "image/png") "png" else "jpg"
                            )
                        })
                }
            }) { bearerAuth(token) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppException(AppError.Network)
        }
        return checkedText(response)
    }

    override suspend fun login(baseUrl: String, user: String, password: String): Access {
        require(Rules.server(baseUrl)) { "Ingresa una dirección HTTP o HTTPS" };
        base =
            baseUrl.trim().trimEnd('/') + "/api/v1/";
        token = ""
        val auth = json.decodeFromString<LoginResponse>(
            request(
                "auth/login",
                json.encodeToString(LoginRequest(user.trim(), password)),
                "POST"
            )
        );
        token = auth.accessToken
        try {
            val access = json.decodeFromString<Access>(request("auth/me"));
            offline?.online(
                baseUrl,
                access
            );
            return access
        } catch (e: CancellationException) {
            token = "";
            throw e
        } catch (e: Exception) {
            token = "";
            throw e
        }
    }

    override suspend fun arrivals(guide: String): List<JsonObject> =
        json.decodeFromString(request("llegadas", params = mapOf("numeroGuia" to guide.trim())))

    override suspend fun inventory(filter: InventoryFilter, page: Int): InventoryPage {
        try {
            if (offline?.status?.value?.offline == true) throw AppException(AppError.Network)
            val result = json.decodeFromString<InventoryPage>(
                request(
                    "motocicletas",
                    params = mapOf(
                        "chasis" to filter.chasis,
                        "marca" to filter.marca,
                        "modelo" to filter.modelo,
                        "color" to filter.color,
                        "pagina" to page.toString(),
                        "tamano" to "20"
                    ).filterValues { it.isNotBlank() })
            )
            val owner = offline?.current?.owner
            if (owner != null && offline?.status?.value?.offline == false) result.items.forEach { row ->
                val id = row["id"]!!.jsonPrimitive.content
                val old = store?.get(owner, "inventory-row", id)?.payload?.let {
                    json.parseToJsonElement(it).jsonObject
                }.orEmpty()
                store?.put(
                    owner,
                    "inventory-row",
                    id,
                    JsonObject(old + row).toString(),
                    epochMillis()
                )
            }
            return result
        } catch (e: AppException) {
            if (e.error != AppError.Network || store == null || offline == null) throw e
            val owner = offline.owner();

            val rows = store.all(owner, "inventory-row")
            if (rows.isEmpty()) {
                val key = OfflineReads.key("motocicletas", mapOf("pagina" to "0", "tamano" to "20"))
                if (store.get(owner, "http", key) == null) throw e
            }
            val matching =
                rows.map { json.parseToJsonElement(it.payload).jsonObject }.filter { row ->
                    fun text(key: String) = row[key]?.jsonPrimitive?.content.orEmpty()
                    listOf(
                        filter.marca to "marca",
                        filter.modelo to "modelo",
                        filter.color to "color"
                    ).all { (value, key) -> text(key).contains(value, true) } &&
                            (filter.chasis.isBlank() || text("chasisCompleto").contains(
                                filter.chasis,
                                true
                            ) || text("chasisVista").contains(filter.chasis, true))
                }.sortedBy { it["id"]!!.jsonPrimitive.long }
            return InventoryPage(
                matching.drop(page * 20).take(20),
                page,
                (matching.size + 19) / 20,
                matching.size.toLong()
            )
        }
    }

    override suspend fun detail(module: Module, id: Long): JsonObject {
        val path = when (module) {
            Module.ARRIVALS -> "llegadas/$id/detalle";
            Module.PERMISSIONS -> "usuarios/$id/permisos";
            else -> "motocicletas/$id"
        }
        val result = json.parseToJsonElement(request(path)).jsonObject
        if (module == Module.FICHAS && store != null && offline?.current != null && !offline.status.value.offline) store.put(
            offline.owner(),
            "inventory-row",
            id.toString(),
            JsonObject(result + ("chasisVista" to JsonPrimitive(result["chasisCompleto"]?.jsonPrimitive?.content.orEmpty()))).toString(),
            epochMillis()
        )
        return result
    }

    override suspend fun submit(path: String, body: JsonObject, method: String): JsonObject? {
        val text = request(path, body.toString(), method);

        val result = if (text.isBlank()) null else json.parseToJsonElement(text).jsonObject
        if (result != null && method == "PUT" && Regex("motocicletas/\\d+").matches(path) && store != null && offline?.current != null) {
            store.put(
                offline.owner(),
                "http",
                OfflineReads.key(path, emptyMap()),
                text,
                epochMillis()
            )
            store.put(
                offline.owner(),
                "inventory-row",
                result["id"]!!.jsonPrimitive.content,
                JsonObject(result + ("chasisVista" to JsonPrimitive(result["chasisCompleto"]?.jsonPrimitive?.content.orEmpty()))).toString(),
                epochMillis()
            )
        }
        return result
    }

    override suspend fun logout() {
        if (offline?.status?.value?.offline != true) request(
            "auth/logout",
            "{}",
            "POST"
        );
        clearSession()
    }

    override suspend fun currentLocation(id: Long): LocationRecord? =
        request("motocicletas/$id/ubicacion").takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<LocationRecord?>(it) }

    override suspend fun sites(): List<Site> = json.decodeFromString(request("ubicaciones"))

    override suspend fun registerLocation(id: Long, destinationId: Long): LocationRecord {
        val payload = buildJsonObject { put("destinoId", destinationId) }
        val response = try {
            request("motocicletas/$id/ubicacion", payload.toString(), "POST")
        } catch (exception: AppException) {
            if (exception.error is AppError.Conflict) {
                throw AppException(
                    AppError.Conflict(
                        "La motocicleta ya tiene ubicación. Actualiza la consulta para ver el destino guardado."
                    )
                )
            }
            throw exception
        }
        return json.decodeFromString(response)
    }

    override suspend fun catalog(): List<CatalogOption> =
        json.decodeFromString(request("catalogo-motocicletas"))

    override suspend fun requestOptions(): List<CatalogOption> =
        json.decodeFromString(request("solicitudes/opciones-motocicleta"))

    override suspend fun addCatalog(option: CatalogOption): CatalogOption {
        val saved = json.decodeFromString<CatalogOption>(
            request(
                "catalogo-motocicletas",
                json.encodeToString(option),
                "POST"
            )
        )
        if (store != null && offline?.current != null) {
            val key = OfflineReads.key("catalogo-motocicletas", emptyMap());

            val owner = offline.owner()
            store.get(owner, "http", key)?.let { old ->
                val rows = json.decodeFromString<List<CatalogOption>>(old.payload);
                store.put(
                    owner,
                    "http",
                    key,
                    json.encodeToString((rows + saved).distinct()),
                    epochMillis()
                )
            }
        }
        return saved
    }

    override suspend fun requests(page: Int): List<AssemblyRequest> = json.decodeFromString(
        request(
            "solicitudes",
            params = mapOf("pagina" to page.toString(), "tamano" to "20")
        )
    )

    override suspend fun request(id: Long): AssemblyRequest =
        json.decodeFromString(request("solicitudes/$id"))

    override suspend fun createRequest(body: NewRequest): AssemblyRequest =
        json.decodeFromString(request("solicitudes", json.encodeToString(body), "POST"))

    override suspend fun events(id: Long): List<RequestEvent> =
        json.decodeFromString(request("solicitudes/$id/seguimiento"))

    override suspend fun advance(id: Long, status: String, reason: String) {
        request(
            "solicitudes/$id/seguimiento",
            buildJsonObject {
                put("estado", status);
                put("motivo", reason)
            }.toString(),
            "POST"
        )
    }

    override suspend fun cancel(id: Long, reason: String) {
        request(
            "solicitudes/$id/anulacion",
            buildJsonObject { put("motivo", reason) }.toString(),
            "POST"
        )
    }

    override suspend fun eligible(chassis: String): List<EligibleUnit> = json.decodeFromString(
        request(
            "motocicletas/elegibles",
            params = mapOf("chasis" to chassis)
        )
    )

    override suspend fun assigned(id: Long, line: Long): List<AssignedUnit> =
        json.decodeFromString(request("solicitudes/$id/lineas/$line/unidades"))

    override suspend fun assign(id: Long, line: Long, motorcycle: Long) {
        request(
            "solicitudes/$id/lineas/$line/unidades",
            buildJsonObject { put("motocicletaId", motorcycle) }.toString(),
            "POST"
        )
    }

    override suspend fun remove(id: Long, line: Long, association: Long, reason: String) {
        request(
            "solicitudes/$id/lineas/$line/unidades/$association",
            buildJsonObject { put("motivo", reason) }.toString(),
            "DELETE"
        )
    }

    override suspend fun transfer(motorcycle: Long, destination: Long): TransferRecord =
        json.decodeFromString(
            request(
                "motocicletas/$motorcycle/traslados",
                buildJsonObject { put("destinoId", destination) }.toString(),
                "POST"
            )
        )

    override suspend fun transfers(motorcycle: Long): List<TransferRecord> =
        json.decodeFromString(request("motocicletas/$motorcycle/ubicacion/historial"))

    override fun clearSession() {
        token = "";
        offline?.close()
    }

    override suspend fun catalogState(id: Long, active: Boolean): CatalogOption {
        val saved = json.decodeFromString<CatalogOption>(
            request(
                "catalogo-motocicletas/$id/estado",
                buildJsonObject { put("activo", active) }.toString(),
                "PUT",
                onlineOnly = true
            )
        )
        return saved
    }
}
