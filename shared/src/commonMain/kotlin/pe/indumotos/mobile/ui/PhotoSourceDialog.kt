package pe.indumotos.mobile.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PhotoSourceDialog(onCamera: () -> Unit, onGallery: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Añadir fotografía") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Toma una fotografía o selecciona una imagen guardada.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = onCamera,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        null
                    );
                    Spacer(Modifier.width(12.dp));
                    Text("Tomar foto")
                }
                OutlinedButton(
                    onClick = onGallery,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        null
                    );
                    Spacer(Modifier.width(12.dp));
                    Text("Elegir de galería")
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } }
    )
}
