namespace AppUsageMonitor.Database.Models;

/// <summary>
/// Criterios de busqueda para consultar el historial de uso.
/// Cualquier campo nulo/vacio se ignora en la consulta.
/// </summary>
public class UsageFilter
{
    public DateOnly? FechaDesde { get; set; }

    public DateOnly? FechaHasta { get; set; }

    public string? Usuario { get; set; }

    /// <summary>Filtra por nombre de aplicacion (coincidencia parcial).</summary>
    public string? Aplicacion { get; set; }

    public static UsageFilter Hoy()
    {
        var hoy = DateOnly.FromDateTime(DateTime.Now);
        return new UsageFilter { FechaDesde = hoy, FechaHasta = hoy };
    }

    public static UsageFilter Ayer()
    {
        var ayer = DateOnly.FromDateTime(DateTime.Now.AddDays(-1));
        return new UsageFilter { FechaDesde = ayer, FechaHasta = ayer };
    }

    public static UsageFilter EstaSemana()
    {
        var hoy = DateTime.Now;
        int diff = (7 + (hoy.DayOfWeek - DayOfWeek.Monday)) % 7;
        var lunes = hoy.AddDays(-diff).Date;
        return new UsageFilter
        {
            FechaDesde = DateOnly.FromDateTime(lunes),
            FechaHasta = DateOnly.FromDateTime(hoy)
        };
    }

    public static UsageFilter EsteMes()
    {
        var hoy = DateTime.Now;
        var inicioMes = new DateTime(hoy.Year, hoy.Month, 1);
        return new UsageFilter
        {
            FechaDesde = DateOnly.FromDateTime(inicioMes),
            FechaHasta = DateOnly.FromDateTime(hoy)
        };
    }
}
