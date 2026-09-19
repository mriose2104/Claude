<#
.SYNOPSIS
    Instala y arranca AppUsageMonitorService como servicio de Windows,
    configurado para iniciar automaticamente con el sistema.

.NOTES
    Debe ejecutarse en una consola de PowerShell "Ejecutar como administrador".
    Requiere haber corrido antes publish-all.ps1 (o publicar manualmente el
    proyecto MonitorService).
#>
param(
    [string]$ServiceName = "AppUsageMonitorService",
    [string]$DisplayName = "Monitor de Uso de Aplicaciones",
    [string]$ExePath
)

$ErrorActionPreference = "Stop"

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Este script debe ejecutarse como Administrador."
    exit 1
}

if (-not $ExePath) {
    $root = Split-Path -Parent $PSScriptRoot
    $ExePath = Join-Path $root "publish\MonitorService\AppUsageMonitor.MonitorService.exe"
}

if (-not (Test-Path $ExePath)) {
    Write-Error "No se encontro el ejecutable del servicio en: $ExePath`nEjecuta primero publish-all.ps1."
    exit 1
}

$existing = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "El servicio '$ServiceName' ya existe. Deteniendolo para reinstalar..." -ForegroundColor Yellow
    Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue
    sc.exe delete $ServiceName | Out-Null
    Start-Sleep -Seconds 2
}

New-Service -Name $ServiceName `
    -BinaryPathName "`"$ExePath`"" `
    -DisplayName $DisplayName `
    -Description "Registra localmente el uso de aplicaciones en este equipo (procesos, usuario, horarios). No registra teclas ni contenido de pantalla." `
    -StartupType Automatic | Out-Null

# Reinicio automatico ante fallos inesperados del servicio.
sc.exe failure $ServiceName reset= 86400 actions= restart/5000/restart/5000/restart/5000 | Out-Null

# Carpeta de datos compartida con el dashboard: permitir lectura/escritura
# a los usuarios locales para que el dashboard (que corre con el usuario
# logueado) pueda leer la base de datos que escribe el servicio (LocalSystem).
$dataDir = Join-Path $env:ProgramData "AppUsageMonitor"
New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
try {
    # Se usa el SID conocido del grupo "Usuarios" (S-1-5-32-545) en vez del
    # nombre de cuenta como texto: el nombre localizado de ese grupo varia
    # segun el idioma de Windows y puede fallar al traducirse.
    $usersSid = New-Object System.Security.Principal.SecurityIdentifier(
        [System.Security.Principal.WellKnownSidType]::BuiltinUsersSid, $null)
    $acl = Get-Acl $dataDir
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule(
        $usersSid, "Modify", "ContainerInherit,ObjectInherit", "None", "Allow")
    $acl.AddAccessRule($rule)
    Set-Acl -Path $dataDir -AclObject $acl
} catch {
    Write-Warning "No se pudieron ajustar permisos de $dataDir. Puede que el dashboard no logre leer la base de datos: $($_.Exception.Message)"
}

Start-Service -Name $ServiceName
Write-Host ""
Write-Host "Servicio '$DisplayName' instalado e iniciado correctamente." -ForegroundColor Green
Write-Host "Se iniciara automaticamente con Windows." -ForegroundColor Green
