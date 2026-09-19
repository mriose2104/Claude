<#
.SYNOPSIS
    LIMPIEZA DE UNA INSTALACION ANTERIOR: detiene y elimina el servicio de
    Windows (SCM) "AppUsageMonitorService" que las primeras versiones de
    este proyecto instalaban con install-service.ps1 (ya retirado).

    Ese enfoque quedo descartado: un servicio LocalSystem corre en la
    Sesion 0 de Windows, aislada de las sesiones interactivas, por lo que
    nunca podia ver las ventanas de los programas de los usuarios (el
    dashboard siempre mostraba cero actividad). El reemplazo es
    install-monitor-task.ps1 / uninstall-monitor-task.ps1 (tarea programada
    por sesion). Usa este script solo si instalaste esa version vieja y
    necesitas quitarla antes de instalar la nueva.

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
