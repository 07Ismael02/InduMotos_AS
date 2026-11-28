package pe.indumotos.mobile.presentation.inventory

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*


@Composable
internal fun Inventory(state: UiState, vm: LogisticsViewModel) {
    var chasis by remember { mutableStateOf("") };

    var marca by remember { mutableStateOf("") };

    var modelo by remember { mutableStateOf("") };

    var color by remember { mutableStateOf("") };

    var filters by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    LaunchedEffect(chasis, marca, modelo, color, state.busy) {
        if (touched && !state.busy) {
            delay(300);

            val filter = InventoryFilter(chasis, marca, modelo, color)
            val value = chasis.trim()
            if (filter != state.filter && (value.isEmpty() || value.length > 4 || (value.length == 4 && value.all { it.isDigit() }))) vm.search(
                filter
            )
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 110.dp
        ), verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (state.module == Module.FICHAS) "Fichas de motocicletas" else "Consulta de inventario",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            );
            Text(
                "Busca por chasis completo o últimos cuatro dígitos.",
                fontSize = 12.sp,
                color = Muted
            );
            Spacer(Modifier.height(16.dp));
            OutlinedTextField(
                chasis,
                {
                    chasis = it;
                    touched = true
                },
                label = { Text("Chasis") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) })
            TextButton(onClick = {
                filters = !filters
            }) { Text(if (filters) "Ocultar filtros" else "Filtrar por marca, modelo y color") }
            if (filters) {
                listOf(
                    Triple("Marca", marca, { s: String ->
                        marca = s;
                        touched = true
                    }),
                    Triple("Modelo", modelo, { s: String ->
                        modelo = s;
                        touched = true
                    }),
                    Triple("Color", color, { s: String ->
                        color = s;
                        touched = true
                    })
                ).forEach { (label, value, change) ->
                    OutlinedTextField(
                        value,
                        change,
                        label = { Text(label) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    vm.search(
                        InventoryFilter(chasis, marca, modelo, color)
                    )
                }, enabled = !state.busy) { Text("Buscar") };
                OutlinedButton(onClick = {
                    chasis = "";
                    marca = "";
                    modelo = "";
                    color = "";
                    vm.search(InventoryFilter())
                }, enabled = !state.busy) { Text("Limpiar") }
            }
            Spacer(Modifier.height(16.dp));
            Text(
                "${state.total} unidades encontradas",
                fontSize = 12.sp,
                color = Muted
            )
        }
        if (state.rows.isEmpty() && !state.busy && state.error == null) item {
            EmptyState(
                "Sin coincidencias",
                "No hay unidades que coincidan con esta consulta."
            )
        }
        item {
            Text(
                "Motocicletas · selecciona una para ver su ficha",
                fontWeight = FontWeight.SemiBold
            )
        }
        items(state.rows, key = { it.text("id") }) { row ->
            OutlinedCard(
                onClick = { vm.inventoryDetail(row.text("id")) },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TwoWheeler,
                            null,
                            tint = Blue
                        );
                        Spacer(Modifier.width(10.dp));
                        Text(
                            row.text("marca") + " " + row.text("modelo"),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        );
                        Text("#" + row.text("id"), fontSize = 11.sp, color = Muted)
                    };
                    Spacer(Modifier.height(10.dp));
                    Text(
                        "Chasis: " + row.text("chasisVista"),
                        fontSize = 13.sp
                    );
                    Text("Color: " + row.text("color"), fontSize = 12.sp, color = Muted);
                    Spacer(
                        Modifier.height(6.dp)
                    );
                    Text("Ver ficha completa →", fontSize = 12.sp, color = Blue)
                }
            }
        }
        if (state.pages > 0) item {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { vm.page(state.page - 1) },
                        enabled = !state.busy && state.page > 0
                    ) { Text("Anterior") };
                    Text(
                        "${state.page + 1} / ${state.pages}",
                        fontSize = 12.sp
                    );
                    OutlinedButton(
                        onClick = { vm.page(state.page + 1) },
                        enabled = !state.busy && state.page + 1 < state.pages
                    ) { Text("Siguiente") }
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (maxOf(0, state.page - 2)..minOf(
                        state.pages - 1,
                        state.page + 2
                    )).forEach { p ->
                        FilterChip(
                            selected = p == state.page,
                            onClick = { vm.page(p) },
                            enabled = !state.busy,
                            label = { Text("${p + 1}") })
                    }
                }
            }
        }
    }
}
