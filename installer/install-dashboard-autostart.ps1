<#
.SYNOPSIS
    Agrega un acceso directo en la carpeta de Inicio del usuario actual para
    que el Dashboard se abra minimizado a la bandeja del sistema al iniciar
    sesion en Windows.
.NOTES
    No requiere permisos de administrador (aplica solo al usuario actual).
#>
param(
    [string]$DashboardExePath
)

$ErrorActionPreference = "Stop"

if (-not $DashboardExePath) {
    $root = Split-Path -Parent $PSScriptRoot
    $DashboardExePath = Join-Path $root "publish\Dashboard\AppUsageMonitor.Dashboard.exe"
}

if (-not (Test-Path $DashboardExePath)) {
    Write-Error "No se encontro el ejecutable del dashboard en: $DashboardExePath`nEjecuta primero publish-all.ps1."
    exit 1
}

$startupFolder = [Environment]::GetFolderPath("Startup")
$shortcutPath = Join-Path $startupFolder "Monitor de Uso de Aplicaciones.lnk"

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $DashboardExePath
$shortcut.Arguments = "--minimized"
$shortcut.WorkingDirectory = Split-Path -Parent $DashboardExePath
$shortcut.Description = "Panel de Monitor de Uso de Aplicaciones"
$shortcut.Save()

Write-Host "Acceso directo creado en: $shortcutPath" -ForegroundColor Green
Write-Host "El dashboard se abrira minimizado a la bandeja en el proximo inicio de sesion." -ForegroundColor Green
