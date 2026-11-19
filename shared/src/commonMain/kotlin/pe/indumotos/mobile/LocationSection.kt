package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.indumotos.mobile.domain.MotorcycleLocation
import pe.indumotos.mobile.domain.Site

@Composable
internal fun LocationSection(
    location: MotorcycleLocation?,
    canRegister: Boolean,
    busy: Boolean,
    error: String?,
    message: String?,
    onLoad: () -> Unit,
    onSave: (Site) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selection by remember(location) { mutableStateOf<Site?>(null) }
    DetailGroup("Ubicación") {
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        message?.let { Text(it) }
        if (location != null) {
            val current = location.current
            Text(current?.destinoNombre ?: "Sin ubicación registrada", style = MaterialTheme.typography.titleSmall)
            if (current != null) {
                DetailValue("Registró", current.registrador)
                DetailValue("Fecha", recordTimestamp(current.fecha))
            }
            if (current != null) {
                Text(
                    "La ubicación inicial ya está registrada. Para mover la motocicleta, registra un traslado.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (canRegister) {
                val options = location.destinations
                if (options.isEmpty()) {
                    Text("No hay ubicaciones disponibles.")
                } else {
                    Box {
                        OutlinedButton(onClick = { expanded = true }, enabled = !busy) {
                            Text(selection?.nombre ?: "Elegir ubicación")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            options.forEach { site ->
                                DropdownMenuItem(text = { Text("${site.nombre} · ${site.codigo}") }, onClick = {
                                    selection = site
                                    expanded = false
                                })
                            }
                        }
                    }
                    Button(onClick = { selection?.let(onSave) }, enabled = !busy && selection != null) {
                        Text("Registrar ubicación")
                    }
                }
            }
        }
        if (location == null) {
            Button(onClick = onLoad, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Consultar ubicación")
            }
        } else {
            OutlinedButton(onClick = onLoad, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Actualizar ubicación consultada")
            }
        }
    }
}
