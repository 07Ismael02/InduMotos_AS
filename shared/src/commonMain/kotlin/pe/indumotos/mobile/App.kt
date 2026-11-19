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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.*

private val Ink=Color(0xFF171A1F)
private val Yellow=Color(0xFFE4D92B)
private val Blue:Color @Composable get()=MaterialTheme.colorScheme.secondary
private val Muted:Color @Composable get()=MaterialTheme.colorScheme.onSurfaceVariant
private fun JsonObject.text(key:String)=get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
private val roleNames=RoleCodes.associateWith(::roleLabel)
private fun icon(m:Module)=when(m){Module.REQUESTS->Icons.Default.Assignment;Module.TRANSFERS->Icons.Default.LocalShipping;Module.INVENTORY->Icons.Default.Inventory2;Module.ARRIVALS->Icons.Default.LocalShipping;Module.FICHAS->Icons.Default.Description;Module.USERS->Icons.Default.PersonAdd;Module.PERMISSIONS->Icons.Default.AdminPanelSettings}
private fun createForm(module:Module):Form=when(module){
 Module.REQUESTS,Module.TRANSFERS->error("Operación mediante su pantalla específica")
 Module.INVENTORY,Module.FICHAS->Form("Registrar ficha de motocicleta","motocicletas",listOf(Field("llegadaId","ID de llegada",numeric=true),Field("marca","Marca"),Field("modelo","Modelo"),Field("color","Color"),Field("chasisCompleto","Chasis completo"),Field("numeroMotor","Número de motor"),Field("dua","DUA"),Field("itemDua","Ítem DUA")))
 Module.ARRIVALS->Form("Registrar llegada","llegadas",listOf(Field("numeroGuia","Número de guía"),Field("cantidadDeclarada","Cantidad declarada",numeric=true),Field("cantidadRecibida","Cantidad recibida",numeric=true),Field("observacion","Observación (opcional)",required=false,maxLength=1000)))
 Module.USERS->Form("Crear cuenta","usuarios",listOf(Field("nombreCompleto","Nombre completo",maxLength=180),Field("nombreUsuario","Usuario",maxLength=80),Field("clave","Contraseña inicial (12–100 caracteres)",secret=true,maxLength=100),Field("rolCodigo","Rol",options=RoleCodes,labels=roleNames)))
 Module.PERMISSIONS->error("Selecciona primero una cuenta")
}
// A theme-aware wordmark; the original bitmap remains in resources unchanged.
@Composable private fun Brand(modifier:Modifier=Modifier){
 Column(modifier,verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
  Text("INDUMOTOS",color=MaterialTheme.colorScheme.onSurface,fontSize=30.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp,maxLines=1)
  Row(Modifier.width(220.dp).padding(top=7.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){
   Box(Modifier.weight(1f).height(3.dp).background(Yellow))
   Box(Modifier.weight(1f).height(3.dp).background(Blue))
   Box(Modifier.weight(1f).height(3.dp).background(Yellow))
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(
    backendUrl: String = "http://127.0.0.1:8089",
    initialTheme: Appearance = Appearance.SYSTEM,
    onAppearanceChanged: (Appearance) -> Unit = {},
    onDarkTheme: (Boolean) -> Unit = {}
){
 val vm:LogisticsViewModel=koinViewModel();val state by vm.state.collectAsState()
 var appearance by remember { mutableStateOf(initialTheme) }
 val darkTheme = when (appearance) {
     Appearance.SYSTEM -> isSystemInDarkTheme()
     Appearance.LIGHT -> false
     Appearance.DARK -> true
 }
 LaunchedEffect(darkTheme) { onDarkTheme(darkTheme) }
 IndumotosTheme(darkTheme = darkTheme){
  Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background){
   if(state.access==null)Login(state,vm,backendUrl)else Workspace(state,vm,appearance) {
       appearance = it
       onAppearanceChanged(it)
   }
   state.form?.let{form->FormDialog(form,state.busy,state.error,{vm.form(null)},vm::submit)}
  }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Workspace(state:UiState,vm:LogisticsViewModel,appearance: Appearance,onAppearanceChanged: (Appearance) -> Unit){
 var showSettings by remember { mutableStateOf(false) }
 var showAccount by remember { mutableStateOf(false) }
 var showProfile by remember { mutableStateOf(false) }
 val account = requireNotNull(state.access)
 val drawer=rememberDrawerState(DrawerValue.Closed);val scope=rememberCoroutineScope();var confirmLogout by remember{mutableStateOf(false)}
 ModalNavigationDrawer(drawerState=drawer,drawerContent={ModalDrawerSheet(modifier=Modifier.widthIn(max=310.dp),drawerContainerColor=MaterialTheme.colorScheme.surface){
  Brand(Modifier.fillMaxWidth().height(96.dp))
  HorizontalDivider();Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp)){
   NavigationDrawerItem(label={Text("Inicio")},icon={Icon(Icons.Default.Home,null)},selected=state.module==null,onClick={if(!state.busy){vm.open(null);scope.launch{drawer.close()}}})
   Text("OPERACIONES",Modifier.padding(16.dp),fontSize=10.sp,color=Muted,letterSpacing=1.sp)
   Module.entries.filter{vm.canRead(it)}.forEach{m->NavigationDrawerItem(label={Text(m.title)},icon={Icon(icon(m),null)},selected=state.module==m,onClick={if(!state.busy){vm.open(m);scope.launch{drawer.close()}}})}
  }
  HorizontalDivider()
  AccountEntry(account, onClick = { showAccount = true })
 }}){
  Scaffold(containerColor=MaterialTheme.colorScheme.background,topBar={TopAppBar(title={Column{Text(state.module?.title?:"Inicio",fontSize=18.sp,fontWeight=FontWeight.SemiBold);Text("INDUMOTOS",fontSize=10.sp,letterSpacing=2.sp,color=Muted)}},navigationIcon={IconButton(onClick={scope.launch{drawer.open()}}){Icon(Icons.Default.Menu,"Abrir menú de navegación")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.surface))},floatingActionButton={
   val m=state.module;val permission=if(m==Module.INVENTORY)"FICHAS_REGISTRAR" else m?.permission
   if(m!=null&&m!=Module.PERMISSIONS&&m!=Module.REQUESTS&&m!=Module.TRANSFERS&&permission!=null&&vm.allowed(permission))ExtendedFloatingActionButton(onClick={vm.form(createForm(m))},expanded=true,icon={Icon(Icons.Default.Add,null)},text={Text(if(m==Module.INVENTORY||m==Module.FICHAS)"Nueva ficha"else if(m==Module.ARRIVALS)"Nueva llegada"else"Nueva cuenta")},containerColor=Yellow,contentColor=Ink)
  }){padding->
   Column(Modifier.padding(padding).fillMaxSize()){
    if(state.busy)LinearProgressIndicator(Modifier.fillMaxWidth(),color=Blue)
    state.error?.let{Notice(it,true)};state.message?.let{Notice(it,false)}
    when(state.module){null->Home(state,vm);Module.INVENTORY->Inventory(state,vm);Module.REQUESTS->RequestsScreen(account,vm::expireSession);Module.TRANSFERS->TransfersScreen(account,vm::expireSession);else->QueryScreen(state,vm)}
   }
  }
 }
 if (showSettings) AppearanceSettings(appearance, onAppearanceChanged) { showSettings = false }
 if (showAccount) AccountMenu(
     account = account,
     canLogout = !state.busy,
     onClose = { showAccount = false },
     onProfile = { showAccount = false; showProfile = true },
     onAppearance = { showAccount = false; showSettings = true },
     onLogout = { showAccount = false; scope.launch { drawer.close() }; confirmLogout = true }
 )
 if (showProfile) AccountProfile(account) { showProfile = false; showAccount = true }
 if(confirmLogout)AlertDialog(onDismissRequest={if(!state.busy)confirmLogout=false},title={Text("Cerrar sesión")},text={Text("Se revocará el acceso de esta sesión en el servidor. Necesitas conexión para confirmarlo.")},confirmButton={TextButton(onClick={confirmLogout=false;vm.logout()},enabled=!state.busy){Text("Cerrar sesión")}},dismissButton={TextButton(onClick={confirmLogout=false}){Text("Cancelar")}})
 if (state.module == Module.INVENTORY) state.detail?.let { record ->
     val location by vm.location.collectAsState()
     val id = record.text("id").toLongOrNull()
     MotorcycleDetail(record, state.busy, vm::closeDetail) {
         if (id != null && vm.allowed(READ_LOCATIONS)) {
             LocationSection(
                 location, vm.allowed(WRITE_LOCATION), state.busy, state.error, state.message,
                 onLoad = { vm.loadLocation(id) },
                 onSave = { vm.registerLocation(id, it) }
             )
         }
     }
 }
}

@Composable private fun Home(state:UiState,vm:LogisticsViewModel){
 LazyColumn(contentPadding=PaddingValues(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Brand(Modifier.fillMaxWidth().height(128.dp))}
  item{Text("Gestión de motocicletas",fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text("Bienvenido, ${state.access!!.nombreUsuario}",fontSize=16.sp);Text(roleLabel(state.access.rolCodigo),color=Blue,fontSize=13.sp);Spacer(Modifier.height(12.dp));Text("Selecciona una operación. Las opciones disponibles corresponden a los permisos de tu cuenta.",fontSize=13.sp,color=Muted,lineHeight=20.sp)}
  items(Module.entries.filter{vm.canRead(it)}){m->OutlinedCard(onClick={vm.open(m)},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){
   Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically){Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(10.dp)){Icon(icon(m),null,tint=MaterialTheme.colorScheme.onSecondaryContainer,modifier=Modifier.padding(12.dp))};Spacer(Modifier.width(16.dp));Column(Modifier.weight(1f)){Text(m.title,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(5.dp));Text(m.description,color=Muted,fontSize=12.sp)};Icon(Icons.Default.ChevronRight,null,tint=Muted)}
  }}
  if(Module.entries.none{vm.canRead(it)})item{Text("Tu cuenta no tiene apartados habilitados. Solicita al administrador que revise tus permisos.",color=Muted)}
 }
}

@Composable private fun Login(state:UiState,vm:LogisticsViewModel,backendUrl:String){
 var user by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var visible by remember{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).imePadding().padding(horizontal=28.dp,vertical=24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
  Brand(Modifier.fillMaxWidth().height(130.dp));Spacer(Modifier.height(30.dp));Text("Iniciar sesión",fontSize=28.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("Gestión de inventario y recepción de motocicletas",color=Muted,fontSize=13.sp,lineHeight=20.sp);Spacer(Modifier.height(28.dp))
  OutlinedTextField(user,{user=it},label={Text("Usuario")},keyboardOptions=KeyboardOptions(autoCorrectEnabled=false,imeAction=ImeAction.Next),singleLine=true,enabled=!state.busy,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(14.dp))
  OutlinedTextField(password,{password=it},label={Text("Contraseña")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,autoCorrectEnabled=false),singleLine=true,enabled=!state.busy,visualTransformation=if(visible)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={IconButton(onClick={visible=!visible}){Icon(if(visible)Icons.Default.VisibilityOff else Icons.Default.Visibility,if(visible)"Ocultar contraseña"else"Mostrar contraseña")}},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(12.dp));state.error?.let{Notice(it,true)};state.message?.let{Notice(it,false)}
  Button(onClick={vm.login(backendUrl,user,password);password=""},enabled=!state.busy&&user.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Yellow,contentColor=Ink)){if(state.busy)CircularProgressIndicator(Modifier.size(22.dp),color=Ink)else Text("Ingresar",fontWeight=FontWeight.SemiBold)}
  Spacer(Modifier.height(20.dp));Text("Acceso exclusivo para personal autorizado. Si necesitas una cuenta, comunícate con el administrador.",fontSize=11.sp,color=Muted,lineHeight=18.sp)
 }
}

@Composable private fun Inventory(state:UiState,vm:LogisticsViewModel){
 var chasis by remember{mutableStateOf("")};var marca by remember{mutableStateOf("")};var modelo by remember{mutableStateOf("")};var color by remember{mutableStateOf("")};var filters by remember{mutableStateOf(false)}
 LazyColumn(contentPadding=PaddingValues(start=20.dp,end=20.dp,top=20.dp,bottom=110.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Text("Consulta de inventario",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("Busca por chasis completo o últimos cuatro dígitos.",fontSize=12.sp,color=Muted);Spacer(Modifier.height(16.dp));OutlinedTextField(chasis,{chasis=it},label={Text("Chasis")},singleLine=true,modifier=Modifier.fillMaxWidth(),leadingIcon={Icon(Icons.Default.Search,null)})
   TextButton(onClick={filters=!filters}){Text(if(filters)"Ocultar filtros"else"Filtrar por marca, modelo y color")}
   if(filters){listOf(Triple("Marca",marca,{s:String->marca=s}),Triple("Modelo",modelo,{s:String->modelo=s}),Triple("Color",color,{s:String->color=s})).forEach{(label,value,change)->OutlinedTextField(value,change,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth().padding(bottom=10.dp))}}
   Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Button(onClick={vm.search(InventoryFilter(chasis,marca,modelo,color))},enabled=!state.busy){Text("Buscar")};OutlinedButton(onClick={chasis="";marca="";modelo="";color="";vm.search(InventoryFilter())},enabled=!state.busy){Text("Limpiar")}}
   Spacer(Modifier.height(16.dp));Text("${state.total} unidades encontradas",fontSize=12.sp,color=Muted)
  }
  if(state.rows.isEmpty()&&!state.busy&&state.error==null)item{EmptyState("Sin coincidencias","No hay unidades que coincidan con esta consulta.")}
  items(state.rows,key={it.text("id")}){row->OutlinedCard(onClick={vm.inventoryDetail(row.text("id"))},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){
   Column(Modifier.padding(18.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.TwoWheeler,null,tint=Blue);Spacer(Modifier.width(10.dp));Text(row.text("marca")+" "+row.text("modelo"),fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));Text("#"+row.text("id"),fontSize=11.sp,color=Muted)};Spacer(Modifier.height(10.dp));Text("Chasis: "+row.text("chasisVista"),fontSize=13.sp);Text("Color: "+row.text("color"),fontSize=12.sp,color=Muted);Spacer(Modifier.height(6.dp));Text("Ver ficha completa →",fontSize=12.sp,color=Blue)}
  }}
  if(state.pages>0)item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){OutlinedButton(onClick={vm.page(state.page-1)},enabled=!state.busy&&state.page>0){Text("Anterior")};Text("${state.page+1} / ${state.pages}",fontSize=12.sp);OutlinedButton(onClick={vm.page(state.page+1)},enabled=!state.busy&&state.page+1<state.pages){Text("Siguiente")}}}
 }
}

