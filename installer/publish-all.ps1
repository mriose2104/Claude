<#
.SYNOPSIS
    Compila y publica los cuatro proyectos de Monitor de Uso de Aplicaciones
    listos para instalar (servicio + dashboard).

.PARAMETER SelfContained
    Si se indica, publica ejecutables autocontenidos (no requieren tener el
    .NET Runtime instalado en el equipo destino), a costa de un tamano mayor.

.EXAMPLE
    .\publish-all.ps1
    .\publish-all.ps1 -SelfContained
#>
param(
    [switch]$SelfContained,
    [string]$Runtime = "win-x64",
    [string]$Configuration = "Release"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$outDir = Join-Path $root "publish"
$selfContainedArg = "false"
if ($SelfContained.IsPresent) { $selfContainedArg = "true" }

function Publish-Project($projectRelativePath, $outputSubfolder) {
    $projectPath = Join-Path $root $projectRelativePath
    $outputPath = Join-Path $outDir $outputSubfolder
    Write-Host "==> Publicando $projectRelativePath -> $outputPath" -ForegroundColor Cyan

    dotnet publish $projectPath `
        -c $Configuration `
        -r $Runtime `
        --self-contained $selfContainedArg `
        -o $outputPath

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo la publicacion de $projectRelativePath"
    }
}

# El monitor puede estar corriendo (arrancado por la tarea programada, o
# revivido por su disparador vigilante) y bloquear su propio .exe/.dll,
# haciendo fallar la publicacion. Se detiene antes de publicar y se vuelve
# a arrancar al final, sin importar si la publicacion tuvo exito o no.
$taskName = "AppUsageMonitorTask"
$task = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
$reiniciarTarea = $false
if ($task) {
    $reiniciarTarea = ($task.State -eq "Running")
    if ($reiniciarTarea) {
        Write-Host "==> Deteniendo temporalmente la tarea '$taskName' para poder publicar..." -ForegroundColor Yellow
        Stop-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    }
}
Get-Process -Name "AppUsageMonitor.MonitorService" -ErrorAction SilentlyContinue | Stop-Process -Force

# El Dashboard tambien bloquea su propio .exe/.dll si quedo abierto (incluso
# minimizado a la bandeja). A diferencia del monitor, no se vuelve a abrir
# solo al terminar: hay que abrirlo a mano despues para probar la version
# nueva.
$dashboardCorriendo = Get-Process -Name "AppUsageMonitor.Dashboard" -ErrorAction SilentlyContinue
if ($dashboardCorriendo) {
    Write-Host "==> Cerrando el Dashboard (seguia abierto/en la bandeja) para poder publicar..." -ForegroundColor Yellow
    $dashboardCorriendo | Stop-Process -Force
}

try {
    Publish-Project "src\AppUsageMonitor.MonitorService\AppUsageMonitor.MonitorService.csproj" "MonitorService"
    Publish-Project "src\AppUsageMonitor.Dashboard\AppUsageMonitor.Dashboard.csproj" "Dashboard"
}
finally {
    if ($reiniciarTarea) {
        Write-Host "==> Reiniciando la tarea '$taskName'..." -ForegroundColor Yellow
        Start-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "Publicacion completa en: $outDir" -ForegroundColor Green
if ($dashboardCorriendo) {
    Write-Host "El Dashboard se cerro para poder publicar: abrelo de nuevo para probar la version nueva." -ForegroundColor Yellow
}
Write-Host "Siguiente paso: ejecutar install-monitor-task.ps1 como Administrador (si aun no esta instalado)." -ForegroundColor Green
