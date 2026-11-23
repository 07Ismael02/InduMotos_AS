package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.CatalogViewModel

@Composable fun CatalogScreen(account: Access, expired: () -> Unit) {
    val vm: CatalogViewModel=koinViewModel();val state by vm.state.collectAsState()
    var brand by remember(account.usuarioId){mutableStateOf("")}
    var model by remember(account.usuarioId){mutableStateOf("")}
    var color by remember(account.usuarioId){mutableStateOf("")}
    LaunchedEffect(account.usuarioId){vm.load()}
    LaunchedEffect(state.expired){if(state.expired)expired()}
    LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text("Catálogo de motocicletas",style=MaterialTheme.typography.headlineSmall)
            Text("Administración define las combinaciones que Inventarios y Comercial pueden seleccionar.")
            OutlinedButton(onClick=vm::load,enabled=!state.busy){Text("Actualizar")}
            if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
            state.message?.let{Text(it,color=MaterialTheme.colorScheme.secondary)}
        }
        if(account.rolCodigo=="ADMIN" && account.permisos.any{it.codigo==MANAGE_CATALOG&&it.permitido}) item {
            OutlinedCard { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("Agregar marca, modelo y color",fontWeight=FontWeight.SemiBold)
                OutlinedTextField(brand,{if(it.length<=120){brand=it;model=""}},label={Text("Marca")},enabled=!state.busy)
                OutlinedTextField(model,{if(it.length<=120)model=it},label={Text("Modelo")},enabled=!state.busy)
                OutlinedTextField(color,{if(it.length<=120)color=it},label={Text("Color")},enabled=!state.busy)
                Button(onClick={vm.add(brand,model,color)},enabled=!state.busy&&listOf(brand,model,color).all{it.isNotBlank()}){Text("Agregar combinación")}
            } }
        }
        state.options.groupBy{it.marca}.forEach { (brand, options) -> item {
            OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(brand,fontWeight=FontWeight.Bold)
                options.groupBy{it.modelo}.forEach{(model,rows)->Text("$model · ${rows.map{it.color}.distinct().joinToString(" · ")}")}
            } }
        } }
        if(!state.busy&&state.options.isEmpty())item{Text("No hay combinaciones disponibles en el catálogo.")}
    }
}
