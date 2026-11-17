param([switch]$ConPruebas)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Push-Location $root
try {
    $env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
    if (-not (Test-Path -LiteralPath "$env:JAVA_HOME/bin/java.exe")) { throw 'Configura JAVA_HOME con un JDK 21.' }
    if (-not (Test-Path -LiteralPath 'local.properties')) {
        $sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
        [IO.File]::WriteAllText((Join-Path $root 'local.properties'), 'sdk.dir=' + $sdk.Replace('\','/') + "`n")
    }
    $tasks = @(':androidApp:assembleDebug')
    if ($ConPruebas) { $tasks += ':shared:testAndroidHostTest' }
    & ./gradlew.bat @tasks --console=plain --max-workers=2 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'No se generó un APK válido. Revisa la salida de Gradle.' }
    $delivery = Join-Path $root 'entrega'
    New-Item -ItemType Directory -Path $delivery -Force | Out-Null
    $apk = Join-Path $delivery 'Indumotos-AS-Sprint1-debug.apk'
    Copy-Item -LiteralPath 'androidApp/build/outputs/apk/debug/androidApp-debug.apk' -Destination $apk -Force
    Get-FileHash -LiteralPath $apk -Algorithm SHA256
} finally { Pop-Location }
