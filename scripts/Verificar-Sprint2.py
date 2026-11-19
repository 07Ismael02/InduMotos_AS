"""HTTP integration checks. Writes academic data ONLY to the isolated verification DB.
Secrets are read locally; Authorization and passwords are never recorded.
"""
import argparse, datetime, json, os, pathlib, subprocess, urllib.request, urllib.error

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--base-url',default='http://127.0.0.1:8090')
    parser.add_argument('--config',default=str(pathlib.Path(os.environ['LOCALAPPDATA'])/'IndumotosAS/sprint1.env'))
    parser.add_argument('--output',default='docs/evidencias/sprint2/http-resultados.json')
    args=parser.parse_args()
    if args.base_url not in ('http://127.0.0.1:8090','http://localhost:8090'):
        raise SystemExit('Este verificador exige el backend local aislado en 8090.')
    config=dict(line.split('=',1) for line in pathlib.Path(args.config).read_text().splitlines() if '=' in line and not line.startswith('#'))
    now=lambda:datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=-5))).isoformat(timespec='seconds')
    tag='ACADEMICO-S2-'+datetime.datetime.now().strftime('%Y%m%d%H%M%S')
    report={'inicio':now(),'baseUrl':args.base_url,'baseDatos':'indumotos_s2_verificacion','datosSinteticos':tag,'casos':[]}
    token=''
    def api(label,method,path,body=None,expected=200,authenticated=True):
        headers={'Content-Type':'application/json'}
        if authenticated and token:headers['Authorization']='Bearer '+token
        data=json.dumps(body).encode() if body is not None else None
        request=urllib.request.Request(args.base_url+path,data=data,headers=headers,method=method)
        try:
            with urllib.request.urlopen(request,timeout=30) as response:status=response.status;text=response.read().decode()
        except urllib.error.HTTPError as e:status=e.code;text=e.read().decode()
        report['casos'].append({'fecha':now(),'caso':label,'metodo':method,'ruta':path,'esperado':expected,'obtenido':status,'aprobado':status==expected})
        if status!=expected:raise AssertionError(f'{label}: esperado {expected}, obtenido {status}')
        return json.loads(text) if text else None
    def check(label,passed):
        report['casos'].append({'fecha':now(),'caso':label,'aprobado':bool(passed)})
        if not passed:raise AssertionError(label)
    def sql(query):
        return subprocess.check_output(['docker','exec','indumotos-distribuidas-db-1','psql','-U','indumotos','-d','indumotos_s2_verificacion','-qAt','-v','ON_ERROR_STOP=1','-c',query],text=True).strip()
    try:
        check('Base aislada y Flyway V1–V15 aplicadas',sql("SELECT count(*) FROM flyway_schema_history WHERE success AND version::integer BETWEEN 1 AND 15")=='15')
        api('Salud','GET','/api/health',authenticated=False)
        api('Solicitudes sin token','GET','/api/v1/solicitudes',expected=401,authenticated=False)
        token=api('Login administrador existente','POST','/api/v1/auth/login',{'nombreUsuario':config['ADMIN_USERNAME'],'clave':config['ADMIN_PASSWORD']},authenticated=False)['accessToken']
        profile=api('Perfil vigente','GET','/api/v1/auth/me');report['rol']=profile['rolCodigo']
        api('Administrador sin permiso de recepción','POST','/api/v1/llegadas',{'numeroGuia':tag,'cantidadDeclarada':3,'cantidadRecibida':3},expected=403)
        sites=[]
        for suffix,kind in [('ALM','ALMACEN'),('TDA','TIENDA')]:
            sites.append(api('Crear sede académica '+kind,'POST','/api/v1/ubicaciones',{'codigo':tag+'-'+suffix,'nombre':'Verificación académica '+suffix,'tipo':kind},expected=201))
        # Fixture only: admin cannot receive stock through API by design. No role is elevated.
        user=config['ADMIN_USERNAME'].replace("'","''")
        arrival=int(sql(f"INSERT INTO llegada(numero_guia,id_receptor,cantidad_declarada,cantidad_recibida,observacion) SELECT '{tag}',id_usuario,3,3,'Datos académicos aislados' FROM usuario WHERE nombre_usuario='{user}' RETURNING id_llegada"))
        motorcycles=[]
        for i,(brand,model,color) in enumerate([('Yamaha','FZ','Azul'),('Honda','Wave','Rojo'),('Yamaha','FZ','Azul')],1):
            mid=int(sql(f"INSERT INTO motocicleta(id_llegada,marca,modelo,color,chasis_completo,numero_motor,dua,item_dua,id_creador) SELECT {arrival},'{brand}','{model}','{color}','{tag}-{i}','{tag}-M{i}','ACADEMICO','{i}',id_usuario FROM usuario WHERE nombre_usuario='{user}' RETURNING id_motocicleta"))
            motorcycles.append(mid)
            api('Ubicación inicial moto '+str(mid),'POST',f'/api/v1/motocicletas/{mid}/ubicacion',{'destinoId':sites[0]['id']},expected=201)
        report['fixture']={'llegada':arrival,'motos':motorcycles,'sedes':[s['id'] for s in sites]}
        api('Cantidad cero rechazada','POST','/api/v1/solicitudes',{'destinoId':sites[1]['id'],'lineas':[{'marca':'Yamaha','modelo':'FZ','color':'Azul','cantidad':0}]},expected=400)
        lines=[{'marca':'Yamaha','modelo':'FZ','color':'Azul','cantidad':1},{'marca':'Honda','modelo':'Wave','color':'Rojo','cantidad':1}]
        request=api('Solicitud con dos líneas','POST','/api/v1/solicitudes',{'destinoId':sites[1]['id'],'observacion':tag,'lineas':lines},expected=201)
        rid=request['id'];report['solicitudPrincipal']=rid
        loaded=api('Consulta posterior de solicitud','GET',f'/api/v1/solicitudes/{rid}')
        check('Cabecera y dos líneas persistidas',loaded['estado']=='PENDIENTE' and len(loaded['lineas'])==2)
        api('Asignación antes de preparar rechazada','POST',f"/api/v1/solicitudes/{rid}/lineas/{loaded['lineas'][0]['id']}/unidades",{'motocicletaId':motorcycles[0]},expected=409)
        api('Cambio a preparación','POST',f'/api/v1/solicitudes/{rid}/seguimiento',{'estado':'EN_PREPARACION','motivo':'Inicio de verificación'})
        associations=[]
        for line,mid in zip(loaded['lineas'],motorcycles):
            candidates=api('Búsqueda real de chasis elegible','GET','/api/v1/motocicletas/elegibles?chasis='+tag)
            check('Candidato presente antes de asignar',any(c['motocicletaId']==mid for c in candidates))
            path=f"/api/v1/solicitudes/{rid}/lineas/{line['id']}/unidades"
            unit=api('Asignar chasis a línea','POST',path,{'motocicletaId':mid},expected=201);associations.append(unit)
            api('Cupo/duplicado bloqueado','POST',path,{'motocicletaId':mid},expected=409)
            check('Consulta de unidad asignada',any(u['motocicletaId']==mid for u in api('Leer asignaciones','GET',path)))
        first=f"/api/v1/solicitudes/{rid}/lineas/{loaded['lineas'][0]['id']}/unidades"
        removal=first+'/'+str(associations[0]['asociacionId'])
        api('Retiro sin motivo rechazado','DELETE',removal,{'motivo':''},expected=400)
        api('Retiro con motivo','DELETE',removal,{'motivo':'Verificación de retiro'},expected=204)
        api('Reasignación después del retiro','POST',first,{'motocicletaId':motorcycles[0]},expected=201)
        api('Confirmar atención','POST',f'/api/v1/solicitudes/{rid}/seguimiento',{'estado':'ATENDIDA','motivo':'Dos chasis preparados'})
        api('Retroceso de estado bloqueado','POST',f'/api/v1/solicitudes/{rid}/seguimiento',{'estado':'PENDIENTE'},expected=409)
        api('Traslado a tienda','POST',f'/api/v1/motocicletas/{motorcycles[0]}/traslados',{'destinoId':sites[1]['id']},expected=201)
        api('Traslado al mismo destino bloqueado','POST',f'/api/v1/motocicletas/{motorcycles[0]}/traslados',{'destinoId':sites[1]['id']},expected=409)
        current=api('Ubicación final','GET',f'/api/v1/motocicletas/{motorcycles[0]}/ubicacion')
        check('Destino final y responsable',current['destinoId']==sites[1]['id'] and current['registrador']==config['ADMIN_USERNAME'])
        history=api('Historial conserva origen y destino','GET',f'/api/v1/motocicletas/{motorcycles[0]}/ubicacion/historial')
        check('Historial de ubicación inicial y traslado',len(history)==2 and history[-1]['tipo']=='TRASLADO')
        second=api('Solicitud para comprobar liberación por rechazo','POST','/api/v1/solicitudes',{'destinoId':sites[1]['id'],'lineas':lines[:1]},expected=201)
        sid=second['id'];lid=second['lineas'][0]['id']
        api('Preparar segunda solicitud','POST',f'/api/v1/solicitudes/{sid}/seguimiento',{'estado':'EN_PREPARACION'})
        api('Asignación exclusiva entre solicitudes','POST',f'/api/v1/solicitudes/{sid}/lineas/{lid}/unidades',{'motocicletaId':motorcycles[0]},expected=409)
        api('Asignar tercera moto de prueba','POST',f'/api/v1/solicitudes/{sid}/lineas/{lid}/unidades',{'motocicletaId':motorcycles[2]},expected=201)
        api('Rechazar con motivo','POST',f'/api/v1/solicitudes/{sid}/seguimiento',{'estado':'RECHAZADA','motivo':'Comprobación de liberación'})
        check('Rechazo libera asignaciones',api('Unidades después del rechazo','GET',f'/api/v1/solicitudes/{sid}/lineas/{lid}/unidades')==[])
        third=api('Solicitud para anulación','POST','/api/v1/solicitudes',{'destinoId':sites[1]['id'],'lineas':lines[:1]},expected=201)
        cid=third['id']
        api('Anulación sin motivo rechazada','POST',f'/api/v1/solicitudes/{cid}/anulacion',{'motivo':''},expected=400)
        api('Anulación pendiente con motivo','POST',f'/api/v1/solicitudes/{cid}/anulacion',{'motivo':'Solicitud académica innecesaria'})
        api('Doble anulación bloqueada','POST',f'/api/v1/solicitudes/{cid}/anulacion',{'motivo':'Segundo intento'},expected=409)
        check('Solicitud anulada se conserva',api('Leer solicitud anulada','GET',f'/api/v1/solicitudes/{cid}')['estado']=='ANULADA')
        events=api('Historial de anulación','GET',f'/api/v1/solicitudes/{cid}/seguimiento')
        check('Motivo y actor de anulación conservados',any(e['estado']=='ANULADA' and e['motivo'] and e['registrador']==config['ADMIN_USERNAME'] for e in events))
        report['comprobacionBD']={'solicitudes':sql(f"SELECT count(*) FROM solicitud_ensamblaje WHERE id_solicitud IN ({rid},{sid},{cid})"),'movimientosMoto':sql(f'SELECT count(*) FROM movimiento_motocicleta WHERE id_motocicleta={motorcycles[0]}')}
        check('Persistencia comprobada en PostgreSQL',report['comprobacionBD']=={'solicitudes':'3','movimientosMoto':'2'})
        api('Logout revoca sesión','POST','/api/v1/auth/logout',expected=204)
        api('Token revocado rechazado','GET','/api/v1/auth/me',expected=401)
        report['resultado']='APROBADO'
    except Exception as e:
        report['resultado']='FALLO';report['bloqueo']=str(e)
        raise
    finally:
        report['fin']=now();output=pathlib.Path(args.output);output.parent.mkdir(parents=True,exist_ok=True)
        output.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
        print(json.dumps({'resultado':report.get('resultado'),'casos':len(report['casos']),'aprobados':sum(c['aprobado'] for c in report['casos']),'evidencia':str(output)},ensure_ascii=False))

if __name__=='__main__':main()
