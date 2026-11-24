package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*

@Composable
internal fun EditMotorcycleDialog(
    record: JsonObject,
    options: List<CatalogOption>,
    busy: Boolean,
    error: String?,
    close: () -> Unit,
    save: (MotorcycleDraft) -> Unit
) {
    fun value(key: String) = record[key]?.jsonPrimitive?.content.orEmpty()
    var draft by remember(record) {
        mutableStateOf(
            MotorcycleDraft(
                1,
                value("marca"),
                value("modelo"),
                value("color"),
                value("chasisCompleto"),
                value("numeroMotor"),
                value("dua"),
                value("itemDua")
            )
        )
    }
    var local by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!busy) close() },
        title = { Text("Editar ficha de motocicleta") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Verifica los datos contra la guía. La actualización conserva el identificador y la recepción vinculada.")
                DraftEditor(draft, options, busy) { draft = it }
                (local ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                local = runCatching {
                    GuideRules.draft(
                        draft,
                        options
                    )
                }.exceptionOrNull()?.message;
                if (local == null) save(draft)
            }, enabled = !busy) { Text("Guardar cambios") }
        },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Cancelar") } })
}
