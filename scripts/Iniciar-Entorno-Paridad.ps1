param([string]$Serial)
$ErrorActionPreference='Stop'
docker start indumotos-distribuidas-db-1
if($LASTEXITCODE -ne 0){throw 'No se inició PostgreSQL existente. No recrear volumen.'}
docker start indumotos-paridad-api
if($LASTEXITCODE -ne 0){throw 'Falta el contenedor local V23/OCR. Consultar docs/CIERRE-PARIDAD-20261123.md; no usar el backend antiguo.'}
if($Serial){
    adb -s $Serial reverse tcp:8089 tcp:8090
    if($LASTEXITCODE -ne 0){throw 'Reconecta y autoriza el teléfono; los contenedores quedan conservados.'}
    adb -s $Serial reverse --list
}
$ready=$false
for($attempt=0;$attempt -lt 30;$attempt++){
    try{if((Invoke-WebRequest 'http://127.0.0.1:8090/api/health' -TimeoutSec 2).StatusCode -eq 200){$ready=$true;break}}catch{}
    Start-Sleep -Seconds 1
}
if(-not $ready){throw 'La API todavía no responde. Revisar Docker antes de iniciar sesión.'}
Write-Host 'API local: http://127.0.0.1:8090/api/health. App: 127.0.0.1:8089 mediante USB.'
