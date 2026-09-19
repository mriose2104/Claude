<#
.SYNOPSIS
    Detiene y elimina la tarea programada del monitor. No borra la base de
    datos ni el historial registrado.
.NOTES
    Ejecutar como Administrador.
#>
param(
    [string]$TaskName = "AppUsageMonitorTask"
)

$ErrorActionPreference = "Stop"

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Este script debe ejecutarse como Administrador."
    exit 1
}

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if (-not $existing) {
    Write-Host "La tarea '$TaskName' no esta instalada." -ForegroundColor Yellow
} else {
    Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "Tarea '$TaskName' eliminada." -ForegroundColor Green
}

# Tambien detiene cualquier instancia que ya este corriendo en sesiones activas.
Get-Process -Name "AppUsageMonitor.MonitorService" -ErrorAction SilentlyContinue |
    Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "La base de datos en %ProgramData%\AppUsageMonitor se conserva." -ForegroundColor Green
