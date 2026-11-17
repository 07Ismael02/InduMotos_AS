param([string]$Serial)
$ErrorActionPreference = 'Stop'
if (-not $Serial) { throw 'Indica el dispositivo autorizado: ./scripts/Conectar-USB.ps1 -Serial RFGL11XYCTD' }
$state = & adb -s $Serial get-state 2>$null
if ($state -ne 'device') { throw 'Conecta el teléfono, desbloquéalo y acepta la autorización de depuración USB.' }
& adb -s $Serial reverse tcp:8089 tcp:8089
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el túnel USB.' }
& adb -s $Serial reverse --list
Write-Host 'En Indumotos AS, servidor: http://127.0.0.1:8089. No se modificó la instalación ni sus datos.'
