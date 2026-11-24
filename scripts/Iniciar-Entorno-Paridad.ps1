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
Write-Host 'API local: http://127.0.0.1:8090/api/health. App: 127.0.0.1:8089 mediante USB.'
