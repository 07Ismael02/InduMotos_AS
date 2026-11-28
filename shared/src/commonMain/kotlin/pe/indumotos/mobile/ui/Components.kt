package pe.indumotos.mobile.ui


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*


internal val Ink = Color(0xFF142E49)
internal val Yellow = Color(0xFFEEE51B)
internal val Blue: Color @Composable get() = MaterialTheme.colorScheme.secondary
internal val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
internal fun JsonObject.text(key: String) = get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
internal fun icon(m: Module) = when (m) {
    Module.REQUESTS -> Icons.Default.Assignment;
    Module.TRANSFERS -> Icons.Default.LocalShipping;
    Module.INVENTORY -> Icons.Default.Inventory2;
    Module.ARRIVALS -> Icons.Default.LocalShipping;
    Module.FICHAS -> Icons.Default.Description
}

internal fun createForm(module: Module): Form = when (module) {
    Module.REQUESTS, Module.TRANSFERS -> error("Operación mediante su pantalla específica")
    Module.INVENTORY, Module.FICHAS -> Form(
        "Registrar ficha de motocicleta",
        "motocicletas",
        listOf(
            Field("llegadaId", "ID de llegada", numeric = true),
            Field("marca", "Marca"),
            Field("modelo", "Modelo"),
            Field("color", "Color"),
            Field("chasisCompleto", "Chasis completo"),
            Field("numeroMotor", "Número de motor"),
            Field("dua", "DUA"),
            Field("itemDua", "Ítem DUA")
        )
    )

    Module.ARRIVALS -> Form(
        "Registrar llegada",
        "llegadas",
        listOf(
            Field("numeroGuia", "Número de guía"),
            Field("cantidadDeclarada", "Cantidad declarada", numeric = true),
            Field("cantidadRecibida", "Cantidad recibida", numeric = true),
            Field("observacion", "Observación (opcional)", required = false, maxLength = 1000)
        )
    )
}

// A theme-aware wordmark; the original bitmap remains in resources unchanged.
@Composable
internal fun Brand(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "INDUMOTOS",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            maxLines = 1
        )
        Row(
            Modifier.width(220.dp).padding(top = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(Modifier.weight(1f).height(3.dp).background(Yellow))
            Box(Modifier.weight(1f).height(3.dp).background(Blue))
            Box(Modifier.weight(1f).height(3.dp).background(Yellow))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
internal fun EmptyState(title: String, copy: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, null, tint = Blue);
        Spacer(Modifier.height(12.dp));
        Text(
            title,
            fontWeight = FontWeight.SemiBold
        );
        Spacer(Modifier.height(8.dp));
        Text(
            copy,
            color = Muted,
            fontSize = 12.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
internal fun Notice(message: String, error: Boolean) {
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            message,
            Modifier.padding(14.dp),
            fontSize = 12.sp,
            lineHeight = 19.sp,
            color = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
internal fun OptionSelect(
    label: String,
    options: List<String>,
    value: String,
    change: (String) -> Unit,
    labels: Map<String, String> = emptyMap()
) {
    var expanded by remember { mutableStateOf(false) };
    Column {
        Text(
            label,
            fontSize = 12.sp,
            color = Muted
        );
        Spacer(Modifier.height(5.dp));
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(labels[value] ?: value, modifier = Modifier.weight(1f));
                Icon(
                    Icons.Default.ExpandMore,
                    null
                )
            };
            DropdownMenu(expanded, { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(labels[option] ?: option) },
                        onClick = {
                            change(option);
                            expanded = false
                        })
                }
            }
        }
    }
}

@Composable
internal fun FormDialog(
    form: Form,
    busy: Boolean,
    error: String?,
    close: () -> Unit,
    submit: (Map<String, String>) -> Unit
) {
    val values = remember(form) {
        mutableStateMapOf<String, String>().apply {
            form.fields.forEach {
                put(
                    it.key,
                    it.options.firstOrNull().orEmpty()
                )
            }
        }
    };

    var local by remember(form) { mutableStateOf<String?>(null) };

    var confirmed by remember(form) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!busy) close() },
        title = { Text(form.title, fontSize = 20.sp) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (form.sensitive) {
                    Text(
                        "Este cambio modifica el acceso de la cuenta. Revisa el identificador y la operación antes de confirmar.",
                        fontSize = 12.sp
                    );
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            confirmed,
                            { confirmed = it },
                            enabled = !busy
                        );
                        Text("Confirmo el cambio de acceso", fontSize = 12.sp)
                    }
                }
                form.fields.forEach { f ->
                    if (f.options.isNotEmpty()) OptionSelect(
                        f.label,
                        f.options,
                        values[f.key].orEmpty(),
                        { if (!busy) values[f.key] = it },
                        f.labels
                    ) else OutlinedTextField(
                        values[f.key].orEmpty(),
                        { if (it.length <= f.maxLength) values[f.key] = it },
                        label = { Text(f.label) },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (f.secret) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = if (f.numeric) KeyboardType.Number else if (f.secret) KeyboardType.Password else KeyboardType.Text),
                        singleLine = f.key != "observacion"
                    )
                }
                (local ?: error)?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    local =
                        if (form.fields.any { it.required && values[it.key].isNullOrBlank() }) "Completa los campos obligatorios" else null;
                    if (local == null) submit(
                        values.toMap()
                    )
                },
                enabled = !busy && (!form.sensitive || confirmed)
            ) { Text(if (busy) "Guardando…" else if (form.sensitive) "Confirmar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Cancelar") } })
}
