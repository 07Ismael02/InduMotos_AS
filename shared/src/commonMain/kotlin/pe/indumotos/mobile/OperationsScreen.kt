package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*
import kotlinx.serialization.json.*

private fun Access.permits(code: String) = permisos.any { it.codigo == code && it.permitido }

@Composable
private fun OperationMessages(state: OperationsState) {
    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
    }
    state.message?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun RequestsScreen(account: Access, expired: () -> Unit) {
    val vm: OperationsViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var creating by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<String?>(null) }
    var removal by remember { mutableStateOf<AssignedUnit?>(null) }
    var chassis by remember(state.line?.id) { mutableStateOf("") }
    LocalSystemBack.current(state.selected != null && !state.busy) {
        if (state.line != null) vm.closeLine() else vm.list(state.page)
    }
    DisposableEffect(account.usuarioId) { onDispose { vm.reset() } }
    LaunchedEffect(account.usuarioId) {
        vm.reset();
        vm.list()
    }
    LaunchedEffect(state.expired) { if (state.expired) expired() }
    LaunchedEffect(state.selected?.id) { if (state.selected != null) creating = false }
    Column(Modifier.fillMaxSize()) {
        if (state.selected != null) Surface(tonalElevation = 2.dp) {
            OutlinedButton(
                onClick = { vm.list(state.page) },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp)
            ) { Text("← Volver a solicitudes") }
        }
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val selected = state.selected
            if (selected == null) {
                item {
                    OperationMessages(state)
                    Text(
                        "Solicitudes de ensamblaje",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Solicita modelos y cantidades; consulta su atención y los chasis vinculados.",
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { vm.list(state.page) },
                            enabled = !state.busy
                        ) { Text("Actualizar") }
                        if (account.permits(CREATE_REQUESTS)) Button(onClick = {
                            creating = true;
                            vm.prepareRequest()
                        }, enabled = !state.busy) { Text("Nueva solicitud") }
                    }
                }
                items(state.requests, key = { it.id }) { request ->
                    OutlinedCard(
                        onClick = { if (!state.busy) vm.select(request.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Solicitud #${request.id}", fontWeight = FontWeight.SemiBold)
                            Text(
                                statusLabel(request.estado),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "${request.lineas.size} líneas · ${request.lineas.sumOf { it.cantidad }} unidades",
                                fontSize = 13.sp
                            )
                            Text(
                                "Por ${request.solicitante} · ${request.destinoNombre.ifBlank { "Destino #${request.destinoId}" }}",
                                fontSize = 12.sp
                            )
                            Text("Ver detalle →", fontSize = 12.sp)
                        }
                    }
                }
                if (state.requests.isEmpty() && !state.busy && state.error == null) item { Text("No hay solicitudes en esta página.") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { vm.list(state.page - 1) },
                            enabled = !state.busy && state.page > 0
                        ) { Text("Anterior") }
                        Text(
                            "Página ${state.page + 1}",
                            Modifier.padding(top = 14.dp),
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { vm.list(state.page + 1) },
                            enabled = !state.busy && state.requests.size == 20
                        ) { Text("Siguiente") }
                    }
                }
            } else {
                item {
                    OperationMessages(state)
                    Text(
                        "Solicitud #${selected.id}",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(statusLabel(selected.estado), color = MaterialTheme.colorScheme.secondary)
                    Text(
                        "Solicitante: ${selected.solicitante}\nDestino: ${selected.destinoNombre.ifBlank { "#${selected.destinoId}" }}\nCreada: ${selected.creadaEn}",
                        fontSize = 12.sp
                    )
                    selected.observacion?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    OutlinedButton(
                        onClick = { vm.select(selected.id) },
                        enabled = !state.busy
                    ) { Text("Actualizar detalle") }
                    if (account.permits(ADVANCE_REQUESTS)) OperationRules.transitions(selected.estado)
                        .forEach { next ->
                            if (next == "RECHAZADA") OutlinedButton(
                                onClick = { action = next },
                                enabled = !state.busy,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Rechazar solicitud") }
                            else Button(
                                onClick = { action = next },
                                enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (next == "EN_PREPARACION") "Iniciar preparación" else "Marcar como atendida") }
                        }
                    if (account.permits(CANCEL_REQUESTS) && selected.estado == "PENDIENTE")
                        OutlinedButton(
                            onClick = { action = "ANULADA" },
                            enabled = !state.busy,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Anular con motivo") }
                    Text(
                        "Modelos solicitados",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
                items(selected.lineas, key = { it.id }) { line ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "${line.marca} ${line.modelo} · ${line.color}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Cantidad solicitada: ${line.cantidad}")
                            OutlinedButton(
                                onClick = { vm.line(line) },
                                enabled = !state.busy
                            ) { Text(if (selected.estado == "EN_PREPARACION") "Elegir motocicletas para este modelo" else "Ver motocicletas asignadas") }
                        }
                    }
                }
                state.line?.let { line ->
                    item {
                        Text(
                            "${line.marca} ${line.modelo} · ${line.color}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${state.assigned.size} de ${line.cantidad} motocicletas asignadas",
                            fontSize = 12.sp
                        )
                        Text(
                            "Primero inicia la preparación. Después elige los chasis concretos que cubrirán este modelo y color.",
                            fontSize = 12.sp
                        )
                    }
                    items(state.assigned, key = { it.asociacionId }) { unit ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(unit.chasis, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Moto #${unit.motocicletaId} · ${unit.asignador}\n${unit.fecha}",
                                    fontSize = 12.sp
                                )
                                if (account.permits(ASSIGN_UNITS) && selected.estado == "EN_PREPARACION")
                                    TextButton(
                                        onClick = { removal = unit },
                                        enabled = !state.busy
                                    ) { Text("Retirar con motivo") }
                            }
                        }
                    }
                    if (account.permits(ASSIGN_UNITS) && selected.estado == "EN_PREPARACION") item {
                        OutlinedTextField(
                            chassis,
                            { chassis = it },
                            label = { Text("Buscar chasis elegible") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.busy
                        )
                        Button(
                            onClick = { vm.search(chassis) },
                            enabled = !state.busy && chassis.isNotBlank()
                        ) { Text("Buscar unidades") }
                        Text(
                            "El servidor verifica coincidencia, cupo y exclusividad al asignar.",
                            fontSize = 12.sp
                        )
                    }
                    items(state.eligible, key = { it.motocicletaId }) { unit ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(unit.chasis);
                                Text(
                                    "${unit.marca} ${unit.modelo} · ${unit.color}",
                                    fontSize = 12.sp
                                )
                                Button(
                                    onClick = { vm.assign(unit) },
                                    enabled = !state.busy && state.assigned.size < line.cantidad
                                ) { Text("Asignar esta motocicleta") }
                            }
                        }
                    }
                }
                item { Text("Historial de la solicitud", fontWeight = FontWeight.SemiBold) }
                items(state.events, key = { it.id }) { event ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(statusLabel(event.estado), fontWeight = FontWeight.SemiBold)
                            event.motivo?.let { Text(it) }
                            Text("${event.registrador} · ${event.fecha}", fontSize = 12.sp)
                        }
                    }
                }
                if (state.events.isEmpty()) item {
                    Text(
                        "No se ha cargado el historial; actualiza el detalle.",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
    if (creating) NewRequestDialog(
        state.sites,
        state.catalog,
        state.busy,
        state.error,
        { creating = false },
        vm::create
    )
    action?.let { next ->
        ReasonDialog(
            "Confirmar: ${statusLabel(next)}",
            state.busy,
            next in listOf("RECHAZADA", "ANULADA"),
            { action = null }) { reason ->
            action = null
            if (next == "ANULADA") vm.cancel(reason) else vm.advance(next, reason)
        }
    }
    removal?.let { unit ->
        ReasonDialog(
            "Retirar chasis ${unit.chasis}",
            state.busy,
            true,
            { removal = null }) { reason ->
            removal = null;
            vm.remove(unit, reason)
        }
    }
}

private data class DraftLine(
    val key: Int,
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val quantity: String = "1"
)

@Composable
private fun NewRequestDialog(
    sites: List<Site>,
    catalog: List<JsonObject>,
    busy: Boolean,
    error: String?,
    close: () -> Unit,
    submit: (NewRequest) -> Unit
) {
    val lines = remember { mutableStateListOf(DraftLine(0)) };

    var nextKey by remember { mutableStateOf(1) }
    var destination by remember { mutableStateOf<Site?>(null) };

    var observation by remember { mutableStateOf("") };

    var local by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!busy) close() },
        title = { Text("Nueva solicitud") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SitePicker(sites, destination, { destination = it }, busy)
                Text(
                    "Selecciona las combinaciones del catálogo administrado por la empresa.",
                    fontSize = 12.sp
                )
                if (sites.isEmpty()) Text(
                    "No hay sedes disponibles. Registra una sede válida o revisa tu permiso de consulta.",
                    fontSize = 12.sp
                )
                lines.forEachIndexed { index, line ->
                    key(line.key) {
                        HorizontalDivider();
                        Text(
                            "Línea ${index + 1}",
                            fontWeight = FontWeight.SemiBold
                        )
                        CatalogPicker(
                            "Marca",
                            line.brand,
                            catalogOptions(catalog, "marca"),
                            busy
                        ) { lines[index] = line.copy(brand = it, model = "", color = "") }
                        key(line.brand) {
                            CatalogPicker(
                                "Modelo",
                                line.model,
                                catalogOptions(catalog, "modelo", line.brand),
                                busy || line.brand.isBlank()
                            ) { lines[index] = line.copy(model = it, color = "") }
                        }
                        key(line.brand, line.model) {
                            CatalogPicker(
                                "Color",
                                line.color,
                                catalogOptions(catalog, "color", line.brand, line.model),
                                busy || line.brand.isBlank() || line.model.isBlank()
                            ) { lines[index] = line.copy(color = it) }
                        }
                        OutlinedTextField(
                            line.quantity,
                            { lines[index] = line.copy(quantity = it) },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            enabled = !busy,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (lines.size > 1) TextButton(
                            onClick = { lines.removeAt(index) },
                            enabled = !busy
                        ) { Text("Quitar línea ${index + 1}") }
                    }
                }
                OutlinedButton(
                    onClick = { lines.add(DraftLine(nextKey++)) },
                    enabled = !busy
                ) { Text("Agregar otra línea") }
                OutlinedTextField(
                    observation,
                    { if (it.length <= 1000) observation = it },
                    label = { Text("Observación opcional") },
                    enabled = !busy
                )
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
            TextButton(onClick = {
                val body = NewRequest(
                    destination?.id ?: 0,
                    observation,
                    lines.map {
                        NewRequestLine(
                            it.brand.trim(),
                            it.model.trim(),
                            it.color.trim(),
                            it.quantity.toIntOrNull() ?: 0
                        )
                    })
                local = runCatching { OperationRules.validate(body) }.exceptionOrNull()?.message
                if (local == null) submit(body)
            }, enabled = !busy) { Text(if (busy) "Enviando…" else "Crear solicitud") }
        },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Cancelar") } })
}

