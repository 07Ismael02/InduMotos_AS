package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import pe.indumotos.mobile.domain.OfflineStatus

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable internal fun OfflineBanner(status:OfflineStatus,busy:Boolean,canDownload:Boolean,download:()->Unit,sync:()->Unit,login:()->Unit) {
    Surface(color=if(status.offline)MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text(if(status.offline)"Sin servidor · consulta local" else "Datos del servidor",style=MaterialTheme.typography.labelLarge)
            Text("${status.pending} borradores en el teléfono · ${status.conflicts} requieren revisión",style=MaterialTheme.typography.bodySmall)
            status.lastDownloadMs?.let{Text("Última lectura online: "+kotlin.time.Instant.fromEpochMilliseconds(it).toString().replace('T',' ').take(16)+" UTC",style=MaterialTheme.typography.labelSmall)}
            if(status.syncing)LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.End) {
                if(status.readOnlySession)TextButton(onClick=login,enabled=!busy){Text("Iniciar sesión online")}
                else {
                    if(canDownload)TextButton(onClick=download,enabled=!busy&&!status.syncing&&!status.offline){Text("Descargar datos")}
                    TextButton(onClick=sync,enabled=!busy&&!status.syncing){Text("Sincronizar")}
                }
            }
            status.message?.let{Text(it,style=MaterialTheme.typography.bodySmall)}
        }
    }
}
