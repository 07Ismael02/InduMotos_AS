package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ubicación", style = MaterialTheme.typography.titleMedium)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        message?.let { Text(it) }
        if (location != null) {
            val current = location.current
            Text(current?.destinoNombre ?: "Sin ubicación registrada")
            if (current != null) {
                Text("Registró: ${current.registrador}")
                Text("Fecha: ${current.fecha}")
            }
            if (canRegister) {
                val options = location.destinations.filterNot { it.id == current?.destinoId }
                if (options.isEmpty()) {
                    Text("No hay otras ubicaciones disponibles.")
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
        TextButton(onClick = onLoad, enabled = !busy) {
            Text(if (location == null) "Consultar ubicación" else "Actualizar consulta")
        }
    }
}
