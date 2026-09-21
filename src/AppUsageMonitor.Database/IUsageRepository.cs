using AppUsageMonitor.Database.Models;

namespace AppUsageMonitor.Database;

public interface IUsageRepository
{
    void Initialize();

    /// <summary>Inserta una nueva sesion en estado "Abierto" y devuelve su Id.</summary>
    long InsertOpenSession(UsageRecord record);

    /// <summary>Cierra una sesion existente calculando la duracion.</summary>
    void CloseSession(long id, DateTime fin, long duracionSegundos);

    /// <summary>
    /// Actualiza la duracion de una sesion que sigue abierta, sin cerrarla,
    /// para que el tiempo activo acumulado se vea reflejado mientras el
    /// programa sigue en uso (no espera a que se cierre para guardarse).
    /// </summary>
    void UpdateProgress(long id, long duracionSegundosParcial);

    /// <summary>Sesiones actualmente abiertas (programas en ejecucion).</summary>
    List<UsageRecord> GetOpenSessions();

    /// <summary>
    /// Cierra a la fuerza cualquier sesion que haya quedado abierta de una
    /// ejecucion anterior (por ejemplo tras un reinicio o corte de energia)
    /// para que no quede "colgada" indefinidamente.
    /// </summary>
    int CloseDanglingSessions();

    List<UsageRecord> Query(UsageFilter filter);

    List<string> GetDistinctUsuarios();

    List<string> GetDistinctAplicaciones();
}
