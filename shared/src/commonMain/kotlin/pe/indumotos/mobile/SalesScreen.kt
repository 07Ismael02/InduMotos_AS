package pe.indumotos.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.SalesViewModel
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun SalesScreen(account: Access, pick: ((PhotoSelection) -> Unit) -> Unit, expired: () -> Unit) {
    val vm: SalesViewModel = koinViewModel();

    val s by vm.state.collectAsState();

    var tab by remember { mutableStateOf(0) };

    var moto by remember { mutableStateOf<StockMotorcycle?>(null) };

    var reviewing by remember { mutableStateOf<ManualPayment?>(null) };

    var rejecting by remember { mutableStateOf<ManualPayment?>(null) };

    var receipt by remember { mutableStateOf("") };

    var verified by remember { mutableStateOf(false) };

    var reason by remember { mutableStateOf("") }
    fun can(code: String) = account.permisos.any { it.codigo == code && it.permitido }
    LaunchedEffect(account.usuarioId) {
        vm.reset();
        vm.load(account.rolCodigo == "ADMIN")
    }
    DisposableEffect(account.usuarioId) { onDispose { vm.reset() } }
    LaunchedEffect(s.expired) { if (s.expired) expired() }
    LaunchedEffect(s.registered) {
        if (s.registered) {
            moto = null;
            tab = 1
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Ventas y pagos", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Registro académico con verificación manual. No procesa transferencias ni confirma pagos bancarios automáticamente.",
                style = MaterialTheme.typography.bodySmall
            )
            if (s.stores.size > 1) SimpleChoice(
                "Tienda",
                s.store?.nombre.orEmpty(),
                s.stores.map { it.nombre },
                !s.busy
            ) { name ->
                s.stores.firstOrNull { it.nombre == name }?.let(vm::select)
            } else s.store?.let { Text(it.nombre, style = MaterialTheme.typography.titleMedium) }
            OutlinedButton(
                onClick = vm::refresh,
                enabled = !s.busy && s.store != null
            ) { Text("Actualizar") }
            Box(
                Modifier.fillMaxWidth().height(4.dp)
            ) { if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth()) }
            s.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            };
            s.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            if (s.store != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Disponibles",
                    "Pagos",
                    "Ventas"
                ).forEachIndexed { i, label ->
                    FilterChip(
                        selected = tab == i,
                        onClick = { tab = i },
                        label = { Text(label) })
                }
            }
        }
        if (tab == 0) items(s.stock?.items.orEmpty(), key = { it.id }) { row ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${row.marca} ${row.modelo} · ${row.color}",
                        style = MaterialTheme.typography.titleMedium
                    );
                    Text(row.chasis, style = MaterialTheme.typography.bodySmall)
                    if (can("PAGOS_REGISTRAR")) Button(
                        onClick = { moto = row },
                        enabled = !s.busy
                    ) { Text("Registrar pago") }
                }
            }
        }
        if (tab == 1) items(s.payments?.items.orEmpty(), key = { it.id }) { payment ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${payment.metodo} · S/ ${payment.importe}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(payment.chasis);
                    Text(paymentStateLabel(payment.estado));
                    Text("Referencia: ${payment.referencia}");
                    Text(
                        "Registró: ${payment.registrador}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    payment.motivo?.let { Text(it) };
                    payment.referenciaVenta?.let { Text("Comprobante de venta: $it") }
                    OutlinedButton(
                        onClick = { vm.receipt(payment) },
                        enabled = !s.busy
                    ) { Text("Ver comprobante adjunto") }
                    if (payment.estado == "PENDIENTE" && can("PAGOS_CONFIRMAR")) {
                        Button(
                            onClick = {
                                reviewing = payment;
                                receipt = "";
                                verified = false
                            },
                            enabled = !s.busy
                        ) { Text("Confirmar abono") }
                        OutlinedButton(
                            onClick = {
                                rejecting = payment;
                                reason = ""
                            },
                            enabled = !s.busy
                        ) { Text("Rechazar con motivo") }
                    }
                }
            }
        }
        if (tab == 2) items(s.sales?.items.orEmpty(), key = { it.id }) { sale ->
            OutlinedCard(
                Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Venta #${sale.id} · S/ ${sale.importe}",
                        style = MaterialTheme.typography.titleMedium
                    );
                    Text(sale.chasis);
                    Text("Comprobante: ${sale.referencia}");
                    Text("Pago: ${sale.referenciaPago.orEmpty()}");
                    Text(
                        "Registró ${sale.registrador}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        val count = when (tab) {
            0 -> s.stock?.totalElementos;
            1 -> s.payments?.totalElementos;
            else -> s.sales?.totalElementos
        }
        val page = when (tab) {
            0 -> s.stock?.pagina;
            1 -> s.payments?.pagina;
            else -> s.sales?.pagina
        } ?: 0
        if (count == 0L && !s.busy) item { Text("No hay registros en este apartado.") }
        if (count != null && count > 10) item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { vm.page(tab, page - 1) },
                    enabled = !s.busy && page > 0
                ) { Text("Anterior") };
                Text(
                    "${page + 1} / ${(count + 9) / 10}",
                    Modifier.padding(top = 12.dp)
                );
                OutlinedButton(
                    onClick = { vm.page(tab, page + 1) },
                    enabled = !s.busy && (page + 1) * 10 < count
                ) { Text("Siguiente") }
            }
        }
    }
    moto?.let { row ->
        PaymentForm(
            row,
            s.busy,
            s.error,
            pick,
            { moto = null }) { id, method, entity, reference, amount, photo ->
            vm.register(
                id,
                row.id,
                method,
                entity,
                reference,
                amount,
                photo
            )
        }
    }
    reviewing?.let { payment ->
        AlertDialog(
            onDismissRequest = { reviewing = null },
            title = { Text("Confirmar abono") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${payment.metodo}: S/ ${payment.importe}. Revisa el abono en la cuenta receptora; la imagen por sí sola no lo confirma.");
                    OutlinedTextField(
                        receipt,
                        { if (it.length <= 100) receipt = it },
                        label = { Text("Comprobante de venta") });
                    Row {
                        Checkbox(
                            verified,
                            { verified = it });
                        Text("Confirmé la recepción real del abono")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewing = null;
                        vm.confirm(
                            payment,
                            receipt,
                            verified
                        )
                    },
                    enabled = verified && receipt.isNotBlank() && !s.busy
                ) { Text("Confirmar y registrar venta") }
            },
            dismissButton = { TextButton(onClick = { reviewing = null }) { Text("Cancelar") } })
    }
    rejecting?.let { payment ->
        AlertDialog(
            onDismissRequest = { rejecting = null },
            title = { Text("Rechazar pago") },
            text = {
                OutlinedTextField(
                    reason,
                    { if (it.length <= 500) reason = it },
                    label = { Text("Motivo") })
            },
            confirmButton = {
                Button(
                    onClick = {
                        rejecting = null;
                        vm.reject(payment, reason)
                    },
                    enabled = reason.isNotBlank() && !s.busy
                ) { Text("Rechazar") }
            },
            dismissButton = { TextButton(onClick = { rejecting = null }) { Text("Cancelar") } })
    }
    s.receipt?.let { bytes ->
        AlertDialog(
            onDismissRequest = vm::closeReceipt,
            title = { Text("Comprobante adjunto") },
            text = {
                val bitmap = remember(bytes) { decodeGuideImage(bytes) };
                if (bitmap != null) Image(
                    bitmap,
                    "Comprobante para revisión manual",
                    Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                ) else Text("No se pudo mostrar la imagen")
            },
            confirmButton = { TextButton(onClick = vm::closeReceipt) { Text("Cerrar") } })
    }
}

