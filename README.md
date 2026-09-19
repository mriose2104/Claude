# Monitor de Uso de Aplicaciones

Aplicacion para Windows 10/11 que registra localmente que programas se
ejecutan en el equipo, quien los usa, cuando se abrieron/cerraron y cuanto
tiempo estuvieron activos. Incluye un servicio de Windows que corre en
segundo plano y un panel (dashboard) con reportes y exportacion.

## Que registra y que NO registra

**Si registra:** usuario de Windows, nombre del proceso y de la aplicacion,
fecha/hora de inicio y fin, duracion, y si esta abierta o cerrada.

**No registra:** contrasenas, pulsaciones de teclado (no es un keylogger),
contenido de ventanas ni capturas de pantalla. Toda la informacion se guarda
en una base de datos **local** (SQLite); nada se envia a internet. Esto se
muestra tambien dentro del dashboard, en la pestana "Que se monitorea".

## Arquitectura / estructura del proyecto

```
AppUsageMonitor.sln
src/
  AppUsageMonitor.Database/       Esquema SQLite (tabla UsoAplicaciones) y repositorio de datos
  AppUsageMonitor.MonitorService/ Servicio de Windows: detecta apertura/cierre de programas
  AppUsageMonitor.Reports/        Consultas agregadas (totales, ranking) y exportacion CSV/Excel
  AppUsageMonitor.Dashboard/      Interfaz WPF: panel principal, historial, reportes, icono en bandeja
installer/
  publish-all.ps1                 Publica los 4 binarios listos para instalar
  install-service.ps1             Instala y arranca el servicio de Windows (auto-inicio)
  uninstall-service.ps1           Detiene y elimina el servicio
  install-dashboard-autostart.ps1 Hace que el dashboard abra minimizado con Windows
  uninstall-dashboard-autostart.ps1
```

### Como se evitan duplicados (minimizar, cambiar de ventana, cerrar, reiniciar)

`ProcessMonitor` (en `AppUsageMonitor.MonitorService`) sondea la lista de
procesos cada pocos segundos y solo considera procesos con una ventana
principal visible (aplicaciones de escritorio reales, no procesos de
sistema en segundo plano). Se guarda **una sola fila por sesion**:

- Se crea una fila nueva unicamente cuando aparece un PID que no se conocia
  (apertura real del programa).
- Minimizar, cambiar de foco entre ventanas o solapar aplicaciones **no**
  modifica la lista de procesos en ejecucion, asi que no genera filas
  nuevas ni duplicados.
- La fila se cierra (Estado = "Cerrado", se calcula la duracion) cuando el
  PID desaparece de la lista, es decir, cuando el proceso realmente termina.
- Si Windows se reinicia o el servicio se detiene de forma abrupta, al
  volver a iniciar se cierran automaticamente las sesiones que hayan
  quedado "Abierto" de la ejecucion anterior (`CloseDanglingSessions`),
  para que no queden colgadas indefinidamente.

## Base de datos

SQLite en `%ProgramData%\AppUsageMonitor\usage.db`, compartida entre el
servicio (que escribe) y el dashboard (que lee). Tabla `UsoAplicaciones`:

| Columna          | Tipo    | Descripcion                          |
|------------------|---------|---------------------------------------|
| Id               | INTEGER | Clave primaria autoincremental        |
| Usuario          | TEXT    | Usuario de Windows                    |
| Proceso          | TEXT    | Nombre del proceso (`chrome.exe`)     |
| Aplicacion       | TEXT    | Nombre visible de la aplicacion       |
| Fecha            | TEXT    | Fecha de inicio de la sesion          |
| HoraInicio       | TEXT    | Fecha y hora de inicio                |
| HoraFin          | TEXT    | Fecha y hora de fin (NULL si abierta) |
| DuracionSegundos | INTEGER | Duracion calculada en segundos        |
| Estado           | TEXT    | `Abierto` / `Cerrado`                 |

## Requisitos para compilar

- Windows 10/11 (el servicio y el dashboard usan APIs de Windows: WPF,
  Windows Services, WinForms para el icono de bandeja).
- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0).
- Visual Studio 2022 (17.8+) o `dotnet` CLI.

> Nota: este proyecto fue generado y verificado revisando el codigo fuente
> en un entorno Linux sin .NET SDK disponible, por lo que **no fue posible
> compilarlo en este entorno**. Antes de instalarlo, compilalo en una
> maquina Windows con el SDK (`dotnet build AppUsageMonitor.sln`) y corrige
> cualquier detalle menor que el compilador senale.

## Compilar

```powershell
dotnet restore AppUsageMonitor.sln
dotnet build AppUsageMonitor.sln -c Release
```

O abrir `AppUsageMonitor.sln` en Visual Studio y compilar la solucion.

## Publicar e instalar (equipo destino)

Ver instrucciones detalladas en [`installer/README.md`](installer/README.md).
Resumen:

```powershell
cd installer
.\publish-all.ps1                      # genera los ejecutables
.\install-service.ps1                  # como Administrador: instala el servicio (auto-inicio)
.\install-dashboard-autostart.ps1      # opcional: dashboard minimizado al iniciar sesion
```

Para ejecutar el dashboard manualmente sin instalar el auto-inicio, basta con
abrir `publish\Dashboard\AppUsageMonitor.Dashboard.exe`.

## Desinstalar

```powershell
cd installer
.\uninstall-service.ps1                # como Administrador
.\uninstall-dashboard-autostart.ps1
```

El historial (`usage.db`) no se borra al desinstalar.

## Uso del Dashboard

- **Panel principal**: tiempo total de uso hoy, aplicaciones usadas hoy,
  programas abiertos ahora mismo, sesiones del dia, ultima actividad, y un
  grafico de barras con las aplicaciones mas usadas del dia.
- **Historial y reportes**: filtros rapidos (Hoy / Ayer / Esta semana /
  Este mes), rango de fechas personalizado, usuario especifico y programa
  especifico; tabla de resultados (Usuario, Programa, Inicio, Fin,
  Duracion, Estado) y exportacion a CSV o Excel.
- **Que se monitorea**: declaracion clara de que datos se registran y
  cuales explicitamente no.
- El dashboard se minimiza a la bandeja del sistema en lugar de cerrarse; el
  icono de la bandeja permite reabrir el panel o salir por completo.

## Limitaciones conocidas

- Para detectar el usuario propietario de procesos de **otras** sesiones de
  usuario en un equipo multiusuario, el servicio necesita privilegios
  elevados (corre como `LocalSystem` por defecto, que normalmente alcanza).
- El sondeo (cada 5 segundos por defecto, configurable en
  `appsettings.json` del servicio, clave `Monitoring:PollIntervalSeconds`)
  implica que una apertura/cierre muy breve (menor al intervalo) podria no
  registrarse; bajar el intervalo aumenta precision a costa de mas uso de
  CPU.
- Requiere el .NET 8 Desktop Runtime en el equipo destino si se publica sin
  `-SelfContained`.
