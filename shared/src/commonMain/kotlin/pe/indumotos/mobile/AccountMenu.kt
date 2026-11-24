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
import androidx.compose.runtime.*
import pe.indumotos.mobile.domain.profileAccessGroups
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun AccountPage(title:String,onBack:()->Unit,content:@Composable ColumnScope.()->Unit){
    Dialog(onDismissRequest=onBack,properties=DialogProperties(usePlatformDefaultWidth=false,dismissOnClickOutside=false)){
        Scaffold(topBar={TopAppBar(title={Text(title)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Volver")}})}){padding->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp),content=content)
        }
    }
}

@Composable private fun ProfileHeader(account:Access){
    ElevatedCard(Modifier.fillMaxWidth()){
        Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
            Surface(shape=CircleShape,color=MaterialTheme.colorScheme.primaryContainer){Box(Modifier.size(72.dp),contentAlignment=Alignment.Center){Icon(Icons.Default.Person,null,Modifier.size(36.dp))}}
            Text(account.nombreUsuario,style=MaterialTheme.typography.headlineSmall)
            Text(roleLabel(account.rolCodigo),color=MaterialTheme.colorScheme.primary)
            Text("INDUMOTOS",style=MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable internal fun AccountMenu(account:Access,canLogout:Boolean,onClose:()->Unit,onProfile:()->Unit,onAppearance:()->Unit,onLogout:()->Unit){
    AccountPage("Ajustes personales",onClose){
        ProfileHeader(account)
        Text("CUENTA",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedCard(Modifier.fillMaxWidth()){
            AccountAction("Perfil y acceso","Usuario y permisos de tu cuenta",Icons.Default.Person,onProfile)
            HorizontalDivider()
            AccountAction("Preferencias","Tema claro, oscuro o del sistema",Icons.Default.Palette,onAppearance)
        }
        Text("SEGURIDAD",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedCard(Modifier.fillMaxWidth()){AccountAction("Cerrar sesión","Finalizar el acceso actual",Icons.AutoMirrored.Filled.Logout,onLogout,canLogout)}
        Text("La consulta local está protegida por el desbloqueo de Android y requiere un acceso online realizado en las últimas 24 horas.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun AccountAction(label:String,description:String,icon:ImageVector,onClick:()->Unit,enabled:Boolean=true){
    Surface(onClick=onClick,enabled=enabled){
        Row(Modifier.fillMaxWidth().padding(16.dp).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
            Icon(icon,null,tint=MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)){Text(label,style=MaterialTheme.typography.titleSmall);Text(description,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            Icon(Icons.Default.ChevronRight,null)
        }
    }
}

@Composable internal fun AccountProfile(account:Access,onBack:()->Unit){
    AccountPage("Perfil y acceso",onBack){
        ProfileHeader(account)
        OutlinedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Información de la cuenta",style=MaterialTheme.typography.titleMedium)
            Text("Usuario",style=MaterialTheme.typography.labelMedium);Text(account.nombreUsuario)
            Text("Rol asignado",style=MaterialTheme.typography.labelMedium);Text(roleLabel(account.rolCodigo))
        }}
        Text("Funciones de tu cuenta",style=MaterialTheme.typography.titleMedium)
        Text("Estos son los módulos en los que puedes trabajar. Abre uno para consultar sus acciones autorizadas.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        account.profileAccessGroups().forEach{group->
            var expanded by remember(account.usuarioId,group.name){mutableStateOf(false)}
            OutlinedCard(onClick={expanded=!expanded},modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){Text(group.name,style=MaterialTheme.typography.titleSmall,modifier=Modifier.weight(1f));Text(if(expanded)"Ocultar"else"Ver acciones",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)}
                    if(expanded){HorizontalDivider();group.permissions.forEach{permission->Text("• "+(pe.indumotos.mobile.domain.PermissionLabels[permission.codigo]?:permission.codigo.lowercase().replace('_',' ').replaceFirstChar{it.uppercase()}),style=MaterialTheme.typography.bodySmall)}}
                }
            }
        }
        if(account.profileAccessGroups().isEmpty())Text("Esta cuenta no tiene módulos operativos habilitados.")
        Text("Para corregir tus datos o recuperar tu contraseña, comunícate con el administrador. Los permisos se confirman en el servidor al iniciar sesión.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
