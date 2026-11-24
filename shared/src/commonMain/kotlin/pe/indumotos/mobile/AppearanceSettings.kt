package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

enum class Appearance(val label: String) {
    SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro")
}

@Composable
internal fun AppearanceSettings(
    appearance: Appearance,
    onChange: (Appearance) -> Unit,
    onClose: () -> Unit
) {
    AccountPage("Preferencias", onClose) {
        Column(Modifier.selectableGroup()) {
            Text("Tema de la aplicación")
            Appearance.entries.forEach { option ->
                val selected = appearance == option
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onChange(option) }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Text(option.label, Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}
