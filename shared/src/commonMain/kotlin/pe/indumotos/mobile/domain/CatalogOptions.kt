package pe.indumotos.mobile.domain

import kotlinx.serialization.json.*

fun catalogOptions(
    rows: List<JsonObject>,
    field: String,
    brand: String = "",
    model: String = ""
): List<String> =
    rows.filter { row ->
        (brand.isBlank() || row["marca"]?.jsonPrimitive?.content.orEmpty().equals(brand, true)) &&
                (model.isBlank() || row["modelo"]?.jsonPrimitive?.content.orEmpty()
                    .equals(model, true))
    }.mapNotNull { it[field]?.jsonPrimitive?.content?.trim()?.takeIf(String::isNotEmpty) }
        .distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
