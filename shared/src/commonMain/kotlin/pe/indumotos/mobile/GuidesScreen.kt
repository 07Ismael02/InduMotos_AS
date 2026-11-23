package pe.indumotos.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.koin.compose.viewmodel.koinViewModel
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*

@Composable fun GuidesScreen(account:Access,editing:Boolean,pickPhotos:((PhotoSelection)->Unit)->Unit,expired:()->Unit) {
    val vm:GuidesViewModel=koinViewModel();val s by vm.state.collectAsState()
    fun can(code:String)=account.permisos.any{it.codigo==code&&it.permitido}
    val canEdit=editing&&can("FICHAS_REGISTRAR")&&can("INVENTARIO_CONSULTAR")
    var query by remember{mutableStateOf("")};var creating by remember{mutableStateOf(false)}
    var confirmReload by remember{mutableStateOf(false)};var confirmFinish by remember{mutableStateOf(false)};var confirmLeave by remember{mutableStateOf(false)}
    var page by remember(s.selected?.id){mutableStateOf(0)};var zoom by remember{mutableStateOf(false)}
    var leaveHome by remember{mutableStateOf(false)}
    val returnHome=LocalReturnHome.current
    LocalSystemBack.current(true) {
        if(!s.busy) {
            if(s.registration?.let{r->s.drafts.any{d->r.borradores.none{it==d}}}==true){leaveHome=true;confirmLeave=true}
            else returnHome()
        }
    }
    DisposableEffect(account.usuarioId,editing){onDispose{vm.reset()}}
    LaunchedEffect(account.usuarioId,editing){vm.reset();if(can("INVENTARIO_CONSULTAR"))vm.list()}
    LaunchedEffect(s.expired){if(s.expired)expired()}
    LaunchedEffect(s.selected?.id){if(s.selected!=null)creating=false}
    fun leave(){leaveHome=false;if(s.registration?.let{r->s.drafts.any{d->r.borradores.none{it==d}}}==true)confirmLeave=true else vm.list(s.query,s.filter)}
    Column(Modifier.fillMaxSize()) {
        if(s.selected!=null)OutlinedButton(onClick={leave()},enabled=!s.busy,modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp)){Text("← Volver a guías")}
        if(s.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        s.error?.let{Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(16.dp))}
        s.message?.let{Text(it,color=MaterialTheme.colorScheme.secondary,modifier=Modifier.padding(16.dp))}
        val arrival=s.selected
        if(arrival==null) LazyColumn(contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {
                Text(if(editing)"Registrar motocicletas de una guía" else "Recepción de guías",style=MaterialTheme.typography.headlineSmall)
                Text(if(editing)"Selecciona una guía pendiente. Guarda cada moto como borrador antes de finalizar el registro." else "Almacén registra lo recibido y adjunta las fotos de la guía.")
                if(!editing&&can("LLEGADAS_REGISTRAR"))Button(onClick={creating=true},enabled=!s.busy){Text("Nueva llegada con fotos")}
                if(can("INVENTARIO_CONSULTAR")) {
                    OutlinedTextField(query,{query=it},label={Text("Número de guía")},singleLine=true,enabled=!s.busy,modifier=Modifier.fillMaxWidth())
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected=s.filter=="PENDIENTE",onClick={vm.list(query,"PENDIENTE")},enabled=!s.busy,label={Text("Pendientes")})
                        FilterChip(selected=s.filter=="CONFIRMADA",onClick={vm.list(query,"CONFIRMADA")},enabled=!s.busy,label={Text("Confirmadas")})
                    }
                    OutlinedButton(onClick={vm.list(query,s.filter)},enabled=!s.busy){Text("Buscar / actualizar")}
                } else Text("Tu cuenta puede registrar la recepción; la consulta requiere permiso de inventario.")
            }
            items(s.page?.items.orEmpty(),key={it.id}) { row -> OutlinedCard(onClick={vm.select(row,canEdit)},enabled=!s.busy,modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text("Guía ${row.numeroGuia}",style=MaterialTheme.typography.titleMedium)
                    Text("${row.estadoRegistro} · ${row.cantidadRecibida} recibidas")
                    Text("${row.fechaLlegada} · ${row.receptorUsuario}")
                    Text(if(canEdit&&row.estadoRegistro=="PENDIENTE")"Continuar registro →" else "Consultar guía →")
                }
            } }
            if(!s.busy&&s.page?.items?.isEmpty()==true)item{Text("No hay guías que coincidan con esta consulta.")}
            s.page?.let{result->item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick={vm.list(s.query,s.filter,result.pagina-1)},enabled=!s.busy&&result.pagina>0){Text("Anterior")}
                Text("${result.pagina+1} / ${result.totalPaginas.coerceAtLeast(1)}",Modifier.padding(top=14.dp))
                OutlinedButton(onClick={vm.list(s.query,s.filter,result.pagina+1)},enabled=!s.busy&&result.pagina+1<result.totalPaginas){Text("Siguiente")}
            }}}
        } else {
            Text("Guía ${arrival.numeroGuia}",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(horizontal=16.dp))
            Text("Declaradas: ${arrival.cantidadDeclarada} · Recibidas: ${arrival.cantidadRecibida}",modifier=Modifier.padding(horizontal=16.dp))
            val registration=s.registration
            if(s.imageBytes!=null) {
                GuidePicture(requireNotNull(s.imageBytes),Modifier.fillMaxWidth().height(170.dp))
                Row(Modifier.padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick={vm.image(s.imageIndex-1)},enabled=!s.busy&&s.imageIndex>0){Text("‹")}
                    Text("Foto ${s.imageIndex+1} de ${s.images.size}",Modifier.padding(top=14.dp))
                    OutlinedButton(onClick={vm.image(s.imageIndex+1)},enabled=!s.busy&&s.imageIndex+1<s.images.size){Text("›")}
                    TextButton(onClick={zoom=true}){Text("Ampliar")}
                }
            }
            LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                item {
                    if(registration!=null&&can("INVENTARIO_CONSULTAR"))OutlinedButton(onClick={
                        if(s.drafts.any{d->registration.borradores.none{it==d}})confirmReload=true else vm.select(arrival,canEdit)
                    },enabled=!s.busy){Text("Actualizar desde servidor")}
                    registration?.let{Text("Registradas: ${it.registradas} · Pendientes: ${it.pending} · ${if(it.terminado)"Confirmada" else "Pendiente"}")}
                    if(s.images.isEmpty()&&!s.busy)Text("Esta recepción no tiene fotos adjuntas.")
                    if(registration==null&&!s.busy&&can("INVENTARIO_CONSULTAR"))OutlinedButton(onClick={vm.select(arrival,canEdit)}){Text("Cargar detalle de guía")}
                    if(canEdit&&registration!=null&&!registration.terminado)OutlinedButton(onClick=vm::ocr,enabled=!s.busy&&s.images.isNotEmpty()){Text("Leer fotos con OCR")}
                }
                if(canEdit&&registration!=null&&!registration.terminado) {
                    s.ocr?.let{result->item {
                        Text("${result.fotosAnalizadas} fotos analizadas; ${result.sugerencias.size} sugerencias. Comprueba cada carácter antes de guardar.")
                        Row { Checkbox(s.ocrReviewed,vm::reviewed,enabled=!s.busy);Text("Revisé los datos contra las fotos") }
                    }}
                    items(s.drafts.drop(page*5).take(5),key={it.posicion}) { draft ->
                        val saved=registration.borradores.any{it==draft}
                        DraftEditor(draft,s.options,s.busy,vm::edit)
                        Text(if(saved)"Guardado en el servidor" else "Cambios sin guardar",color=if(saved)MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                        s.ocr?.sugerencias?.getOrNull(draft.posicion-1)?.let { suggestion ->
                            Text("OCR: ${suggestion.marca} ${suggestion.modelo} · ${suggestion.color} · ${suggestion.chasisCompleto}")
                            if(registration.borradores.none{it.posicion==draft.posicion})TextButton(onClick={vm.useSuggestion(draft.posicion-1,draft.posicion)},enabled=!s.busy){Text("Usar sugerencia y revisar")}
                        }
                        Button(onClick={vm.save(draft)},enabled=!s.busy&&!saved&&s.ocrReviewed){Text("Guardar borrador ${draft.posicion}")}
                        HorizontalDivider()
                    }
                    item {
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick={page--},enabled=!s.busy&&page>0){Text("5 anteriores")}
                            OutlinedButton(onClick={page++},enabled=!s.busy&&(page+1)*5<s.drafts.size){Text("5 siguientes")}
                        }
                        Button(onClick={confirmFinish=true},enabled=!s.busy&&s.ocrReviewed&&GuideRules.canFinish(registration,s.drafts),modifier=Modifier.fillMaxWidth()){Text("Finalizar registro de la guía")}
                        Text("Solo finalizar incorpora las fichas al inventario. No registres motos que no llegaron físicamente.")
                    }
                } else if(registration?.terminado==true)item{Text("Registro confirmado. Esta guía está en modo consulta.")}
                else if(!editing)item{Text("Para registrar las motocicletas, Inventarios debe abrir Fichas y seleccionar esta guía.")}
            }
        }
    }
    if(creating)NewArrivalDialog(s.busy,s.error,pickPhotos,{creating=false},vm::register)
    if(zoom&&s.imageBytes!=null)ZoomGuide(requireNotNull(s.imageBytes)){zoom=false}
    if(confirmReload)AlertDialog(onDismissRequest={confirmReload=false},title={Text("Recargar registro")},text={Text("Se conservarán los borradores del servidor y se descartarán los cambios locales sin guardar.")},confirmButton={TextButton(onClick={confirmReload=false;s.selected?.let{vm.select(it,canEdit)}}){Text("Recargar")}},dismissButton={TextButton(onClick={confirmReload=false}){Text("Cancelar")}})
    if(confirmFinish)AlertDialog(onDismissRequest={confirmFinish=false},title={Text("Finalizar registro")},text={Text("Se incorporarán las motocicletas guardadas y la guía quedará confirmada. Verifica los datos y las cantidades antes de continuar.")},confirmButton={Button(onClick={confirmFinish=false;vm.finish()},enabled=!s.busy){Text("Finalizar")}},dismissButton={TextButton(onClick={confirmFinish=false}){Text("Cancelar")}})
    if(confirmLeave)AlertDialog(onDismissRequest={confirmLeave=false},title={Text("Cambios sin guardar")},text={Text("Hay bloques editados que aún no están guardados en el servidor. ¿Deseas salir y descartar esos cambios locales?")},confirmButton={TextButton(onClick={confirmLeave=false;if(leaveHome)returnHome() else vm.list(s.query,s.filter)}){Text("Descartar y volver")}},dismissButton={TextButton(onClick={confirmLeave=false}){Text("Seguir editando")}})
}