@Composable private fun QueryScreen(state:UiState,vm:LogisticsViewModel){
 val module=state.module!!;var id by remember(module){mutableStateOf("")}
 LazyColumn(contentPadding=PaddingValues(start=20.dp,end=20.dp,top=24.dp,bottom=110.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  item{Text(module.title,fontSize=24.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text(module.description,fontSize=13.sp,color=Muted,lineHeight=20.sp)}
  if(module==Module.USERS)item{EmptyState("Cuentas individuales","Usa Nueva cuenta para registrar un trabajador y asignarle su rol. Para consultar accesos, abre Roles y permisos.")}
  val consult=if(module==Module.PERMISSIONS)vm.allowed("PERMISOS_ASIGNAR")else vm.allowed("INVENTARIO_CONSULTAR")
  if(module!=Module.USERS&&consult)item{
   OutlinedCard(colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){Column(Modifier.padding(20.dp)){
    Text(if(module==Module.ARRIVALS)"Consultar recepción"else if(module==Module.PERMISSIONS)"Consultar cuenta"else"Consultar ficha",fontWeight=FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp));OutlinedTextField(id,{id=it},label={Text(if(module==Module.ARRIVALS)"ID de llegada"else if(module==Module.PERMISSIONS)"ID de usuario"else"ID de motocicleta")},singleLine=true,modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));Spacer(Modifier.height(12.dp));Button(onClick={vm.query(id)},enabled=!state.busy&&Rules.positive(id)){Text("Consultar")}
   }}
  }
  state.detail?.let{record->
   if(module==Module.PERMISSIONS)item{PermissionDetail(record,vm)}
   else item{RecordDetail(record)}
  }
  if(module!=Module.USERS&&state.detail==null)item{EmptyState(if(consult)"Consulta por identificador"else"Registro autorizado",if(consult)"Ingresa el identificador asignado por el servidor al guardar el registro."else"Tu cuenta puede registrar información. La consulta requiere el permiso de inventario.")}
 }
}

