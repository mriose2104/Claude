using System.Diagnostics;
using System.Runtime.Versioning;
using AppUsageMonitor.Database;
using AppUsageMonitor.Database.Models;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace AppUsageMonitor.MonitorService;

/// <summary>
/// Detecta apertura y cierre de aplicaciones con ventana visible mediante
/// sondeo periodico de la lista de procesos. Solo se registran procesos
/// con una ventana principal (aplicaciones de escritorio reales), lo que
/// evita duplicados al minimizar, cambiar de ventana o simplemente perder
/// el foco: esas acciones no alteran la lista de procesos en ejecucion,
/// por lo que no generan nuevas filas. Una sesion nueva solo se crea
/// cuando aparece un PID que no se conocia, y se cierra cuando ese PID
/// desaparece de la lista.
///
/// No se inspecciona el titulo real de la ventana mas alla de comprobar
/// que existe (para filtrar procesos sin interfaz), no se usan hooks de
/// teclado ni se captura contenido de pantalla.
/// </summary>
[SupportedOSPlatform("windows")]
public class ProcessMonitor
{
    private readonly IUsageRepository _repository;
    private readonly ILogger<ProcessMonitor> _logger;
    private readonly MonitoringOptions _options;
    private readonly Dictionary<int, ActiveSession> _active = new();

    public ProcessMonitor(IUsageRepository repository, ILogger<ProcessMonitor> logger, IOptions<MonitoringOptions> options)
    {
        _repository = repository;
        _logger = logger;
        _options = options.Value;
    }

    private sealed class ActiveSession
    {
        public required long RecordId { get; init; }
        public required string AppName { get; init; }
        public required DateTime StartedAt { get; init; }
    }

    public void Poll()
    {
        var seenPids = new HashSet<int>();

        foreach (var process in Process.GetProcesses())
        {
            using (process)
            {
                int pid;
                try
                {
                    pid = process.Id;
                    if (pid == 0) continue;
                    if (IsExcluded(process.ProcessName)) continue;
                    if (process.MainWindowHandle == IntPtr.Zero) continue;
                    if (string.IsNullOrWhiteSpace(process.MainWindowTitle)) continue;
                }
                catch (Exception ex)
                {
                    _logger.LogDebug(ex, "No se pudo inspeccionar un proceso, se omite.");
                    continue;
                }

                seenPids.Add(pid);

                if (!_active.ContainsKey(pid))
                {
                    RegisterStart(process);
                }
            }
        }

        var closedPids = _active.Keys.Where(pid => !seenPids.Contains(pid)).ToList();
        foreach (var pid in closedPids)
        {
            RegisterStop(pid);
        }
    }

    private bool IsExcluded(string processName) =>
        _options.ExcludedProcessNames.Any(excluded => string.Equals(excluded, processName, StringComparison.OrdinalIgnoreCase));

    private void RegisterStart(Process process)
    {
        string appName;
        string processFileName;
        try
        {
            appName = GetFriendlyAppName(process);
            processFileName = process.ProcessName + ".exe";
        }
        catch (Exception ex)
        {
            _logger.LogDebug(ex, "No se pudo leer informacion del proceso {Pid}", process.Id);
            return;
        }

        var usuario = NativeMethods.GetProcessOwner(process.Id) ?? Environment.UserName;
        var now = DateTime.Now;

        var record = new UsageRecord
        {
            Usuario = usuario,
            Proceso = processFileName,
            Aplicacion = appName,
            Fecha = DateOnly.FromDateTime(now),
            Inicio = now,
            Estado = "Abierto",
        };

        long id;
        try
        {
            id = _repository.InsertOpenSession(record);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "No se pudo guardar el inicio de {App}", appName);
            return;
        }

        _active[process.Id] = new ActiveSession
        {
            RecordId = id,
            AppName = appName,
            StartedAt = now,
        };

        _logger.LogInformation("Inicio: {App} ({Proceso}) usuario {Usuario}", appName, processFileName, usuario);
    }

    private void RegisterStop(int pid)
    {
        if (!_active.TryGetValue(pid, out var session)) return;
        _active.Remove(pid);

        var fin = DateTime.Now;
        var duracion = Math.Max(0, (long)(fin - session.StartedAt).TotalSeconds);

        try
        {
            _repository.CloseSession(session.RecordId, fin, duracion);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "No se pudo guardar el cierre de {App}", session.AppName);
            return;
        }

        _logger.LogInformation("Cierre: {App} duracion {Duracion}", session.AppName, FormatMinutos(duracion));
    }

    private static string FormatMinutos(long segundos)
    {
        if (segundos < 60) return $"{segundos}s";
        var minutos = segundos / 60;
        var resto = segundos % 60;
        return resto == 0 ? $"{minutos}m" : $"{minutos}m {resto}s";
    }

    /// <summary>Se llama al detener el servicio para cerrar prolijamente todo lo que quedaba abierto.</summary>
    public void CloseAllActiveSessions()
    {
        foreach (var pid in _active.Keys.ToList())
        {
            RegisterStop(pid);
        }
    }

    private static string GetFriendlyAppName(Process process)
    {
        try
        {
            var description = process.MainModule?.FileVersionInfo.FileDescription;
            if (!string.IsNullOrWhiteSpace(description))
            {
                return description!;
            }
        }
        catch
        {
            // Acceso denegado a procesos de otros usuarios o protegidos del sistema.
        }

        return process.ProcessName;
    }
}
