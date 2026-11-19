package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.indumotos.mobile.domain.Access
import pe.indumotos.mobile.domain.roleLabel

@Composable
internal fun AccountEntry(account: Access, onClick: () -> Unit) {
    val initials = account.nombreUsuario.split('.', '-', '_', ' ')
        .filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().semantics {
        contentDescription = "Abrir mi cuenta"
    }) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { Text(initials) }
            }
            Column(Modifier.weight(1f)) {
                Text(account.nombreUsuario, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(roleLabel(account.rolCodigo), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
    }
}

@Composable
internal fun AccountMenu(
    account: Access,
    canLogout: Boolean,
    onClose: () -> Unit,
    onProfile: () -> Unit,
    onAppearance: () -> Unit,
    onLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Mi cuenta") },
        text = {
            Column {
                Text(account.nombreUsuario, style = MaterialTheme.typography.titleSmall)
                Text(roleLabel(account.rolCodigo))
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                AccountAction("Perfil", Icons.Default.Person, onProfile)
                AccountAction("Apariencia", Icons.Default.Palette, onAppearance)
                HorizontalDivider()
                AccountAction("Cerrar sesión", Icons.AutoMirrored.Filled.Logout, onLogout, canLogout)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Cerrar") } }
    )
}

@Composable
private fun AccountAction(label: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
internal fun AccountProfile(account: Access, onBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onBack,
        title = { Text("Mi perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Usuario: ${account.nombreUsuario}")
                Text("Rol: ${roleLabel(account.rolCodigo)}")
                Text("Identificador: ${account.usuarioId}")
            }
        },
        confirmButton = { TextButton(onClick = onBack) { Text("Volver a mi cuenta") } }
    )
}
