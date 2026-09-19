namespace AppUsageMonitor.Dashboard.Controls;

/// <summary>Fila lista para dibujar en BarChartControl: ya trae el ancho de barra calculado.</summary>
public class BarChartItem
{
    public string Etiqueta { get; set; } = string.Empty;

    public long Segundos { get; set; }

    public string DuracionTexto { get; set; } = string.Empty;

    /// <summary>Ancho de la barra en pixeles, ya escalado contra el maximo del conjunto.</summary>
    public double AnchoBarra { get; set; }
}
