using AppUsageMonitor.Database;
using AppUsageMonitor.MonitorService;

// Evita que se acumulen varias instancias detectando y guardando actividad
// por separado (por ejemplo si alguien abre el .exe a mano mientras la
// tarea programada ya tiene una corriendo). "Local\" limita el mutex a la
// sesion actual: en un equipo con varias sesiones (Escritorio Remoto) cada
// una puede tener la suya.
using var singleInstanceMutex = new Mutex(true, "Local\\AppUsageMonitor.MonitorService.SingleInstance", out var isNewInstance);
if (!isNewInstance)
{
    return;
}

var builder = Host.CreateApplicationBuilder(args);

builder.Services.AddSingleton<IUsageRepository>(_ =>
    new UsageRepository(DatabasePathProvider.GetDefaultPath()));

builder.Services.Configure<MonitoringOptions>(builder.Configuration.GetSection("Monitoring"));
builder.Services.AddSingleton<ProcessMonitor>();
builder.Services.AddHostedService<Worker>();

// Corre sin consola (OutputType=WinExe) y como tarea de la sesion del
// usuario, sin privilegios de administrador, asi que se reemplaza el logger
// de consola por defecto por uno a archivo (ver FileLoggerProvider).
var logPath = Path.Combine(DatabasePathProvider.GetDefaultDirectory(), "logs", "monitor.log");
builder.Logging.ClearProviders();
builder.Logging.AddProvider(new FileLoggerProvider(logPath));

var host = builder.Build();
host.Run();
