package pe.indumotos.mobile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*

private val Ink = Color(0xFF142E49)
private val Yellow = Color(0xFFEEE51B)
private val Blue: Color @Composable get() = MaterialTheme.colorScheme.secondary
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private fun JsonObject.text(key: String) = get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
private fun icon(m: Module) = when (m) {
    Module.SALES -> Icons.Default.PointOfSale;
    Module.CATALOG -> Icons.Default.Category;
    Module.REQUESTS -> Icons.Default.Assignment;
    Module.TRANSFERS -> Icons.Default.LocalShipping;
    Module.INVENTORY -> Icons.Default.Inventory2;
    Module.ARRIVALS -> Icons.Default.LocalShipping;
    Module.FICHAS -> Icons.Default.Description
}

private fun createForm(module: Module): Form = when (module) {
    Module.SALES, Module.CATALOG, Module.REQUESTS, Module.TRANSFERS -> error("Operación mediante su pantalla específica")
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
private fun Brand(modifier: Modifier = Modifier) {
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
fun App(
    backendUrl: String = "http://127.0.0.1:8089",
    pickGuidePhotos: ((PhotoSelection) -> Unit) -> Unit = { it(PhotoSelection(error = "Selección de fotos no disponible en esta plataforma.")) },
    initialTheme: Appearance = Appearance.SYSTEM,
    onAppearanceChanged: (Appearance) -> Unit = {},
    onDarkTheme: (Boolean) -> Unit = {},
    installSystemBack: SystemBackInstaller = { _, _ -> },
    onExitHint: () -> Unit = {},
    onExitRequested: () -> Unit = {},
    unlockOffline: ((Boolean, String?) -> Unit) -> Unit = {
        it(
            false,
            "La consulta offline protegida no está disponible en esta plataforma."
        )
    },
    isActive: Boolean = true,
    takePhoto: ((PhotoSelection) -> Unit) -> Unit = { it(PhotoSelection(error = "La cámara no está disponible en esta plataforma.")) }
) {
    val vm: LogisticsViewModel = koinViewModel();

    val state by vm.state.collectAsState()
    val offlineStatus by vm.offlineStatus.collectAsState()
    var photoRequest by remember(state.access?.usuarioId) {
        mutableStateOf<((PhotoSelection) -> Unit)?>(
            null
        )
    }
    LaunchedEffect(isActive, state.access?.usuarioId, offlineStatus.readOnlySession) {
        if (isActive && state.access != null && !offlineStatus.readOnlySession) while (true) {
            delay(30000);
            vm.refreshConnection()
        }
    }
    var exitArmed by remember { mutableStateOf(false) }
    LaunchedEffect(state.access?.usuarioId) { exitArmed = false }
    LaunchedEffect(state.module) { if (state.module != null) exitArmed = false }
    LaunchedEffect(exitArmed) {
        if (exitArmed) {
            delay(2000);
            exitArmed = false
        }
    }
    installSystemBack(true) {
        if (!state.busy) {
            if (state.editing) {
                vm.cancelEdit()
            } else if (state.detail != null) {
                vm.closeDetail()
            } else if (state.module != null) {
                exitArmed = false;
                vm.open(null);
                onExitHint();
                exitArmed = true
            } else if (exitArmed) {
                exitArmed = false;
                onExitRequested()
            } else {
                exitArmed = true;
                onExitHint()
            }
        }
    }
    var appearance by remember { mutableStateOf(initialTheme) }
    val darkTheme = when (appearance) {
        Appearance.SYSTEM -> isSystemInDarkTheme()
        Appearance.LIGHT -> false
        Appearance.DARK -> true
    }
    LaunchedEffect(darkTheme) { onDarkTheme(darkTheme) }
    IndumotosTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalSystemBack provides installSystemBack,
            LocalReturnHome provides {
                vm.open(null);
                onExitHint();
                exitArmed = true
            }) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (state.access == null) Login(
                    state,
                    vm,
                    backendUrl,
                    unlockOffline
                ) else Workspace(state, vm, appearance, { photoRequest = it }, offlineStatus) {
                    appearance = it
                    onAppearanceChanged(it)
                }
                state.form?.let { form ->
                    FormDialog(
                        form,
                        state.busy,
                        state.error,
                        { vm.form(null) },
                        vm::submit
                    )
                }
                photoRequest?.let { receiver ->
                    PhotoSourceDialog(
                        onCamera = {
                            photoRequest = null;
                            takePhoto(receiver)
                        },
                        onGallery = {
                            photoRequest = null;
                            pickGuidePhotos(receiver)
                        },
                        onClose = {
                            photoRequest = null;
                            receiver(PhotoSelection())
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Workspace(
    state: UiState,
    vm: LogisticsViewModel,
    appearance: Appearance,
    pickGuidePhotos: ((PhotoSelection) -> Unit) -> Unit,
    offlineStatus: OfflineStatus,
    onAppearanceChanged: (Appearance) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val account = requireNotNull(state.access)
    val drawer = rememberDrawerState(DrawerValue.Closed);

    val scope = rememberCoroutineScope();

    var confirmLogout by remember { mutableStateOf(false) }
    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet(
            modifier = Modifier.widthIn(max = 310.dp),
            drawerContainerColor = MaterialTheme.colorScheme.surface
        ) {
            Brand(Modifier.fillMaxWidth().height(96.dp))
            HorizontalDivider();
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp)
            ) {
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = state.module == null,
                    onClick = {
                        if (!state.busy) {
                            vm.open(null);
                            scope.launch { drawer.close() }
                        }
                    })
                Text(
                    "OPERACIONES",
                    Modifier.padding(16.dp),
                    fontSize = 10.sp,
                    color = Muted,
                    letterSpacing = 1.sp
                )
                Module.entries.filter { vm.canRead(it) }.forEach { m ->
                    NavigationDrawerItem(
                        label = { Text(m.title) },
                        icon = { Icon(icon(m), null) },
                        selected = state.module == m,
                        onClick = {
                            if (!state.busy) {
                                vm.open(m);
                                scope.launch { drawer.close() }
                            }
                        })
                }
            }
            HorizontalDivider()
            AccountEntry(account, onClick = {
                scope.launch { drawer.close() };
                showAccount = true
            })
        }
    }) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                state.module?.title ?: "Inicio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            );
                            Text(
                                "INDUMOTOS",
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                color = Muted
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawer.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                "Abrir menú de navegación"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            floatingActionButton = {
                val m = state.module;

                val permission = if (m == Module.INVENTORY) "FICHAS_REGISTRAR" else m?.permission
                if (m == Module.INVENTORY && vm.allowed("FICHAS_REGISTRAR")) ExtendedFloatingActionButton(
                    onClick = { vm.open(Module.FICHAS) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Registrar desde guía") })
            }) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                OfflineBanner(
                    offlineStatus,
                    state.busy,
                    vm.allowed("INVENTARIO_CONSULTAR"),
                    vm::downloadOffline,
                    vm::synchronize,
                    vm::reconnect
                )
                Box(Modifier.fillMaxWidth().height(4.dp)) {
                    if (state.busy) LinearProgressIndicator(
                        Modifier.fillMaxWidth(),
                        color = Blue
                    )
                }
                state.error?.let { Notice(it, true) };
                state.message?.let { Notice(it, false) }
                when (state.module) {
                    Module.SALES -> SalesScreen(
                        account,
                        pickGuidePhotos,
                        vm::expireSession
                    );
                    null -> Home(state, vm);
                    Module.INVENTORY -> Inventory(
                        state,
                        vm
                    );
                    Module.FICHAS -> GuidesScreen(
                        account,
                        true,
                        pickGuidePhotos,
                        vm::expireSession,
                        offlineStatus
                    );
                    Module.ARRIVALS -> GuidesScreen(
                        account,
                        false,
                        pickGuidePhotos,
                        vm::expireSession,
                        offlineStatus
                    );
                    Module.CATALOG -> CatalogScreen(
                        account,
                        vm::expireSession,
                        offlineStatus.offline
                    );
                    Module.REQUESTS -> RequestsScreen(
                        account,
                        vm::expireSession
                    );
                    Module.TRANSFERS -> TransfersScreen(account, vm::expireSession)
                }
            }
        }
    }
    LocalSystemBack.current(drawer.isOpen) { scope.launch { drawer.close() } }
    if (showSettings) AppearanceSettings(appearance, onAppearanceChanged) {
        showSettings = false;
        showAccount = true
    }
    if (showAccount) AccountMenu(
        account = account,
        canLogout = !state.busy,
        onClose = { showAccount = false },
        onProfile = {
            showAccount = false;
            showProfile = true
        },
        onAppearance = {
            showAccount = false;
            showSettings = true
        },
        onLogout = {
            showAccount = false;
            scope.launch { drawer.close() };
            confirmLogout = true
        }
    )
    if (showProfile) AccountProfile(account) {
        showProfile = false;
        showAccount = true
    }
    if (confirmLogout) AlertDialog(
        onDismissRequest = { if (!state.busy) confirmLogout = false },
        title = { Text("Cerrar sesión") },
        text = { Text(if (offlineStatus.offline) "Se cerrará el acceso local. Los borradores se conservan para tu próximo login; sin servidor no se confirma revocación remota." else "Se revocará el acceso de esta sesión en el servidor.") },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmLogout = false;
                    vm.logout()
                },
                enabled = !state.busy
            ) { Text("Cerrar sesión") }
        },
        dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } })
    if (state.module in listOf(Module.INVENTORY, Module.FICHAS)) state.detail?.let { record ->
        val location by vm.location.collectAsState()
        val id = record.text("id").toLongOrNull()
        MotorcycleDetail(
            record,
            state.busy,
            vm::closeDetail,
            extras = {
                if (id != null) VehicleExtras(
                    id,
                    account,
                    offlineStatus.offline,
                    pickGuidePhotos,
                    vm::expireSession
                )
            }) {
            if (vm.allowed("FICHAS_EDITAR")) OutlinedButton(
                onClick = vm::beginEdit,
                enabled = !state.busy
            ) { Text("Editar ficha") }
            if (id != null && vm.allowed(READ_LOCATIONS)) {
                LocationSection(
                    location, vm.allowed(WRITE_LOCATION), state.busy, state.error, state.message,
                    onLoad = { vm.loadLocation(id) },
                    onSave = { vm.registerLocation(id, it) }
                )
            }
        }
        if (state.editing) EditMotorcycleDialog(
            record,
            state.editOptions,
            state.busy,
            state.error,
            vm::cancelEdit,
            vm::updateMotorcycle
        )
    }
}

