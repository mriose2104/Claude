namespace AppUsageMonitor.Dashboard;

/// <summary>Fila ya formateada para mostrar en el DataGrid de historial/reportes.</summary>
public class HistorialRow
{
    public string Usuario { get; set; } = string.Empty;
    public string Programa { get; set; } = string.Empty;
    public string Proceso { get; set; } = string.Empty;
    public string Fecha { get; set; } = string.Empty;
    public string Inicio { get; set; } = string.Empty;
    public string Fin { get; set; } = string.Empty;
    public string Duracion { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
}
