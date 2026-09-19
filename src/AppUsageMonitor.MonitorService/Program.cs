using AppUsageMonitor.Database;
using AppUsageMonitor.MonitorService;

var builder = Host.CreateApplicationBuilder(args);

builder.Services.AddWindowsService(options =>
{
    options.ServiceName = "AppUsageMonitorService";
});

builder.Services.AddSingleton<IUsageRepository>(_ =>
    new UsageRepository(DatabasePathProvider.GetDefaultPath()));

builder.Services.Configure<MonitoringOptions>(builder.Configuration.GetSection("Monitoring"));
builder.Services.AddSingleton<ProcessMonitor>();
builder.Services.AddHostedService<Worker>();

// El event log de Windows solo esta disponible corriendo como servicio;
// igual agregamos el logger de consola para depurar en modo interactivo.
builder.Logging.AddEventLog(settings => settings.SourceName = "AppUsageMonitorService");

var host = builder.Build();
host.Run();
