using AppUsageMonitor.Database;
using AppUsageMonitor.MonitorService;

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
