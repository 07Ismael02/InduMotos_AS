package pe.indumotos.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.serialization.json.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.VehicleViewModel

@Composable
internal fun VehicleExtras(
    id: Long,
    account: Access,
    offline: Boolean,
    pick: ((PhotoSelection) -> Unit) -> Unit,
    expired: () -> Unit
) {
    val vm: VehicleViewModel = koinViewModel(key = "ficha-$id-${account.usuarioId}");

    val s by vm.state.collectAsState()
    var showJourney by remember(id) { mutableStateOf(false) }
    var edit by remember(id) { mutableStateOf(false) };

    var change by remember(id) { mutableStateOf<String?>(null) };

    var reason by remember(id) { mutableStateOf("") };

    var selectionError by remember(id) { mutableStateOf<String?>(null) }
    fun can(code: String) = account.permisos.any { it.codigo == code && it.permitido }
    DisposableEffect(id, account.usuarioId) { onDispose { vm.reset() } }
    LaunchedEffect(id, account.usuarioId) { vm.load(id, can("DISPONIBILIDAD_CONSULTAR")) }
    LaunchedEffect(s.expired) { if (s.expired) expired() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        (selectionError ?: s.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        s.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (s.image != null) {
            val bitmap = remember(s.image) { decodeGuideImage(s.image!!) };
            bitmap?.let {
                Image(
                    it,
                    "Fotografía adjunta de la motocicleta",
                    Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } else OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Sin fotografía de esta unidad",
                    style = MaterialTheme.typography.titleMedium
                );
                Text(
                    "Adjunta fotos propias para identificarla.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (s.card?.fotos?.isNotEmpty() == true) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.photo(s.index - 1) },
                enabled = !s.busy && s.index > 0
            ) { Text("Anterior") }
            Text("${s.index + 1} / ${s.card!!.fotos.size}", Modifier.padding(top = 12.dp))
            OutlinedButton(
                onClick = { vm.photo(s.index + 1) },
                enabled = !s.busy && s.index < s.card!!.fotos.lastIndex
            ) { Text("Siguiente") }
        }
        if (!offline && can("FICHAS_EDITAR")) OutlinedButton(onClick = {
            pick { result ->
                selectionError =
                    result.error;
                if (result.photos.size == 1) vm.upload(result.photos.first()) else if (result.photos.isNotEmpty()) selectionError =
                    "Selecciona una sola foto por envío."
            }
        }, enabled = !s.busy && (s.card?.fotos?.size ?: 0) < 3) { Text("Adjuntar fotografía") }
        s.availability?.let { a ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(vehicleStateLabel(a.estado), style = MaterialTheme.typography.titleMedium)
                    Text(a.motivo);
                    a.ubicacion?.let { Text("Ubicación: $it") }
                    Text(
                        if (a.puedeOfrecerse) "Disponible para ofrecer" else "No disponible para venta",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!offline && can("DISPONIBILIDAD_CAMBIAR") && a.estado !in listOf(
                            "VENDIDA",
                            "EN_TRANSITO"
                        )
                    ) OutlinedButton(
                        onClick = {
                            reason = "";
                            change =
                                if (a.estado == "BLOQUEADA") "HABILITADA" else "BLOQUEADA"
                        },
                        enabled = !s.busy
                    ) { Text(if (a.estado == "BLOQUEADA") "Reactivar motocicleta" else "Desactivar motocicleta") }
                }
            }
        }
        if (can(READ_LOCATIONS)) OutlinedButton(
            onClick = {
                showJourney = true;
                vm.journey()
            },
            enabled = !s.busy
        ) { Text("Ver recorrido completo") }
        s.card?.datos?.let { d ->
            DetailGroup("Características") {
                DetailValue("Año", d.anio?.toString());
                DetailValue(
                    "Cilindrada",
                    d.cilindrada?.let { "$it cc" });
                DetailValue(
                    "Transmisión",
                    d.transmision?.takeIf { it.isNotBlank() });
                DetailValue(
                    "Combustible",
                    d.combustible?.takeIf { it.isNotBlank() });
                DetailValue(
                    "Descripción",
                    d.descripcion?.takeIf { it.isNotBlank() })
                if (!offline && can("FICHAS_EDITAR")) OutlinedButton(
                    onClick = { edit = true },
                    enabled = !s.busy
                ) { Text("Editar características") }
            }
        }
    }
    if (showJourney) AlertDialog(
        onDismissRequest = { showJourney = false },
        title = { Text("Recorrido completo") },
        text = {
            LazyColumn(
                Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (s.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                s.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                s.journey?.let { j ->
                    item { Text("Ubicación: " + j["ubicacionActual"]?.jsonPrimitive?.contentOrNull.orEmpty()) }
                    items(j["eventos"]?.jsonArray.orEmpty()) { event ->
                        val e = event.jsonObject;
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    e["titulo"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(e["detalle"]?.jsonPrimitive?.contentOrNull.orEmpty())
                                Text(
                                    listOfNotNull(
                                        e["origen"]?.jsonPrimitive?.contentOrNull,
                                        e["destino"]?.jsonPrimitive?.contentOrNull
                                    ).joinToString(" → ")
                                )
                                Text(
                                    e["responsable"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    e["fecha"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showJourney = false }) { Text("Cerrar") } })
    if (edit && s.card != null) VehicleSpecsDialog(s.card!!.datos, { edit = false }) {
        edit = false;
        vm.save(it)
    }
    change?.let { target ->
        AlertDialog(
            onDismissRequest = { change = null },
            title = { Text(if (target == "HABILITADA") "Reactivar motocicleta" else "Desactivar motocicleta") },
            text = {
                Column {
                    Text("Se conservarán la ficha y su historial. Indica el motivo.");
                    OutlinedTextField(
                        reason,
                        { if (it.length <= 500) reason = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        change = null;
                        vm.change(target, reason)
                    },
                    enabled = reason.isNotBlank() && !s.busy
                ) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { change = null }) { Text("Cancelar") } })
    }
}

internal fun vehicleStateLabel(s: String) = mapOf(
    "DISPONIBLE" to "Disponible",
    "BLOQUEADA" to "Inactiva · bloqueada",
    "EN_REVISION" to "En revisión",
    "VENDIDA" to "Vendida",
    "ASIGNADA" to "Asignada",
    "EN_TRANSITO" to "En tránsito",
    "SIN_UBICACION" to "Sin ubicación",
    "PAGO_PENDIENTE" to "Pago pendiente de revisión"
)[s] ?: s.replace('_', ' ')

@Composable
private fun VehicleSpecsDialog(d: VehicleSpecs, close: () -> Unit, save: (VehicleSpecs) -> Unit) {
    var year by remember { mutableStateOf(d.anio?.toString().orEmpty()) };

    var cc by remember { mutableStateOf(d.cilindrada?.toString().orEmpty()) };

    var transmission by remember { mutableStateOf(d.transmision.orEmpty()) };

    var fuel by remember { mutableStateOf(d.combustible.orEmpty()) };

    var description by remember { mutableStateOf(d.descripcion.orEmpty()) };

    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = close, title = { Text("Características") }, text = {
        Column(
            Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Registra únicamente datos conocidos. Los campos son opcionales.")
            OutlinedTextField(
                year,
                { if (it.length <= 4 && it.all(Char::isDigit)) year = it },
                label = { Text("Año") },
                singleLine = true
            )
            OutlinedTextField(
                cc,
                { if (it.length <= 4 && it.all(Char::isDigit)) cc = it },
                label = { Text("Cilindrada en cc") },
                singleLine = true
            )
            SimpleChoice(
                "Transmisión",
                transmission,
                listOf("Manual", "Automática", "Semiautomática"),
                true
            ) { transmission = it }
            SimpleChoice(
                "Combustible",
                fuel,
                listOf("Gasolina", "Eléctrica", "Híbrida"),
                true
            ) { fuel = it }
            OutlinedTextField(
                description,
                { if (it.length <= 2000) description = it },
                label = { Text("Descripción") })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Button(onClick = {
            val y = year.toIntOrNull();

            val c =
                cc.toIntOrNull();
            if ((year.isNotBlank() && (y == null || y !in 1900..2100)) || (cc.isNotBlank() && (c == null || c !in 1..5000))) error =
                "Revisa año y cilindrada" else save(
                VehicleSpecs(
                    y,
                    c,
                    transmission,
                    fuel,
                    description,
                    d.version
                )
            )
        }) { Text("Guardar") }
    }, dismissButton = { TextButton(onClick = close) { Text("Cancelar") } })
}

@Composable
internal fun SimpleChoice(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    change: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (value.isBlank()) "Seleccionar $label" else "$label: $value") };
        DropdownMenu(
            expanded,
            { expanded = false }) {
            options.forEach { v ->
                DropdownMenuItem(
                    text = { Text(v) },
                    onClick = {
                        expanded = false;
                        change(v)
                    })
            }
        }
    }
}