@Composable
private fun Home(state: UiState, vm: LogisticsViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Brand(Modifier.fillMaxWidth().height(128.dp)) }
        item {
            Text(
                "Gestión de motocicletas",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            );
            Spacer(Modifier.height(10.dp));
            Text(
                "Bienvenido, ${state.access!!.nombreUsuario}",
                fontSize = 16.sp
            );
            Text(
                roleLabel(state.access.rolCodigo),
                color = Blue,
                fontSize = 13.sp
            );
            Spacer(Modifier.height(12.dp));
            Text(
                "Selecciona una operación. Las opciones disponibles corresponden a los permisos de tu cuenta.",
                fontSize = 13.sp,
                color = Muted,
                lineHeight = 20.sp
            )
        }
        items(Module.entries.filter { vm.canRead(it) }) { m ->
            OutlinedCard(
                onClick = { vm.open(m) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            icon(m),
                            null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    };
                    Spacer(Modifier.width(16.dp));
                    Column(Modifier.weight(1f)) {
                        Text(
                            m.title,
                            fontWeight = FontWeight.SemiBold
                        );
                        Spacer(Modifier.height(5.dp));
                        Text(
                            m.description,
                            color = Muted,
                            fontSize = 12.sp
                        )
                    };
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
        if (Module.entries.none { vm.canRead(it) }) item {
            Text(
                "Tu cuenta no tiene apartados habilitados. Solicita al administrador que revise tus permisos.",
                color = Muted
            )
        }
    }
}

