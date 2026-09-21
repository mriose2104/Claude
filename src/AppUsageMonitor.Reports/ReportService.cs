using AppUsageMonitor.Database;
using AppUsageMonitor.Database.Models;
using AppUsageMonitor.Reports.Models;

namespace AppUsageMonitor.Reports;

/// <summary>
/// Punto de entrada para consultas de reportes y dashboard. Envuelve el
/// repositorio con la logica de agregacion que necesita la interfaz
/// (totales, ranking de aplicaciones, resumen diario).
/// </summary>
public class ReportService
{
    private readonly IUsageRepository _repository;

    public ReportService(IUsageRepository repository)
    {
        _repository = repository;
    }

    public IReadOnlyList<UsageRecord> GetHistorial(UsageFilter filter) => _repository.Query(filter);

    public IReadOnlyList<UsageRecord> GetProgramasAbiertos() => _repository.GetOpenSessions();

    public IReadOnlyList<string> GetUsuarios() => _repository.GetDistinctUsuarios();

    public IReadOnlyList<string> GetAplicaciones() => _repository.GetDistinctAplicaciones();

    /// <summary>
    /// Duracion a mostrar: el monitor guarda tiempo activo (en primer
    /// plano), no tiempo abierto, y lo actualiza en la base de datos
    /// mientras la sesion sigue abierta, asi que el valor guardado ya es
    /// el correcto tanto para sesiones abiertas como cerradas.
    /// </summary>
    public static long DuracionEfectivaSegundos(UsageRecord record) => record.DuracionSegundos;

    public long GetTotalUsoSegundos(UsageFilter filter) =>
        _repository.Query(filter).Sum(DuracionEfectivaSegundos);

    public IReadOnlyList<AppUsageSummary> GetTopAplicaciones(UsageFilter filter, int top = 10)
    {
        return _repository.Query(filter)
            .GroupBy(r => r.Aplicacion)
            .Select(g => new AppUsageSummary
            {
                Aplicacion = g.Key,
                TotalSegundos = g.Sum(DuracionEfectivaSegundos),
                Sesiones = g.Count(),
            })
            .OrderByDescending(a => a.TotalSegundos)
            .Take(top)
            .ToList();
    }

    public DailySummary GetResumenDiario(DateOnly fecha, string? usuario = null)
    {
        var filter = new UsageFilter { FechaDesde = fecha, FechaHasta = fecha, Usuario = usuario };
        var registros = _repository.Query(filter);

        var porAplicacion = registros
            .GroupBy(r => r.Aplicacion)
            .Select(g => new AppUsageSummary
            {
                Aplicacion = g.Key,
                TotalSegundos = g.Sum(DuracionEfectivaSegundos),
                Sesiones = g.Count(),
            })
            .OrderByDescending(a => a.TotalSegundos)
            .ToList();

        var ultimaActividad = registros
            .Select(r => r.Fin ?? r.Inicio)
            .DefaultIfEmpty()
            .Max();

        return new DailySummary
        {
            Fecha = fecha,
            TotalSegundosUso = registros.Sum(DuracionEfectivaSegundos),
            AplicacionesDistintas = porAplicacion.Count,
            SesionesTotales = registros.Count,
            UltimaActividad = ultimaActividad == default ? null : ultimaActividad,
            PorAplicacion = porAplicacion,
        };
    }
}
