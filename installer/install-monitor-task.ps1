<#
.SYNOPSIS
    Registra el motor de deteccion (AppUsageMonitor.MonitorService.exe) como
    una tarea programada que arranca en segundo plano, sin ventana visible,
    cada vez que un usuario inicia sesion en Windows.

.NOTES
    Ejecutar como Administrador.

    Por que una tarea programada y NO un "servicio de Windows" clasico:
    un servicio que corre como LocalSystem se ejecuta en la Sesion 0 de
    Windows, que esta aislada de las sesiones interactivas de los usuarios
    por diseno de seguridad (desde Windows Vista, para evitar ataques de
    "shatter"). Un proceso en la Sesion 0 NO puede ver las ventanas de las
    aplicaciones que un usuario abre en su escritorio -- por eso un
    servicio clasico jamas lograria detectar que programas tiene abiertos
    (se probo en la practica: el servicio quedaba "Running" pero el
    dashboard siempre mostraba cero actividad).

    La solucion, igual a la que usan herramientas de seguimiento de tiempo
    reales, es registrar el motor de deteccion como tarea programada que
    corre DENTRO de la sesion de cada usuario: arranca solo, sin que el
    usuario abra nada, sin mostrar ninguna ventana (el ejecutable se
    compila con OutputType=WinExe), pero conservando acceso a las ventanas
    de esa sesion.
#>
param(
    [string]$TaskName = "AppUsageMonitorTask",
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
    Write-Error "No se encontro el ejecutable del monitor en: $ExePath`nEjecuta primero publish-all.ps1."
    exit 1
}

$existing = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "La tarea '$TaskName' ya existe. Se reemplaza..." -ForegroundColor Yellow
    Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
}

$action = New-ScheduledTaskAction -Execute $ExePath

# Dos disparadores: al iniciar sesion (arranque inmediato) y uno que se
# repite cada 5 minutos como "vigilante". Se probo en la practica que el
# proceso puede morir sin dejar rastro (por ejemplo al desconectarse una
# sesion de Escritorio Remoto), sin que Windows lo reporte como una falla
# de la tarea. El disparador repetitivo, combinado con
# MultipleInstances=IgnoreNew, hace que Task Scheduler lo vuelva a lanzar
# solo si nota que ya no esta corriendo, sin crear copias duplicadas si
# sigue vivo.
$triggerLogon = New-ScheduledTaskTrigger -AtLogOn
$triggerWatchdog = New-ScheduledTaskTrigger -Once -At (Get-Date) `
    -RepetitionInterval (New-TimeSpan -Minutes 5) `
    -RepetitionDuration (New-TimeSpan -Days 3650)

# Grupo "Usuarios" (no un usuario especifico): la tarea arranca para
# cualquiera que inicie sesion en este equipo, sin privilegios elevados.
$principal = New-ScheduledTaskPrincipal -GroupId "BUILTIN\Users" -RunLevel Limited
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries `
    -StartWhenAvailable -ExecutionTimeLimit ([TimeSpan]::Zero) -Hidden `
    -MultipleInstances IgnoreNew `
    -RestartCount 999 -RestartInterval (New-TimeSpan -Minutes 1)

Register-ScheduledTask -TaskName $TaskName `
    -Action $action -Trigger @($triggerLogon, $triggerWatchdog) -Principal $principal -Settings $settings `
    -Description "Monitor de Uso de Aplicaciones: registra localmente que programas se usan en esta sesion. No registra teclas ni contenido de pantalla." | Out-Null

# Carpeta de datos compartida con el dashboard: permitir lectura/escritura a
# los usuarios locales (en un equipo con varios usuarios, todos comparten la
# misma base de datos).
$dataDir = Join-Path $env:ProgramData "AppUsageMonitor"
New-Item -ItemType Directory -Path $dataDir -Force | Out-Null
try {
    # SID conocido del grupo "Usuarios" (S-1-5-32-545) en vez del nombre de
    # cuenta como texto, que puede fallar al traducirse segun el idioma.
    $usersSid = New-Object System.Security.Principal.SecurityIdentifier(
        [System.Security.Principal.WellKnownSidType]::BuiltinUsersSid, $null)
    $acl = Get-Acl $dataDir
    $rule = New-Object System.Security.AccessControl.FileSystemAccessRule(
        $usersSid, "Modify", "ContainerInherit,ObjectInherit", "None", "Allow")
    $acl.AddAccessRule($rule)
    Set-Acl -Path $dataDir -AclObject $acl
} catch {
    Write-Warning "No se pudieron ajustar permisos de $dataDir : $($_.Exception.Message)"
}

# Arranca ya mismo para la sesion actual, sin esperar al proximo inicio de sesion.
Start-ScheduledTask -TaskName $TaskName

Write-Host ""
Write-Host "Tarea '$TaskName' registrada e iniciada." -ForegroundColor Green
Write-Host "Arrancara automaticamente cada vez que un usuario inicie sesion en este equipo." -ForegroundColor Green
Write-Host "Registro de diagnostico: $dataDir\logs\monitor.log" -ForegroundColor Green
