# Instalador - Monitor de Uso de Aplicaciones

Scripts de PowerShell para publicar, instalar y desinstalar el servicio y el
dashboard. Ejecutar siempre desde una consola de PowerShell abierta en
Windows 10/11.

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

2. **Instalar el servicio de Windows** (requiere consola "Ejecutar como administrador")

   ```powershell
   .\install-service.ps1
   ```

   Crea el servicio `AppUsageMonitorService`, lo configura en inicio
   automatico, lo arranca y ajusta permisos de la carpeta de datos
   (`%ProgramData%\AppUsageMonitor`) para que el dashboard pueda leerla.

3. **Configurar el Dashboard para que abra con Windows** (opcional, sin permisos de administrador)

   ```powershell
   .\install-dashboard-autostart.ps1
   ```

   Crea un acceso directo en la carpeta de Inicio del usuario actual que
   abre el dashboard minimizado a la bandeja del sistema.

## Desinstalar

```powershell
.\uninstall-service.ps1              # como administrador
.\uninstall-dashboard-autostart.ps1  # usuario normal
```

La base de datos (`%ProgramData%\AppUsageMonitor\usage.db`) **no se borra**
al desinstalar, para conservar el historial. Bórrala manualmente si ya no la
necesitas.

## Verificar que el servicio esta corriendo

```powershell
Get-Service AppUsageMonitorService
```

## Ver el registro de eventos del servicio

El servicio escribe en el Visor de eventos de Windows, origen
`AppUsageMonitorService` (Registro de aplicacion), ademas de la consola
cuando se ejecuta en modo interactivo (`dotnet run` dentro del proyecto
`AppUsageMonitor.MonitorService`).
