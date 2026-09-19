namespace AppUsageMonitor.Database.Models;

/// <summary>
/// Representa una fila de la tabla UsoAplicaciones: una sesion de uso
/// de una aplicacion, desde que se detecta abierta hasta que se cierra.
/// </summary>
public class UsageRecord
{
    public long Id { get; set; }

    public string Usuario { get; set; } = string.Empty;

    /// <summary>Nombre del proceso, por ejemplo "chrome.exe".</summary>
    public string Proceso { get; set; } = string.Empty;

    /// <summary>Nombre visible de la aplicacion, por ejemplo "Google Chrome".</summary>
    public string Aplicacion { get; set; } = string.Empty;

    /// <summary>Fecha en la que inicio la sesion (yyyy-MM-dd).</summary>
    public DateOnly Fecha { get; set; }

    public DateTime Inicio { get; set; }

    public DateTime? Fin { get; set; }

    public long DuracionSegundos { get; set; }

    /// <summary>"Abierto" o "Cerrado".</summary>
    public string Estado { get; set; } = "Abierto";

    public TimeSpan Duracion => TimeSpan.FromSeconds(DuracionSegundos);
}
