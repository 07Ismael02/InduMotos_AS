package pe.indumotos.mobile

import pe.indumotos.mobile.ui.*
import pe.indumotos.mobile.presentation.session.*
import pe.indumotos.mobile.presentation.inventory.*
import pe.indumotos.mobile.presentation.guides.*
import pe.indumotos.mobile.presentation.operations.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*


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
                if (m == Module.INVENTORY && vm.canRead(Module.FICHAS) && vm.allowed("FICHAS_REGISTRAR")) ExtendedFloatingActionButton(
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
            if (!offlineStatus.offline && vm.allowed("FICHAS_EDITAR")) OutlinedButton(
                onClick = vm::beginEdit,
                enabled = !state.busy
            ) { Text("Editar ficha") }
            if (id != null && vm.allowed(READ_LOCATIONS)) {
                LocationSection(
                    location, !offlineStatus.offline && vm.allowed(WRITE_LOCATION), state.busy, state.error, state.message,
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
