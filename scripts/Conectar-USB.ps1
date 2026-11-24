param([string]$Serial, [ValidateRange(1,65535)][int]$BackendPort=8090, [ValidateRange(1,65535)][int]$AppPort=8089)
$ErrorActionPreference = 'Stop'
if (-not $Serial) { throw 'Indica el dispositivo autorizado: ./scripts/Conectar-USB.ps1 -Serial RFGL11XYCTD' }
$state = & adb -s $Serial get-state 2>$null
if ($state -ne 'device') { throw 'Conecta el teléfono, desbloquéalo y acepta la autorización de depuración USB.' }
& adb -s $Serial reverse "tcp:$AppPort" "tcp:$BackendPort"
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el túnel USB.' }
& adb -s $Serial reverse --list
Write-Host "Túnel: teléfono 127.0.0.1:$AppPort → PC 127.0.0.1:$BackendPort. No se modificó la instalación ni sus datos."
