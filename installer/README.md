# Instalador - Monitor de Uso de Aplicaciones

Scripts de PowerShell para publicar, instalar y desinstalar el motor de
deteccion y el dashboard. Ejecutar siempre desde una consola de PowerShell
abierta en Windows 10/11.

## Por que una tarea programada y no un "servicio de Windows" clasico

La primera version de este proyecto instalaba el motor de deteccion como
servicio de Windows (Service Control Manager) corriendo como `LocalSystem`.
En pruebas reales se confirmo que **no funciona**: un servicio `LocalSystem`
corre en la Sesion 0 de Windows, aislada de las sesiones interactivas de
los usuarios desde Windows Vista (para evitar ataques de "shatter"). Un
proceso en la Sesion 0 no tiene acceso a las ventanas de los programas que
un usuario abre en su escritorio, asi que el servicio siempre detectaba
cero actividad aunque apareciera "Running" en `Get-Service`.

La solucion (la misma que usan herramientas reales de seguimiento de
tiempo) es registrar el motor de deteccion como **tarea programada que
corre dentro de la sesion de cada usuario**: arranca sola al iniciar
sesion, sin que el usuario abra nada, sin mostrar ninguna ventana, pero
con acceso normal a las ventanas de esa sesion.

## Orden de uso

1. **Publicar los binarios**

   ```powershell
   cd installer
   .\publish-all.ps1
   ```

   Esto genera `..\publish\MonitorService\` y `..\publish\Dashboard\` con los
   ejecutables listos para instalar (requiere tener el .NET 8 SDK instalado
   en la maquina donde se compila). Usa `-SelfContained` si el equipo destino
   no tiene el .NET 8 Runtime instalado:

   ```powershell
   .\publish-all.ps1 -SelfContained
   ```

2. **Instalar el motor de deteccion** (requiere consola "Ejecutar como administrador")

   ```powershell
   .\install-monitor-task.ps1
   ```

   Registra la tarea programada `AppUsageMonitorTask` (arranca al iniciar
   sesion cualquier usuario de este equipo, sin ventana visible), ajusta
   permisos de la carpeta de datos compartida y arranca el monitor de
   inmediato para la sesion actual (no hace falta cerrar sesion para
   probarlo).

3. **Configurar el Dashboard para que abra con Windows** (opcional, sin permisos de administrador)

   ```powershell
   .\install-dashboard-autostart.ps1
   ```

   Crea un acceso directo en la carpeta de Inicio del usuario actual que
   abre el dashboard minimizado a la bandeja del sistema.

## Desinstalar

```powershell
.\uninstall-monitor-task.ps1          # como administrador
.\uninstall-dashboard-autostart.ps1
```

La base de datos (`%ProgramData%\AppUsageMonitor\usage.db`) **no se borra**
al desinstalar, para conservar el historial. Bórrala manualmente si ya no la
necesitas.

Si instalaste una version anterior que uso `install-service.ps1` (servicio
de Windows clasico, ya retirado), quítala primero con:

```powershell
.\uninstall-service.ps1               # como administrador
```

## Verificar que el monitor esta corriendo

```powershell
Get-ScheduledTask -TaskName AppUsageMonitorTask
Get-Process -Name AppUsageMonitor.MonitorService -ErrorAction SilentlyContinue
```

Si `Get-Process` no devuelve nada, el proceso se cayo (se observo en la
practica que una desconexion de Escritorio Remoto puede matarlo sin dejar
error en el log). La tarea tiene un disparador "vigilante" que revisa cada
5 minutos y lo vuelve a levantar solo si no esta corriendo, asi que no hace
falta intervenir manualmente; si quieres forzar que arranque de inmediato
en vez de esperar el proximo ciclo:

```powershell
Start-ScheduledTask -TaskName AppUsageMonitorTask
```

## Ver el registro de diagnostico

El monitor corre sin consola y sin privilegios de administrador (tarea de
la sesion del usuario), asi que escribe su propio archivo de registro en
lugar del Visor de eventos de Windows:

```
%ProgramData%\AppUsageMonitor\logs\monitor.log
```