@Composable
private fun Login(
    state: UiState,
    vm: LogisticsViewModel,
    backendUrl: String,
    unlockOffline: ((Boolean, String?) -> Unit) -> Unit
) {
    LaunchedEffect(backendUrl) { vm.findOffline(backendUrl) }
    var user by remember { mutableStateOf("") };

    var password by remember { mutableStateOf("") };

    var visible by remember { mutableStateOf(false) }
    var showOffline by remember { mutableStateOf(false) }
    var showRecovery by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.widthIn(max = 440.dp).fillMaxWidth().fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Brand(
                Modifier.fillMaxWidth().height(130.dp)
            );
            Spacer(Modifier.height(30.dp));
            Text(
                "Iniciar sesión",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            );
            Spacer(Modifier.height(8.dp));
            Text(
                "Gestión de inventario y recepción de motocicletas",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 20.sp
            );
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                user,
                { user = it },
                label = { Text("Usuario") },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth()
            );
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                password,
                { password = it },
                label = { Text("Contraseña") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false
                ),
                singleLine = true,
                enabled = !state.busy,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        visible = !visible
                    }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp));
            state.error?.let {
                Notice(
                    it,
                    true
                )
            };
            state.message?.let { Notice(it, false) }
            Button(
                onClick = {
                    vm.login(backendUrl, user, password);
                    password = ""
                },
                enabled = !state.busy && user.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink)
            ) {
                if (state.busy) CircularProgressIndicator(
                    Modifier.size(22.dp),
                    color = Ink
                ) else Text("Ingresar", fontWeight = FontWeight.SemiBold)
            }
            TextButton(
                onClick = { showRecovery = true },
                enabled = !state.busy
            ) { Text("Olvidé mi contraseña") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showOffline = true },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    null,
                    modifier = Modifier.size(18.dp)
                );
                Spacer(Modifier.width(8.dp));
                Text("Sin conexión")
            }
            Spacer(Modifier.height(20.dp));
            Text(
                "Acceso exclusivo para personal autorizado. Si necesitas una cuenta, comunícate con el administrador.",
                fontSize = 11.sp,
                color = Muted,
                lineHeight = 18.sp
            )
        }
    }
    if (showRecovery) AlertDialog(
        onDismissRequest = { showRecovery = false },
        title = { Text("Recuperar acceso") },
        text = { Text("Comunícate con el administrador de Indumotos e indica tu usuario. Después de verificar tu identidad, podrá restablecer tu contraseña desde Trabajadores, en el sistema web. No compartas tu contraseña anterior. La recuperación requiere conexión; este formulario no envía correos ni cambia contraseñas automáticamente.") },
        confirmButton = { TextButton(onClick = { showRecovery = false }) { Text("Entendido") } })
    if (showOffline) OfflineAccountDialog(
        state.offlineProfiles,
        onDismiss = { showOffline = false },
        onOpen = { profile ->
            showOffline = false
            unlockOffline { ok, error ->
                if (ok) vm.openOffline(profile) else vm.offlineError(
                    error ?: "No se desbloquearon los datos locales."
                )
            }
        })

}

