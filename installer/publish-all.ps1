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

Publish-Project "src\AppUsageMonitor.MonitorService\AppUsageMonitor.MonitorService.csproj" "MonitorService"
Publish-Project "src\AppUsageMonitor.Dashboard\AppUsageMonitor.Dashboard.csproj" "Dashboard"

Write-Host ""
Write-Host "Publicacion completa en: $outDir" -ForegroundColor Green
Write-Host "Siguiente paso: ejecutar install-service.ps1 como Administrador." -ForegroundColor Green
