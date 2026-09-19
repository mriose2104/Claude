namespace AppUsageMonitor.Reports.Models;

/// <summary>Resumen de actividad de un dia especifico, usado en el panel principal.</summary>
public class DailySummary
{
    public DateOnly Fecha { get; set; }

    public long TotalSegundosUso { get; set; }

    public int AplicacionesDistintas { get; set; }

    public int SesionesTotales { get; set; }

    public DateTime? UltimaActividad { get; set; }

    public List<AppUsageSummary> PorAplicacion { get; set; } = new();

    public TimeSpan DuracionTotal => TimeSpan.FromSeconds(TotalSegundosUso);
}