@Composable private fun PermissionDetail(record:JsonObject,vm:LogisticsViewModel){
 val accountId=record.text("usuarioId");var selectedRole by remember(record){mutableStateOf(record.text("rolCodigo"))};var code by remember(record){mutableStateOf(PermissionLabels.keys.first())};var decision by remember(record){mutableStateOf("DENEGAR")}
 Column(verticalArrangement=Arrangement.spacedBy(16.dp)){
  OutlinedCard(colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){Column(Modifier.padding(20.dp)){
   Text(record.text("nombreUsuario"),fontSize=19.sp,fontWeight=FontWeight.Bold);Text("#"+accountId+" · "+roleLabel(record.text("rolCodigo")),color=Blue,fontSize=12.sp);Spacer(Modifier.height(18.dp))
   record["permisos"]?.jsonArray?.forEach{p->val permission=p.jsonObject;Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Text(PermissionLabels[permission.text("codigo")]?:permission.text("codigo"),modifier=Modifier.weight(1f),fontSize=12.sp);Text(if(permission["permitido"]?.jsonPrimitive?.booleanOrNull==true)"Permitido"else"Denegado",color=if(permission["permitido"]?.jsonPrimitive?.booleanOrNull==true)Blue else MaterialTheme.colorScheme.error,fontSize=11.sp)};HorizontalDivider()}
  }}
  OutlinedCard(colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Text("Modificar acceso",fontWeight=FontWeight.SemiBold);OptionSelect("Rol",RoleCodes,selectedRole,{selectedRole=it},roleNames)
   OutlinedButton(onClick={vm.form(Form("Confirmar cambio de rol de #$accountId","usuarios/$accountId/rol",listOf(Field("rolCodigo","Rol",options=listOf(selectedRole),labels=roleNames)),"PUT",sensitive=true))},enabled=selectedRole!=record.text("rolCodigo")){Text("Cambiar rol")}
   HorizontalDivider();OptionSelect("Permiso",PermissionLabels.keys.toList(),code,{code=it},PermissionLabels);OptionSelect("Excepción",listOf("PERMITIR","DENEGAR"),decision,{decision=it},mapOf("PERMITIR" to "Permitir","DENEGAR" to "Denegar"))
   OutlinedButton(onClick={vm.form(Form("Confirmar excepción de #$accountId","usuarios/$accountId/permisos/$code",listOf(Field("decision","Decisión",options=listOf(decision),labels=mapOf("PERMITIR" to "Permitir","DENEGAR" to "Denegar"))),"PUT",sensitive=true))}){Text("Guardar excepción")}
   TextButton(onClick={vm.form(Form("Retirar excepción de #$accountId","usuarios/$accountId/permisos/$code",emptyList(),"DELETE",sensitive=true))}){Text("Retirar excepción seleccionada")}
  }}
 }
}

