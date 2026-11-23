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
    DisposableEffect(account.usuarioId){onDispose{vm.reset()}}
    LaunchedEffect(account.usuarioId){vm.reset();vm.load()}
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
                Text("Selecciona información existente o usa Añadir para registrar un valor nuevo.",style=MaterialTheme.typography.bodySmall)
                CatalogValue("Marca",brand,state.options.brands(),!state.busy) {
                    if(brand!=it){brand=it;model="";color=""}
                }
                key(brand) {
                    CatalogValue("Modelo",model,state.options.models(brand),!state.busy&&brand.isNotBlank()) {
                        if(model!=it){model=it;color=""}
                    }
                }
                key(brand,model) {
                    // En administración se puede reutilizar un color existente para una combinación nueva.
                    CatalogValue("Color",color,state.options.map{it.color}.distinct().sorted(),!state.busy&&brand.isNotBlank()&&model.isNotBlank()) {color=it}
                }
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

@Composable private fun CatalogValue(label:String,value:String,options:List<String>,enabled:Boolean,change:(String)->Unit) {
    var expanded by remember{mutableStateOf(false)}
    var writing by remember{mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        if(writing) {
            OutlinedTextField(value,{if(it.length<=120)change(it)},label={Text("$label nuevo")},
                singleLine=true,enabled=enabled,modifier=Modifier.fillMaxWidth())
            TextButton(onClick={writing=false;change("")},enabled=enabled){Text("Elegir de la lista")}
        } else {
            Box {
                OutlinedButton(onClick={expanded=true},enabled=enabled,modifier=Modifier.fillMaxWidth()) {
                    Text("$label: ${value.ifBlank{"Seleccionar ▾"}}")
                }
                DropdownMenu(expanded&&enabled,onDismissRequest={expanded=false},modifier=Modifier.heightIn(max=280.dp)) {
                    options.forEach{option->DropdownMenuItem(text={Text(option)},onClick={expanded=false;change(option)})}
                    HorizontalDivider()
                    DropdownMenuItem(text={Text("+ Añadir ${label.lowercase()}")},onClick={expanded=false;writing=true;change("")})
                }
            }
        }
    }
}
