param(
    [string]$BackendPath = "$env:USERPROFILE\Documents\PharmaMobil\proyecto-final-dam\BACK-END-INDUMOTOS",
    [string]$ConfigPath = "$env:LOCALAPPDATA\IndumotosAS\sprint1.env"
)
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $ConfigPath)) { throw 'Falta la configuración privada de este equipo. Consulta docs/INTEGRACION-SPRINT1.md; no copies credenciales al repositorio.' }
$allowed = @('DB_URL','DB_USER','DB_PASSWORD','ADMIN_USERNAME','ADMIN_PASSWORD','JWT_SECRET_BASE64','PORT','SERVER_ADDRESS')
foreach ($line in Get-Content -LiteralPath $ConfigPath) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$' -and $matches[1] -in $allowed) {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}
$jar = Join-Path $BackendPath 'target/backend-indumotos-0.1.0-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $jar)) { throw 'Compila el backend con Java 21 y Maven: mvn -Dmaven.test.skip=true package. No se instalarán herramientas automáticamente.' }
if ($env:SERVER_ADDRESS -ne '127.0.0.1' -or $env:PORT -ne '8089') { throw 'Esta configuración local debe usar 127.0.0.1:8089.' }
& 'C:/Program Files/Zulu/zulu-21/bin/java.exe' -Xms128m -Xmx512m -jar $jar
if ($LASTEXITCODE -ne 0) { throw 'El backend no pudo iniciarse. Conserva la base y revisa la salida.' }
