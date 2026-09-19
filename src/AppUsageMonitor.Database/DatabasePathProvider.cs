namespace AppUsageMonitor.Database;

/// <summary>
/// Resuelve la ubicacion de la base de datos local compartida entre el
/// servicio (MonitorService) y el panel (Dashboard). Se usa una carpeta
/// de ProgramData para que ambos procesos, aunque corran con usuarios
/// distintos (LocalSystem para el servicio, el usuario logueado para el
/// dashboard), puedan acceder al mismo archivo.
/// </summary>
public static class DatabasePathProvider
{
    public const string FolderName = "AppUsageMonitor";
    public const string FileName = "usage.db";

    public static string GetDefaultDirectory()
    {
        var baseDir = Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData);
        return Path.Combine(baseDir, FolderName);
    }

    public static string GetDefaultPath() => Path.Combine(GetDefaultDirectory(), FileName);

    public static string BuildConnectionString(string dbPath, bool readOnly = false)
    {
        var mode = readOnly ? "ReadOnly" : "ReadWriteCreate";
        return $"Data Source={dbPath};Mode={mode};Cache=Shared";
    }
}