@Composable
private fun OfflineAccountDialog(
    profiles: List<OfflineProfile>,
    onDismiss: () -> Unit,
    onOpen: (OfflineProfile) -> Unit
) {
    var selectedOwner by remember(profiles) { mutableStateOf(profiles.firstOrNull()?.owner) }
    val selected = profiles.firstOrNull { it.owner == selectedOwner }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sin conexión") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (profiles.isEmpty()) {
                    Text("Todavía no hay cuentas con datos descargados en este teléfono.")
                    Text(
                        "Inicia sesión con conexión y pulsa Descargar datos para preparar este modo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text("Elige la cuenta cuyos datos quieres consultar.")
                    LazyColumn(
                        Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles, key = { it.owner }) { profile ->
                            OutlinedCard(
                                onClick = { selectedOwner = profile.owner },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedOwner == profile.owner,
                                        onClick = null
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            roleLabel(profile.access.rolCodigo),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            profile.access.nombreUsuario,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        "Android pedirá el desbloqueo del dispositivo. Solo verás los datos guardados de esa cuenta.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = { if (selected != null) Button(onClick = { onOpen(selected) }) { Text("Abrir datos") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Volver") } }
    )
}

@Composable
private fun Inventory(state: UiState, vm: LogisticsViewModel) {
    var chasis by remember { mutableStateOf("") };

    var marca by remember { mutableStateOf("") };

    var modelo by remember { mutableStateOf("") };

    var color by remember { mutableStateOf("") };

    var filters by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    LaunchedEffect(chasis, marca, modelo, color, state.busy) {
        if (touched && !state.busy) {
            delay(300);

            val filter = InventoryFilter(chasis, marca, modelo, color)
            val value = chasis.trim()
            if (filter != state.filter && (value.isEmpty() || value.length > 4 || (value.length == 4 && value.all { it.isDigit() }))) vm.search(
                filter
            )
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 110.dp
        ), verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (state.module == Module.FICHAS) "Fichas de motocicletas" else "Consulta de inventario",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            );
            Text(
                "Busca por chasis completo o últimos cuatro dígitos.",
                fontSize = 12.sp,
                color = Muted
            );
            Spacer(Modifier.height(16.dp));
            OutlinedTextField(
                chasis,
                {
                    chasis = it;
                    touched = true
                },
                label = { Text("Chasis") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) })
            TextButton(onClick = {
                filters = !filters
            }) { Text(if (filters) "Ocultar filtros" else "Filtrar por marca, modelo y color") }
            if (filters) {
                listOf(
                    Triple("Marca", marca, { s: String ->
                        marca = s;
                        touched = true
                    }),
                    Triple("Modelo", modelo, { s: String ->
                        modelo = s;
                        touched = true
                    }),
                    Triple("Color", color, { s: String ->
                        color = s;
                        touched = true
                    })
                ).forEach { (label, value, change) ->
                    OutlinedTextField(
                        value,
                        change,
                        label = { Text(label) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    vm.search(
                        InventoryFilter(chasis, marca, modelo, color)
                    )
                }, enabled = !state.busy) { Text("Buscar") };
                OutlinedButton(onClick = {
                    chasis = "";
                    marca = "";
                    modelo = "";
                    color = "";
                    vm.search(InventoryFilter())
                }, enabled = !state.busy) { Text("Limpiar") }
            }
            Spacer(Modifier.height(16.dp));
            Text(
                "${state.total} unidades encontradas",
                fontSize = 12.sp,
                color = Muted
            )
        }
        if (state.rows.isEmpty() && !state.busy && state.error == null) item {
            EmptyState(
                "Sin coincidencias",
                "No hay unidades que coincidan con esta consulta."
            )
        }
        item {
            Text(
                "Motocicletas · selecciona una para ver su ficha",
                fontWeight = FontWeight.SemiBold
            )
        }
        items(state.rows, key = { it.text("id") }) { row ->
            OutlinedCard(
                onClick = { vm.inventoryDetail(row.text("id")) },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TwoWheeler,
                            null,
                            tint = Blue
                        );
                        Spacer(Modifier.width(10.dp));
                        Text(
                            row.text("marca") + " " + row.text("modelo"),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        );
                        Text("#" + row.text("id"), fontSize = 11.sp, color = Muted)
                    };
                    Spacer(Modifier.height(10.dp));
                    Text(
                        "Chasis: " + row.text("chasisVista"),
                        fontSize = 13.sp
                    );
                    Text("Color: " + row.text("color"), fontSize = 12.sp, color = Muted);
                    Spacer(
                        Modifier.height(6.dp)
                    );
                    Text("Ver ficha completa →", fontSize = 12.sp, color = Blue)
                }
            }
        }
        if (state.pages > 0) item {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { vm.page(state.page - 1) },
                        enabled = !state.busy && state.page > 0
                    ) { Text("Anterior") };
                    Text(
                        "${state.page + 1} / ${state.pages}",
                        fontSize = 12.sp
                    );
                    OutlinedButton(
                        onClick = { vm.page(state.page + 1) },
                        enabled = !state.busy && state.page + 1 < state.pages
                    ) { Text("Siguiente") }
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (maxOf(0, state.page - 2)..minOf(
                        state.pages - 1,
                        state.page + 2
                    )).forEach { p ->
                        FilterChip(
                            selected = p == state.page,
                            onClick = { vm.page(p) },
                            enabled = !state.busy,
                            label = { Text("${p + 1}") })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, copy: String) {
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
private fun Notice(message: String, error: Boolean) {
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
private fun OptionSelect(
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
private fun FormDialog(
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
