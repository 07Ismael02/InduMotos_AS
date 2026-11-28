package pe.indumotos.mobile.presentation.session

import pe.indumotos.mobile.ui.*
import pe.indumotos.mobile.presentation.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*


@Composable
internal fun Login(
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
internal fun OfflineAccountDialog(
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
