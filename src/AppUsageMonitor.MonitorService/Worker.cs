using AppUsageMonitor.Database;
using Microsoft.Extensions.Options;

namespace AppUsageMonitor.MonitorService;

public class Worker : BackgroundService
{
    private readonly IUsageRepository _repository;
    private readonly ProcessMonitor _monitor;
    private readonly ILogger<Worker> _logger;
    private readonly MonitoringOptions _options;

    public Worker(
        IUsageRepository repository,
        ProcessMonitor monitor,
        ILogger<Worker> logger,
        IOptions<MonitoringOptions> options)
    {
        _repository = repository;
        _monitor = monitor;
        _logger = logger;
        _options = options.Value;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _repository.Initialize();

        var cerradas = _repository.CloseDanglingSessions();
        if (cerradas > 0)
        {
            _logger.LogWarning("Se cerraron {Cantidad} sesiones que quedaron abiertas de una ejecucion anterior.", cerradas);
        }

        _logger.LogInformation("Monitor de Uso de Aplicaciones iniciado. Intervalo de sondeo: {Segundos}s", _options.PollIntervalSeconds);

        var interval = TimeSpan.FromSeconds(Math.Max(1, _options.PollIntervalSeconds));

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                _monitor.Poll();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error durante el sondeo de procesos.");
            }

            try
            {
                await Task.Delay(interval, stoppingToken);
            }
            catch (TaskCanceledException)
            {
                break;
            }
        }

        _logger.LogInformation("Deteniendo servicio, cerrando sesiones activas...");
        _monitor.CloseAllActiveSessions();
    }
}
