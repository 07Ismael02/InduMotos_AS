package pe.indumotos.mobile.data

import kotlinx.coroutines.CancellationException
import pe.indumotos.mobile.domain.*

/** Cache only operational reads; never auth, staff administration or OCR execution. */
class OfflineReads(private val session: OfflineSession, private val store: OfflineStore) {
    suspend fun read(key: String, network: suspend () -> String): String {
        val profile = session.current ?: return network()
        val owner = session.owner()
        if (session.readOnly() || session.status.value.offline) return store.get(
            owner,
            "http",
            key
        )?.payload ?: throw AppException(AppError.Network)
        return try {
            val value = network();

            val time = epochMillis();
            store.put(owner, "http", key, value, time)
            if (session.current?.owner == owner) session.downloaded(time)
            // Return the committed local value, not a separate UI copy.
            requireNotNull(store.get(owner, "http", key)).payload
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            when (e.error) {
                AppError.Network -> {
                    if (session.current?.owner == owner) session.offline();
                    store.get(
                        owner,
                        "http",
                        key
                    )?.payload ?: throw e
                }

                AppError.Forbidden, AppError.Unauthorized -> {
                    store.remove(owner, "http", key);
                    throw e
                }

                else -> throw e
            }
        }
    }

    companion object {
        fun eligible(path: String) =
            path == "catalogo-motocicletas" || path == "motocicletas" || Regex("motocicletas/\\d+").matches(
                path
            ) || path == "ubicaciones" || Regex("motocicletas/\\d+/ubicacion(?:/historial)?").matches(
                path
            ) ||
                    Regex("motocicletas/\\d+/(?:ficha-ampliada|recorrido|disponibilidad(?:/historial)?)").matches(
                        path
                    ) || path == "llegadas/pagina" || Regex("llegadas/\\d+(?:/detalle|/registro|/guias)?").matches(
                path
            )

        fun key(path: String, params: Map<String, String>) =
            path + "|" + params.entries.sortedBy { it.key }
                .joinToString("") { (k, v) -> "${k.length}:$k${v.length}:$v" }
    }
}
