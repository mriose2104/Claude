using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Logging;

namespace AppUsageMonitor.MonitorService;

/// <summary>
/// Consulta el historial local de Chrome/Edge (el archivo "History" que ya
/// guardan en el disco de este equipo) solo para saber el DOMINIO de la
/// pagina visitada mas recientemente, y compararlo contra una lista de
/// sitios a rastrear (appsettings.json, clave Monitoring:SitiosWeb).
///
/// Nunca se guarda ni se expone la URL completa, el titulo de la pagina, ni
/// el historial de navegacion en si: solo se usa el nombre de dominio para
/// decidir con que etiqueta registrar el tiempo (ej. "YouTube"), y solo
/// para los dominios que el usuario configuro explicitamente.
///
/// Limitacion conocida: el historial solo se actualiza al navegar a una
/// pagina, no al cambiar entre pestanas ya cargadas, asi que con varias
/// pestanas abiertas puede quedarse con el dominio de la ultima que se
/// cargo de verdad en vez de la que esta visible en este momento.
/// </summary>
public class BrowserHistoryReader
{
    private readonly ILogger _logger;

    private static readonly Dictionary<string, string> HistoryPathPorProceso = new(StringComparer.OrdinalIgnoreCase)
    {
        ["chrome"] = @"Google\Chrome\User Data\Default\History",
        ["msedge"] = @"Microsoft\Edge\User Data\Default\History",
    };

    public BrowserHistoryReader(ILogger logger)
    {
        _logger = logger;
    }

    /// <summary>
    /// Devuelve el nombre configurado para el dominio visitado mas
    /// recientemente en ese navegador, o null si no aplica (no es un
    /// navegador reconocido, no se pudo leer el historial, o el dominio no
    /// esta en la lista de sitios a rastrear).
    /// </summary>
    public string? GetSitioActivo(string processName, IReadOnlyDictionary<string, string> sitiosWeb)
    {
        if (sitiosWeb.Count == 0) return null;
        if (!HistoryPathPorProceso.TryGetValue(processName, out var relativePath)) return null;

        var localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        var historyPath = Path.Combine(localAppData, relativePath);
        if (!File.Exists(historyPath)) return null;

        var tempCopy = Path.Combine(Path.GetTempPath(), $"appusagemonitor_history_{Guid.NewGuid():N}.db");
        try
        {
            // El navegador tiene el archivo abierto; se copia para leerlo
            // sin pelear por el lock, y se borra la copia enseguida.
            File.Copy(historyPath, tempCopy, overwrite: true);

            using var connection = new SqliteConnection($"Data Source={tempCopy};Mode=ReadOnly");
            connection.Open();

            using var command = connection.CreateCommand();
            command.CommandText = "SELECT url FROM urls ORDER BY last_visit_time DESC LIMIT 1;";
            var url = command.ExecuteScalar() as string;
            if (string.IsNullOrWhiteSpace(url)) return null;

            var host = new Uri(url).Host;
            if (host.StartsWith("www.", StringComparison.OrdinalIgnoreCase))
            {
                host = host["www.".Length..];
            }

            foreach (var (dominio, etiqueta) in sitiosWeb)
            {
                if (host.Equals(dominio, StringComparison.OrdinalIgnoreCase) ||
                    host.EndsWith("." + dominio, StringComparison.OrdinalIgnoreCase))
                {
                    return etiqueta;
                }
            }

            return null;
        }
        catch (Exception ex)
        {
            _logger.LogDebug(ex, "No se pudo leer el historial de {Proceso}", processName);
            return null;
        }
        finally
        {
            try { File.Delete(tempCopy); } catch { /* nada que hacer si no se pudo borrar la copia temporal */ }
        }
    }
}
