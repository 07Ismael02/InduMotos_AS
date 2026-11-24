package pe.indumotos.mobile.domain

data class ProfileAccessGroup(val name:String,val permissions:List<Permission>)

fun Access.profileAccessGroups():List<ProfileAccessGroup> = permisos.filter{it.permitido}.distinctBy{it.codigo}
    .groupBy{permission->when(permission.codigo.substringBefore('_')){
        "LLEGADAS","RECEPCIONES"->"Recepción de motocicletas"
        "INVENTARIO","FICHAS","DISPONIBILIDAD","DIFERENCIAS"->"Inventario y fichas"
        "CATALOGO"->"Catálogo"
        "SOLICITUDES","UNIDADES","ENVIOS"->"Solicitudes y ensamblaje"
        "UBICACIONES","TRASLADOS"->"Ubicaciones y traslados"
        "TIENDA","VENTAS","PAGOS"->"Ventas y pagos"
        "USUARIOS","PERMISOS"->"Administración del personal"
        "REPORTES"->"Reportes"
        else->"Otros accesos"
    }}.map{(name,permissions)->ProfileAccessGroup(name,permissions)}.sortedBy{it.name}