@Composable internal fun ChoiceField(label:String,value:String,options:List<String>,enabled:Boolean,change:(String)->Unit) {
    var expanded by remember(options){mutableStateOf(false)}
    Box {
        OutlinedCard(onClick={expanded=true},enabled=enabled,modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)){Text(label,style=MaterialTheme.typography.labelMedium);Text(value.ifBlank{"Seleccionar"})}
        }
        DropdownMenu(expanded&&enabled,{expanded=false},modifier=Modifier.heightIn(max=280.dp)) {
            options.forEach{option->DropdownMenuItem(text={Text(option)},onClick={change(option);expanded=false})}
            if(options.isEmpty())DropdownMenuItem(text={Text("Sin opciones disponibles")},onClick={},enabled=false)
        }
    }
}

@Composable internal fun DraftEditor(d:MotorcycleDraft,options:List<CatalogOption>,busy:Boolean,edit:(MotorcycleDraft)->Unit) {
    Text("Motocicleta ${d.posicion}",style=MaterialTheme.typography.titleMedium)
    ChoiceField("Marca",d.marca,options.brands(),!busy){edit(d.copy(marca=it,modelo="",color=""))}
    ChoiceField("Modelo",d.modelo,options.models(d.marca),!busy&&d.marca.isNotBlank()){edit(d.copy(modelo=it,color=""))}
    ChoiceField("Color",d.color,options.colors(d.marca,d.modelo),!busy&&d.modelo.isNotBlank()){edit(d.copy(color=it))}
    OutlinedTextField(d.chasisCompleto,{if(it.length<=120)edit(d.copy(chasisCompleto=it))},label={Text("Chasis completo")},enabled=!busy,singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(d.numeroMotor,{if(it.length<=120)edit(d.copy(numeroMotor=it))},label={Text("Número de motor")},enabled=!busy,singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(d.dua,{if(it.length<=120)edit(d.copy(dua=it))},label={Text("DUA")},enabled=!busy,singleLine=true,modifier=Modifier.fillMaxWidth())
    OutlinedTextField(d.itemDua,{if(it.length<=120)edit(d.copy(itemDua=it))},label={Text("Ítem DUA")},enabled=!busy,singleLine=true,modifier=Modifier.fillMaxWidth())
}

@Composable private fun NewArrivalDialog(busy:Boolean,error:String?,pick:((PhotoSelection)->Unit)->Unit,close:()->Unit,submit:(String,Int,Int,String,List<GuidePhoto>)->Unit) {
    var guide by remember{mutableStateOf("")};var declared by remember{mutableStateOf("")};var received by remember{mutableStateOf("")};var note by remember{mutableStateOf("")}
    var photos by remember{mutableStateOf<List<GuidePhoto>>(emptyList())};var local by remember{mutableStateOf<String?>(null)}
    AlertDialog(onDismissRequest={if(!busy)close()},title={Text("Nueva llegada")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(guide,{if(it.length<=120)guide=it},label={Text("Número de guía")},enabled=!busy)
        OutlinedTextField(declared,{declared=it},label={Text("Cantidad declarada")},enabled=!busy,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        OutlinedTextField(received,{received=it},label={Text("Cantidad recibida")},enabled=!busy,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        OutlinedTextField(note,{if(it.length<=1000)note=it},label={Text("Observación opcional")},enabled=!busy)
        OutlinedButton(onClick={pick{result->local=result.error;if(result.error==null&&result.photos.isNotEmpty())photos=result.photos}},enabled=!busy){Text("Seleccionar de 1 a 3 fotos")}
        Text("JPG/PNG · hasta 5 MB por foto · mínimo 600 × 600 px · máximo 20 MP.")
        photos.forEachIndexed{i,photo->GuidePicture(photo.bytes,Modifier.fillMaxWidth().height(110.dp));Text("Foto ${i+1} · ${photo.name}")}
        (local?:error)?.let{Text(it,color=MaterialTheme.colorScheme.error)}
    }},confirmButton={Button(onClick={
        local=runCatching{GuideRules.arrival(guide,declared.toIntOrNull()?:0,received.toIntOrNull()?:0,photos)}.exceptionOrNull()?.message
        if(local==null)submit(guide,declared.toInt(),received.toInt(),note,photos)
    },enabled=!busy){Text("Registrar llegada")}},dismissButton={TextButton(onClick=close,enabled=!busy){Text("Cancelar")}})
}

@Composable private fun GuidePicture(bytes:ByteArray,modifier:Modifier) {
    val bitmap=remember(bytes){decodeGuideImage(bytes)}
    if(bitmap!=null)Image(bitmap,"Foto de la guía para revisión",modifier) else Text("No se pudo mostrar esta imagen.")
}
@Composable private fun ZoomGuide(bytes:ByteArray,close:()->Unit) {
    var scale by remember{mutableStateOf(1f)};var x by remember{mutableStateOf(0f)};var y by remember{mutableStateOf(0f)}
    val transform=rememberTransformableState{zoom,pan,_->scale=(scale*zoom).coerceIn(1f,6f);x+=pan.x;y+=pan.y}
    Dialog(onDismissRequest=close){Surface{Column {
        Text("Amplía con dos dedos y arrastra para revisar.",Modifier.padding(12.dp))
        Box(Modifier.fillMaxWidth().height(420.dp).transformable(transform)){GuidePicture(bytes,Modifier.fillMaxSize().graphicsLayer{scaleX=scale;scaleY=scale;translationX=x;translationY=y})}
        TextButton(onClick=close){Text("Volver al registro")}
    }}}
}
