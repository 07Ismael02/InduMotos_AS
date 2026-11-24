package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.UsersViewModel

@Composable
fun UsersScreen(
    account: Access,
    permissions: Boolean,
    refreshKey: String?,
    create: () -> Unit,
    expired: () -> Unit
) {
    val vm: UsersViewModel = koinViewModel();

    val s by vm.state.collectAsState()
    var search by remember(account.usuarioId, permissions) { mutableStateOf("") }
    var edit by remember(s.selected?.id) { mutableStateOf(false) }
    var role by remember(s.access) { mutableStateOf(s.access?.rolCodigo.orEmpty()) }
    var storeUser by remember { mutableStateOf<UserAccount?>(null) }
    var chosenStore by remember { mutableStateOf<StoreSummary?>(null) }
    var resetUser by remember { mutableStateOf<UserAccount?>(null) }
    var confirm by remember { mutableStateOf<Pair<String, String?>?>(null) }
    DisposableEffect(account.usuarioId, permissions) { onDispose { vm.reset() } }
    LaunchedEffect(account.usuarioId, permissions) {
        vm.reset();
        vm.list()
    }
    LaunchedEffect(search) {
        delay(300);
        if (s.page != null) vm.list(search)
    }
    LaunchedEffect(refreshKey) { if (refreshKey != null) vm.list(search) }
    LaunchedEffect(s.expired) { if (s.expired) expired() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            Modifier.widthIn(max = 840.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    if (permissions) "Accesos del personal" else "Personal registrado",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Busca por nombre o usuario. Los permisos efectivos los valida el servidor.")
                if (!permissions && account.permisos.any { it.codigo == "USUARIOS_CREAR" && it.permitido }) Button(
                    onClick = create,
                    enabled = !s.busy
                ) { Text("Nueva cuenta") }
                OutlinedTextField(
                    search,
                    { if (it.length <= 180) search = it },
                    label = { Text("Nombre o usuario") },
                    singleLine = true,
                    enabled = !s.busy,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { vm.list(search) },
                    enabled = !s.busy
                ) { Text("Actualizar") }
                if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                s.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                if (s.selected == null) s.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                s.page?.let { Text("${it.totalElementos} cuentas") }
            }
            items(s.page?.items.orEmpty(), key = { it.id }) { user ->
                OutlinedCard(
                    onClick = { if (permissions) vm.select(user) },
                    enabled = !s.busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(user.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                        Text("@${user.nombreUsuario} · ${roleLabel(user.rolCodigo)}")
                        Text(user.estado)
                        if (user.rolCodigo in listOf(
                                "COMERCIAL",
                                "FACTURACION"
                            )
                        ) Text(
                            user.tiendaNombre ?: "Sin tienda asignada",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!permissions && account.rolCodigo == "ADMIN" && user.rolCodigo in listOf(
                                "COMERCIAL",
                                "FACTURACION"
                            ) && user.estado == "ACTIVO"
                        ) OutlinedButton(onClick = {
                            chosenStore = null;
                            storeUser = user;
                            vm.stores()
                        }, enabled = !s.busy) { Text("Asignar tienda") }

                        if (permissions) Text(
                            "Consultar accesos →",
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!permissions && account.rolCodigo == "ADMIN" && user.id != account.usuarioId && user.estado == "ACTIVO") OutlinedButton(
                            onClick = { resetUser = user },
                            enabled = !s.busy
                        ) { Text("Restablecer contraseña") }
                    }
                }
            }
            if (!s.busy && s.page?.items?.isEmpty() == true) item { Text("No hay cuentas que coincidan.") }
            s.page?.let { p ->
                if (p.pages > 1) item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { vm.list(search, p.pagina - 1) },
                            enabled = !s.busy && p.pagina > 0
                        ) { Text("Anterior") }
                        Text("${p.pagina + 1} / ${p.pages}")
                        OutlinedButton(
                            onClick = { vm.list(search, p.pagina + 1) },
                            enabled = !s.busy && p.pagina + 1 < p.pages
                        ) { Text("Siguiente") }
                    }
                }
            }
        }
    }
    s.selected?.let { user ->
        Dialog(
            onDismissRequest = vm::close,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedButton(
                            onClick = vm::close,
                            enabled = !s.busy
                        ) { Text("← Volver al personal") }
                        Text(user.nombreCompleto, style = MaterialTheme.typography.headlineSmall)
                        Text("@${user.nombreUsuario} · ${roleLabel(s.access?.rolCodigo ?: user.rolCodigo)} · ${user.estado}")
                        if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        s.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                edit,
                                { edit = it },
                                enabled = !s.busy
                            );
                            Text("Habilitar edición")
                        }
                        if (edit && s.access != null) {
                            ChoiceField("Rol", role, RoleCodes, !s.busy) { role = it }
                            OutlinedButton(
                                onClick = { confirm = "ROL" to role },
                                enabled = !s.busy && role != s.access?.rolCodigo
                            ) { Text("Guardar rol") }
                        }
                    }
                    items(s.access?.permisos.orEmpty(), key = { it.codigo }) { p ->
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    PermissionLabels[p.codigo] ?: p.codigo.replace('_', ' '),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    if (p.permitido) "Permitido" else "Denegado",
                                    color = if (p.permitido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Text(p.excepcion?.let { "Excepción: $it" }
                                    ?: if (p.porRol) "Heredado del rol" else "No incluido en el rol")
                                if (edit) {
                                    OutlinedButton(
                                        onClick = { confirm = p.codigo to "PERMITIR" },
                                        enabled = !s.busy && p.excepcion != "PERMITIR" && AccessRules.canAllow(
                                            s.access!!.rolCodigo,
                                            p.codigo
                                        )
                                    ) { Text("Permitir") }
                                    OutlinedButton(
                                        onClick = { confirm = p.codigo to "DENEGAR" },
                                        enabled = !s.busy && p.excepcion != "DENEGAR"
                                    ) { Text("Denegar") }
                                    if (p.excepcion != null) TextButton(onClick = {
                                        confirm = p.codigo to null
                                    }, enabled = !s.busy) { Text("Restablecer permiso del rol") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    confirm?.let { change ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Confirmar cambio de acceso") },
            text = {
                Text(
                    "Cuenta: ${s.selected?.nombreUsuario}. ${
                        if (change.first == "ROL") "Nuevo rol: ${
                            roleLabel(change.second.orEmpty())
                        }" else "${PermissionLabels[change.first] ?: change.first}: ${change.second ?: "restablecer"}"
                    }. Afectará los permisos de esta cuenta."
                )
            },
            confirmButton = {
                Button(onClick = {
                    confirm =
                        null;
                    if (change.first == "ROL") vm.role(change.second!!) else vm.permission(
                        change.first,
                        change.second
                    )
                }, enabled = !s.busy) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Cancelar") } })
    }
    resetUser?.let { user ->
        var adminKey by remember(user.id) { mutableStateOf("") };

        var newKey by remember(user.id) { mutableStateOf("") };

        var repeated by remember(user.id) { mutableStateOf("") };

        var verified by remember(user.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { resetUser = null },
            title = { Text("Restablecer contraseña") },
            text = {
                Column(
                    Modifier.heightIn(max = 360.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Cuenta: ${user.nombreUsuario}. Verifica su identidad antes de continuar.")
                    listOf(
                        Triple(
                            "Tu contraseña de administrador",
                            adminKey,
                            { v: String -> adminKey = v }),
                        Triple("Nueva contraseña", newKey, { v: String -> newKey = v }),
                        Triple("Repetir nueva contraseña", repeated, { v: String -> repeated = v })
                    ).forEach { (label, value, change) ->
                        OutlinedTextField(
                            value,
                            { if (it.length <= 100) change(it) },
                            label = { Text(label) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        "Usa entre12 y100 caracteres. Se invalidarán las sesiones online anteriores de esta cuenta.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row {
                        Checkbox(
                            verified,
                            { verified = it });
                        Text("Verifiqué la identidad del trabajador")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val a = adminKey;

                        val n = newKey;
                        adminKey = "";
                        newKey = "";
                        repeated = "";
                        resetUser =
                            null;
                        vm.resetPassword(user, a, n)
                    },
                    enabled = verified && adminKey.isNotBlank() && newKey.length in 12..100 && newKey == repeated && !s.busy
                ) { Text("Restablecer") }
            },
            dismissButton = { TextButton(onClick = { resetUser = null }) { Text("Cancelar") } })
    }

    storeUser?.let { user ->
        AlertDialog(
            onDismissRequest = { if (!s.busy) storeUser = null },
            title = { Text("Asignar tienda") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(user.nombreUsuario);
                    SimpleChoice(
                        "Tienda",
                        chosenStore?.nombre.orEmpty(),
                        s.stores.map { it.nombre },
                        !s.busy
                    ) { name -> chosenStore = s.stores.firstOrNull { it.nombre == name } }
                    s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    storeUser = null;
                    vm.assignStore(
                        user,
                        chosenStore!!
                    )
                }, enabled = chosenStore != null && !s.busy) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { storeUser = null },
                    enabled = !s.busy
                ) { Text("Cancelar") }
            })
    }

}
