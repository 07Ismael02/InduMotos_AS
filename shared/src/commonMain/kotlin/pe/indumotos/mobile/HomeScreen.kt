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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*


@Composable
internal fun Home(state: UiState, vm: LogisticsViewModel) {
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
                            FieldAccess.description(m, vm::allowed),
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
                "Tu cuenta no tiene operaciones de campo. La gestión de personal, permisos, catálogo y ventas se realiza en el sistema web.",
                color = Muted
            )
        }
    }
}
