package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import pe.indumotos.mobile.domain.limaTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MotorcycleDetail(
    record: JsonObject,
    busy: Boolean,
    close: () -> Unit,
    extras: @Composable () -> Unit = {},
    location: @Composable () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember(record) { mutableStateOf(false) }
    val title = listOfNotNull(record.value("marca"), record.value("modelo")).joinToString(" ")
    Dialog(
        onDismissRequest = close,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("Ficha de motocicleta") },
                    navigationIcon = {
                        IconButton(onClick = close, enabled = !busy) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { insets ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(insets),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null)
                                Text(
                                    title.ifBlank { "Motocicleta" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            SelectionContainer {
                                DetailValue(
                                    "Chasis completo",
                                    record.value("chasisCompleto")
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(
                                        AnnotatedString(
                                            record.value(
                                                "chasisCompleto"
                                            ).orEmpty()
                                        )
                                    );
                                    copied = true
                                },
                                enabled = !record.value("chasisCompleto").isNullOrBlank()
                            ) { Text(if (copied) "Chasis copiado" else "Copiar chasis completo") }
                            DetailValue("Color", record.value("color"))
                        }
                    }
                }
                item { extras() }
                item { location() }
                item {
                    DetailGroup("Motor y documentación") {
                        DetailValue("Número de motor", record.value("numeroMotor"))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.weight(2f)) { DetailValue("DUA", record.value("dua")) }
                            Box(Modifier.weight(1f)) {
                                DetailValue(
                                    "Ítem DUA",
                                    record.value("itemDua")
                                )
                            }
                        }
                    }
                }
                item {
                    DetailGroup("Registro de ingreso") {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.weight(1f)) {
                                DetailValue(
                                    "Número de ficha",
                                    record.value("id")
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                DetailValue(
                                    "Llegada vinculada",
                                    record.value("llegadaId")
                                )
                            }
                        }
                        DetailValue("Registró", record.value("creadorUsuario"))
                        DetailValue(
                            "Fecha de registro",
                            record.value("creadoEn")?.let(::recordTimestamp)
                        )
                        (record["llegada"] as? JsonObject)?.let { arrival ->
                            DetailValue("Guía de remisión", arrival.value("numeroGuia"))
                            DetailValue(
                                "Fecha de recepción",
                                arrival.value("fechaLlegada")?.let(::recordTimestamp)
                            )
                            DetailValue("Recibió", arrival.value("receptorUsuario"))
                            DetailValue(
                                "Cantidades",
                                "Declaradas: ${arrival.value("cantidadDeclarada")} · Recibidas: ${
                                    arrival.value("cantidadRecibida")
                                }"
                            )
                            DetailValue("Observación", arrival.value("observacion"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
internal fun DetailValue(label: String, value: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value ?: "Sin información", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun JsonObject.value(key: String): String? =
    (get(key) as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

internal fun recordTimestamp(value: String): String = limaTimestamp(value)
