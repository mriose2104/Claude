namespace AppUsageMonitor.MonitorService;

public class MonitoringOptions
{
    public int PollIntervalSeconds { get; set; } = 5;

    public string[] ExcludedProcessNames { get; set; } = Array.Empty<string>();

    /// <summary>
    /// Sitios web a rastrear por separado cuando Chrome o Edge estan en
    /// primer plano: clave = dominio (ej. "youtube.com"), valor = nombre
    /// a mostrar (ej. "YouTube"). Un dominio que no esta aqui no se separa,
    /// sigue contando solo como tiempo de "Google Chrome"/"Microsoft Edge".
    /// </summary>
    public Dictionary<string, string> SitiosWeb { get; set; } = new();
}
