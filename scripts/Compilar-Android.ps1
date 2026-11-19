param(
    [switch]$ConPruebas,
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AndroidSdk = $env:ANDROID_HOME,
    [string]$BackendUrl
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Push-Location $root
try {
    if (-not $JavaHome) { $JavaHome = Join-Path $env:ProgramFiles 'Android/Android Studio/jbr' }
    if (-not (Test-Path -LiteralPath "$JavaHome/bin/java.exe")) { throw 'Indica -JavaHome o JAVA_HOME con un JDK compatible con Gradle 9.1 (21 recomendado).' }
    $env:JAVA_HOME = $JavaHome
    if (-not $AndroidSdk) { $AndroidSdk = $env:ANDROID_SDK_ROOT }
    if (-not $AndroidSdk) { $AndroidSdk = Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
    if (-not (Test-Path -LiteralPath 'local.properties')) {
        [IO.File]::WriteAllText((Join-Path $root 'local.properties'), 'sdk.dir=' + $AndroidSdk.Replace('\','/') + "`n")
    }
    $tasks = @(':androidApp:assembleDebug')
    if ($ConPruebas) { $tasks += ':shared:testAndroidHostTest' }
    $configuration = @()
    if ($BackendUrl) { $configuration += "-Pindumotos.backendUrl=$BackendUrl" }
    & ./gradlew.bat @tasks @configuration --console=plain --max-workers=2 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'No se generó un APK válido. Revisa la salida de Gradle.' }
    $delivery = Join-Path $root 'entrega'
    New-Item -ItemType Directory -Path $delivery -Force | Out-Null
    $builtApk = Join-Path $root 'androidApp/build/outputs/apk/debug/androidApp-debug.apk'
    $signer = Get-ChildItem -LiteralPath (Join-Path $AndroidSdk 'build-tools') -Directory |
        Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } |
        Sort-Object { [version]$_.Name } -Descending |
        ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
        Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    if (-not $signer) { throw 'No se encontró apksigner en el SDK indicado. No se publicó un APK de entrega.' }
    & $signer verify --print-certs $builtApk
    if ($LASTEXITCODE -ne 0) { throw 'La firma del APK no pasó la verificación.' }
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $apk = Join-Path $delivery "Indumotos-AS-login-$stamp-debug.apk"
    Copy-Item -LiteralPath $builtApk -Destination $apk
    Get-FileHash -LiteralPath $apk -Algorithm SHA256
    Write-Output 'APK verificado; no instalado. Comparar el certificado con el APK del dispositivo antes de actualizarlo.'
} finally { Pop-Location }
