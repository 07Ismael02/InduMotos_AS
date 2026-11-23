package pe.indumotos.mobile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.CatalogViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable fun CatalogScreen(account: Access, expired: () -> Unit) {
    val vm: CatalogViewModel=koinViewModel();val state by vm.state.collectAsState()
    var brand by remember(account.usuarioId){mutableStateOf("")}
    var model by remember(account.usuarioId){mutableStateOf("")}
    var color by remember(account.usuarioId){mutableStateOf("")}
    var showForm by remember(account.usuarioId){mutableStateOf(false)}
    var confirmLeave by remember{mutableStateOf(false)}
    val returnHome=LocalReturnHome.current
    LocalSystemBack.current(true) {
        if(!state.busy) {
            if(showForm&&listOf(brand,model,color).any{it.isNotBlank()}&&!state.options.containsCombination(brand,model,color))confirmLeave=true
            else returnHome()
        }
    }
    DisposableEffect(account.usuarioId){onDispose{vm.reset()}}
    LaunchedEffect(account.usuarioId){vm.reset();vm.load()}
    LaunchedEffect(state.expired){if(state.expired)expired()}
    Box(Modifier.fillMaxSize().imePadding(),contentAlignment=Alignment.TopCenter) {
    LazyColumn(Modifier.widthIn(max=840.dp).fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Catálogo",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                    Text("${state.options.brands().size} marcas · ${state.options.size} combinaciones",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick=vm::load,enabled=!state.busy){Icon(Icons.Default.Refresh,"Actualizar catálogo")}
            }
            Text("Marcas, modelos y colores disponibles para el registro y las solicitudes.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
            state.message?.let{Text(it,color=MaterialTheme.colorScheme.secondary)}
        }
        if(account.rolCodigo=="ADMIN" && account.permisos.any{it.codigo==MANAGE_CATALOG&&it.permitido}) item {
            if(!showForm) FilledTonalButton(onClick={showForm=true},modifier=Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add,null);Spacer(Modifier.width(8.dp));Text("Nueva combinación")
            }
            else OutlinedCard(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text("Nueva combinación",style=MaterialTheme.typography.titleMedium)
                Text("Elige un valor existente o añade uno nuevo desde su lista.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                BoxWithConstraints {
                val wide=maxWidth>=600.dp
                @Composable fun fields(modifier:Modifier) {
                Column(modifier){CatalogValue("Marca",brand,state.options.brands(),!state.busy) {
                    if(brand!=it){brand=it;model="";color=""}
                }}
                Column(modifier){
                key(brand) {
                    CatalogValue("Modelo",model,state.options.models(brand),!state.busy&&brand.isNotBlank()) {
                        if(model!=it){model=it;color=""}
                    }
                }}
                Column(modifier){
                key(brand,model) {
                    // En administración se puede reutilizar un color existente para una combinación nueva.
                    CatalogValue("Color",color,state.options.map{it.color}.distinct().sorted(),!state.busy&&brand.isNotBlank()&&model.isNotBlank()) {color=it}
                }}
                }
                if(wide)Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){fields(Modifier.weight(1f))}
                else Column(verticalArrangement=Arrangement.spacedBy(12.dp)){fields(Modifier.fillMaxWidth())}
                }
                Button(onClick={vm.add(brand,model,color)},modifier=Modifier.fillMaxWidth(),enabled=!state.busy&&listOf(brand,model,color).all{it.isNotBlank()}){Text("Guardar combinación")}
                TextButton(onClick={showForm=false},enabled=!state.busy,modifier=Modifier.align(Alignment.End)){Text("Cerrar formulario")}
            } }
        }
        items(state.options.groupBy{it.marca}.toList(),key={it.first}) { (brand, options) ->
            OutlinedCard(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(brand,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
                options.groupBy{it.modelo}.forEach{(model,rows)->
                    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text(model,style=MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            rows.map{it.color}.distinct().forEach{color->Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(8.dp)) {
                                Text(color,Modifier.padding(horizontal=10.dp,vertical=6.dp),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSecondaryContainer)
                            }}
                        }
                    }
                }
            } }
        }
        if(!state.busy&&state.options.isEmpty())item{Text("No hay combinaciones disponibles en el catálogo.")}
    }
    }
    if(confirmLeave)AlertDialog(onDismissRequest={confirmLeave=false},title={Text("Combinación sin guardar")},
        text={Text("Hay información nueva sin guardar. ¿Deseas descartarla y volver a Inicio?")},
        confirmButton={TextButton(onClick={confirmLeave=false;returnHome()}){Text("Descartar y volver")}},
        dismissButton={TextButton(onClick={confirmLeave=false}){Text("Seguir editando")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CatalogValue(label:String,value:String,options:List<String>,enabled:Boolean,change:(String)->Unit) {
    var expanded by remember{mutableStateOf(false)}
    var writing by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        if(writing) {
            OutlinedTextField(value,{if(it.length<=120)change(it)},label={Text("$label nuevo")},
                singleLine=true,enabled=enabled,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Next),modifier=Modifier.fillMaxWidth())
            TextButton(onClick={writing=false;change("")},enabled=enabled){Text("Elegir de la lista")}
        } else {
            ExposedDropdownMenuBox(expanded&&enabled,onExpandedChange={if(enabled)expanded=it}) {
                OutlinedTextField(value=value,onValueChange={},readOnly=true,label={Text(label)},placeholder={Text("Seleccionar")},enabled=enabled,
                    trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded&&enabled)},singleLine=true,
                    modifier=Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable,enabled).fillMaxWidth())
                ExposedDropdownMenu(expanded&&enabled,onDismissRequest={expanded=false},modifier=Modifier.heightIn(max=280.dp)) {
                    options.forEach{option->DropdownMenuItem(text={Text(option)},onClick={expanded=false;change(option)})}
                    HorizontalDivider()
                    DropdownMenuItem(text={Text("+ Añadir ${label.lowercase()}")},onClick={expanded=false;writing=true;change("")})
                }
            }
        }
    }
}