private val FieldLabels=mapOf("id" to "Identificador","llegadaId" to "Llegada vinculada","numeroGuia" to "Guía de remisión","fechaLlegada" to "Fecha de recepción","receptorId" to "ID de receptor","receptorUsuario" to "Recibido por","cantidadDeclarada" to "Cantidad declarada","cantidadRecibida" to "Cantidad recibida","observacion" to "Observación","marca" to "Marca","modelo" to "Modelo","color" to "Color","chasisCompleto" to "Chasis completo","numeroMotor" to "Número de motor","dua" to "DUA","itemDua" to "Ítem DUA","creadorUsuario" to "Registrado por","creadoEn" to "Fecha de registro","cantidadFichas" to "Fichas registradas","diferenciaInformativa" to "Diferencia informativa")
@Composable private fun RecordDetail(record:JsonObject){
 OutlinedCard(colors=CardDefaults.outlinedCardColors(containerColor=MaterialTheme.colorScheme.surface)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  record.forEach{(key,value)->when(value){is JsonObject->{Text(if(key=="llegada")"Recepción"else key,fontWeight=FontWeight.SemiBold);RecordDetail(value)};is JsonArray->{Text("Fichas vinculadas (${value.size})",fontWeight=FontWeight.SemiBold);value.forEach{if(it is JsonObject)RecordDetail(it)}};else->{Column{Text(FieldLabels[key]?:key,color=Muted,fontSize=11.sp);Spacer(Modifier.height(4.dp));Text((value as? JsonPrimitive)?.contentOrNull?.ifBlank{"Sin información"}?:"Sin información",fontSize=13.sp)}}}}
 }}
}
@Composable private fun EmptyState(title:String,copy:String){Column(Modifier.fillMaxWidth().padding(vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Info,null,tint=Blue);Spacer(Modifier.height(12.dp));Text(title,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Text(copy,color=Muted,fontSize=12.sp,lineHeight=19.sp)}}
@Composable private fun Notice(message:String,error:Boolean){Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=6.dp),shape=RoundedCornerShape(8.dp),color=if(error)MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer){Text(message,Modifier.padding(14.dp),fontSize=12.sp,lineHeight=19.sp,color=if(error)MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)}}
@Composable private fun OptionSelect(label:String,options:List<String>,value:String,change:(String)->Unit,labels:Map<String,String> = emptyMap()){
 var expanded by remember{mutableStateOf(false)};Column{Text(label,fontSize=12.sp,color=Muted);Spacer(Modifier.height(5.dp));Box{OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(labels[value]?:value,modifier=Modifier.weight(1f));Icon(Icons.Default.ExpandMore,null)};DropdownMenu(expanded,{expanded=false}){options.forEach{option->DropdownMenuItem(text={Text(labels[option]?:option)},onClick={change(option);expanded=false})}}}}
}
@Composable private fun FormDialog(form:Form,busy:Boolean,error:String?,close:()->Unit,submit:(Map<String,String>)->Unit){
 val values=remember(form){mutableStateMapOf<String,String>().apply{form.fields.forEach{put(it.key,it.options.firstOrNull().orEmpty())}}};var local by remember(form){mutableStateOf<String?>(null)};var confirmed by remember(form){mutableStateOf(false)}
 AlertDialog(onDismissRequest={if(!busy)close()},title={Text(form.title,fontSize=20.sp)},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
  if(form.sensitive){Text("Este cambio modifica el acceso de la cuenta. Revisa el identificador y la operación antes de confirmar.",fontSize=12.sp);Row(verticalAlignment=Alignment.CenterVertically){Checkbox(confirmed,{confirmed=it},enabled=!busy);Text("Confirmo el cambio de acceso",fontSize=12.sp)}}
  form.fields.forEach{f->if(f.options.isNotEmpty())OptionSelect(f.label,f.options,values[f.key].orEmpty(),{if(!busy)values[f.key]=it},f.labels)else OutlinedTextField(values[f.key].orEmpty(),{if(it.length<=f.maxLength)values[f.key]=it},label={Text(f.label)},enabled=!busy,modifier=Modifier.fillMaxWidth(),visualTransformation=if(f.secret)PasswordVisualTransformation()else VisualTransformation.None,keyboardOptions=KeyboardOptions(keyboardType=if(f.numeric)KeyboardType.Number else if(f.secret)KeyboardType.Password else KeyboardType.Text),singleLine=f.key!="observacion")}
  (local?:error)?.let{Text(it,color=MaterialTheme.colorScheme.error,fontSize=12.sp)}
 }},confirmButton={TextButton(onClick={local=if(form.fields.any{it.required&&values[it.key].isNullOrBlank()})"Completa los campos obligatorios"else null;if(local==null)submit(values.toMap())},enabled=!busy&&(!form.sensitive||confirmed)){Text(if(busy)"Guardando…"else if(form.sensitive)"Confirmar"else"Guardar")}},dismissButton={TextButton(onClick=close,enabled=!busy){Text("Cancelar")}})
}
