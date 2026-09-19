namespace AppUsageMonitor.Reports.Models;

/// <summary>Tiempo total acumulado de una aplicacion dentro de un periodo consultado.</summary>
public class AppUsageSummary
{
    public string Aplicacion { get; set; } = string.Empty;

    public long TotalSegundos { get; set; }

    public int Sesiones { get; set; }

    public TimeSpan Duracion => TimeSpan.FromSeconds(TotalSegundos);
}