internal fun paymentStateLabel(state: String) = when (state) {
    "PENDIENTE" -> "Pendiente de revisión";
    "CONFIRMADO" -> "Confirmado manualmente";
    "RECHAZADO" -> "Rechazado";
    else -> state
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun PaymentForm(
    moto: StockMotorcycle,
    busy: Boolean,
    error: String?,
    pick: ((PhotoSelection) -> Unit) -> Unit,
    close: () -> Unit,
    submit: (String, String, String, String, String, GuidePhoto) -> Unit
) {
    val request = remember(moto.id) { Uuid.random().toString() };

    var method by remember { mutableStateOf("YAPE") };

    var entity by remember { mutableStateOf("") };

    var reference by remember { mutableStateOf("") };

    var amount by remember { mutableStateOf("") };

    var photo by remember { mutableStateOf<GuidePhoto?>(null) };

    var local by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!busy) close() },
        title = { Text("Registrar pago pendiente") },
        text = {
            Column(
                Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("${moto.marca} ${moto.modelo} · ${moto.color}")
                SimpleChoice(
                    "Método",
                    method,
                    listOf("YAPE", "PLIN", "TRANSFERENCIA"),
                    !busy
                ) { method = it }
                if (method == "TRANSFERENCIA") SimpleChoice(
                    "Banco",
                    entity,
                    listOf(
                        "BCP",
                        "Interbank",
                        "BBVA",
                        "Scotiabank",
                        "Banco de la Nación",
                        "Otro banco"
                    ),
                    !busy
                ) { entity = it }
                OutlinedTextField(
                    reference,
                    { if (it.length <= 100) reference = it },
                    label = { Text("Referencia de la operación") },
                    singleLine = true,
                    enabled = !busy
                )
                OutlinedTextField(
                    amount,
                    { if (it.length <= 13) amount = it.replace(',', '.') },
                    label = { Text("Importe en soles") },
                    singleLine = true,
                    enabled = !busy
                )
                OutlinedButton(
                    onClick = {
                        pick { r ->
                            local = r.error;
                            if (r.photos.size == 1) photo =
                                r.photos.first() else if (r.photos.isNotEmpty()) local =
                                "Selecciona un solo comprobante"
                        }
                    },
                    enabled = !busy
                ) { Text(if (photo == null) "Adjuntar comprobante JPG/PNG" else "Cambiar comprobante") }
                photo?.let { Text("Imagen adjunta", style = MaterialTheme.typography.bodySmall) }
                Text(
                    "La moto quedará reservada mientras Facturación revisa el pago. No se envía dinero desde esta aplicación.",
                    style = MaterialTheme.typography.bodySmall
                )
                (local ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    submit(
                        request,
                        method,
                        if (method == "TRANSFERENCIA") entity else method,
                        reference.trim(),
                        amount.trim(),
                        photo!!
                    )
                },
                enabled = !busy && photo != null && reference.isNotBlank() && amount.isNotBlank() && (method != "TRANSFERENCIA" || entity.isNotBlank())
            ) { Text(if (busy) "Enviando…" else "Enviar a revisión") }
        },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Cancelar") } })
}
