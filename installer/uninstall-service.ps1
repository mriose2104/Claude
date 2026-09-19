<#
.SYNOPSIS
    Detiene y elimina el servicio AppUsageMonitorService.
    No borra la base de datos ni el historial registrado.
.NOTES
    Ejecutar como Administrador.
#>
param(
    [string]$ServiceName = "AppUsageMonitorService"
)

$ErrorActionPreference = "Stop"

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Este script debe ejecutarse como Administrador."
    exit 1
}

$existing = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if (-not $existing) {
    Write-Host "El servicio '$ServiceName' no esta instalado." -ForegroundColor Yellow
    exit 0
}

Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue
sc.exe delete $ServiceName | Out-Null

Write-Host "Servicio '$ServiceName' eliminado. La base de datos en %ProgramData%\AppUsageMonitor se conserva." -ForegroundColor Green