@Composable
private fun CatalogPicker(
    label: String,
    value: String,
    options: List<String>,
    busy: Boolean,
    change: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Box {
            OutlinedCard(
                onClick = { expanded = true },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            label,
                            fontSize = 12.sp
                        );
                        Text(value.ifBlank { "Seleccionar" })
                    }
                    Text("▾")
                }
            }
            DropdownMenu(
                expanded && !busy,
                { expanded = false },
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            change(option);
                            expanded = false
                        })
                }
                if (options.isEmpty()) DropdownMenuItem(
                    text = { Text("Sin opciones registradas") },
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun ReasonDialog(
    title: String,
    busy: Boolean,
    required: Boolean,
    close: () -> Unit,
    submit: (String) -> Unit
) {
    var reason by remember(title) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!busy) close() },
        title = { Text(title) },
        text = {
            Column {
                Text(
                    "La operación se registra en el servidor. Revisa la solicitud antes de confirmar.",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    reason,
                    { if (it.length <= 1000) reason = it },
                    label = { Text(if (required) "Motivo obligatorio" else "Motivo opcional") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { submit(reason) },
                enabled = !busy && (!required || reason.isNotBlank())
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Cancelar") } })
}

@Composable
private fun SitePicker(sites: List<Site>, selected: Site?, select: (Site) -> Unit, busy: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = !busy && sites.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(selected?.let { "${it.codigo} · ${it.nombre}" } ?: "Seleccionar sede de destino") }
        DropdownMenu(
            expanded,
            {
                expanded = false
            }) {
            sites.forEach { site ->
                DropdownMenuItem(
                    text = { Text("${site.codigo} · ${site.nombre} (${site.tipo.lowercase()})") },
                    onClick = {
                        select(site);
                        expanded = false
                    })
            }
        }
    }
}

@Composable
fun TransfersScreen(account: Access, expired: () -> Unit) {
    val vm: OperationsViewModel = koinViewModel(key = "traslados-${account.usuarioId}");

    val state by vm.state.collectAsState()
    var motorcycle by remember { mutableStateOf("") };

    var loadedId by remember { mutableStateOf<Long?>(null) }
    var destination by remember(loadedId) { mutableStateOf<Site?>(null) };

    var confirm by remember { mutableStateOf(false) }
    LocalSystemBack.current(loadedId != null && !state.busy) {
        loadedId = null;
        destination = null
    }
    DisposableEffect(account.usuarioId) { onDispose { vm.reset() } }
    LaunchedEffect(account.usuarioId) {
        vm.reset();
        if (account.permits("INVENTARIO_CONSULTAR")) vm.motorcycles()
    }
    LaunchedEffect(state.expired) { if (state.expired) expired() }
    LaunchedEffect(state.movements.lastOrNull()?.id) {
        destination = null;
        confirm = false
    }
    Column(Modifier.fillMaxSize()) {
        OperationMessages(state)
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (loadedId == null) item {
                Text("Traslado de motocicletas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Consulta el recorrido antes de trasladar a una tienda o despacho activo.",
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    motorcycle,
                    {
                        motorcycle = it;
                        loadedId = null;
                        destination = null;
                        confirm = false
                    },
                    label = { Text("Buscar por chasis completo o últimos 4 dígitos") },
                    enabled = !state.busy,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            loadedId = null;
                            vm.motorcycles(motorcycle)
                        },
                        enabled = !state.busy && motorcycle.isNotBlank()
                    ) { Text("Buscar motocicleta") };
                    OutlinedButton(onClick = {
                        motorcycle = "";
                        loadedId = null;
                        vm.motorcycles()
                    }, enabled = !state.busy) { Text("Recientes") }
                }
                Text("Selecciona una motocicleta para consultar su recorrido.", fontSize = 12.sp)
            }
            if (loadedId == null) items(
                state.motorcycles,
                key = { it["id"]?.jsonPrimitive?.content.orEmpty() }) { row ->
                OutlinedCard(onClick = {
                    loadedId = row["id"]?.jsonPrimitive?.longOrNull;
                    destination =
                        null;
                    loadedId?.let(vm::movements)
                }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${row["marca"]?.jsonPrimitive?.content.orEmpty()} ${row["modelo"]?.jsonPrimitive?.content.orEmpty()} · ${row["color"]?.jsonPrimitive?.content.orEmpty()}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Chasis: ${row["chasisVista"]?.jsonPrimitive?.content.orEmpty()}")
                        Text("Consultar recorrido →")
                    }
                }
            }
            if (loadedId != null) item {
                OutlinedButton(
                    onClick = {
                        loadedId = null;
                        destination = null;
                        confirm = false
                    },
                    enabled = !state.busy
                ) { Text("← Volver a motocicletas") }
                Text("Recorrido de la motocicleta", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (state.busy) Text("Cargando recorrido…")
                state.error?.let {
                    TextButton(
                        onClick = { loadedId?.let(vm::movements) },
                        enabled = !state.busy
                    ) { Text("Reintentar consulta") }
                }
            }
            if (loadedId != null && state.movements.isNotEmpty()) item {
                // The API returns history in ascending order; the last event is current.
                val current = state.movements.last()
                Text(
                    "Ubicación conocida: ${current.destinoNombre}",
                    fontWeight = FontWeight.SemiBold
                )
                Text("Moto #$loadedId · Destino #${current.destinoId}", fontSize = 12.sp)
                if (account.permits(TRANSFER_UNITS)) {
                    SitePicker(state.sites.filter {
                        it.tipo in listOf(
                            "TIENDA",
                            "DESPACHO"
                        ) && it.id != current.destinoId
                    }, destination, { destination = it }, state.busy)
                    Button(
                        onClick = { confirm = true },
                        enabled = !state.busy && destination != null && destination?.id != current.destinoId
                    ) { Text("Confirmar traslado") }
                }
            }
            if (loadedId != null && state.movements.isEmpty() && !state.busy && state.error == null) item {
                Text(
                    "Sin movimientos. Registra primero la ubicación inicial desde la ficha de inventario."
                )
            }
            if (loadedId != null) items(state.movements, key = { it.id }) { movement ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (movement.tipo == "TRASLADO") "Traslado #${movement.id}" else "Ubicación inicial #${movement.id}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("${movement.origenId?.let { "Origen #$it → " } ?: ""}${movement.destinoNombre}")
                        Text("${movement.registrador}\n${movement.fecha}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("Confirmar traslado") },
        text = { Text("Motocicleta #$loadedId → ${destination?.nombre}. Se conservará el historial de origen y destino.") },
        confirmButton = {
            TextButton(onClick = {
                confirm = false;
                vm.transfer(
                    requireNotNull(loadedId), requireNotNull(destination).id
                )
            }, enabled = !state.busy) { Text("Trasladar") }
        },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } })
}
