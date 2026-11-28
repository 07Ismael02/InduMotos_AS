package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import pe.indumotos.mobile.domain.OfflineStatus
import pe.indumotos.mobile.domain.limaTimestamp

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
internal fun OfflineBanner(
    status: OfflineStatus,
    busy: Boolean,
    canDownload: Boolean,
    download: () -> Unit,
    sync: () -> Unit,
    login: () -> Unit
) {
    var details by remember { mutableStateOf(false) }
    Surface(color = if (status.offline) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                Text(
                    if (status.offline) "Sin servidor · consulta local" else "Datos del servidor",
                    style = MaterialTheme.typography.labelLarge
                )
                if (status.pending > 0 || status.conflicts > 0) Text(
                    "${status.pending} pendientes · ${status.conflicts} por revisar",
                    style = MaterialTheme.typography.labelSmall
                )
                if (status.syncing) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            TextButton(onClick = { details = true }) { Text("Ver estado") }
        }
    }
    if (details) AlertDialog(
        onDismissRequest = { details = false },
        title = { Text("Datos y sincronización") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (status.offline) "Sin servidor · consulta local" else "Datos del servidor",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "${status.pending} borradores en el teléfono · ${status.conflicts} requieren revisión",
                    style = MaterialTheme.typography.bodySmall
                )
                status.lastDownloadMs?.let {
                    Text(
                        "Última lectura online: " + limaTimestamp(
                            kotlin.time.Instant.fromEpochMilliseconds(it).toString()
                        ),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (status.syncing) LinearProgressIndicator(Modifier.fillMaxWidth())
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    if (status.readOnlySession) TextButton(onClick = login, enabled = !busy) {
                        Text(
                            "Iniciar sesión online"
                        )
                    }
                    else {
                        if (canDownload) TextButton(
                            onClick = download,
                            enabled = !busy && !status.syncing && !status.offline
                        ) { Text("Descargar datos") }
                        TextButton(
                            onClick = sync,
                            enabled = !busy && !status.syncing
                        ) { Text("Sincronizar") }
                    }
                }
                status.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = { details = false }) { Text("Volver") } })
}
