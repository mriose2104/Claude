using Microsoft.Data.Sqlite;

namespace AppUsageMonitor.Database;

/// <summary>
/// Crea el archivo de base de datos y la tabla UsoAplicaciones si no existen.
/// Es seguro llamarlo en cada arranque del servicio o del dashboard.
/// </summary>
public static class DatabaseInitializer
{
    public static void EnsureCreated(string dbPath)
    {
        var directory = Path.GetDirectoryName(dbPath);
        if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
        {
            Directory.CreateDirectory(directory);
        }

        using var connection = new SqliteConnection(DatabasePathProvider.BuildConnectionString(dbPath));
        connection.Open();

        using var command = connection.CreateCommand();
        command.CommandText = """
            CREATE TABLE IF NOT EXISTS UsoAplicaciones (
                Id                INTEGER PRIMARY KEY AUTOINCREMENT,
                Usuario           TEXT NOT NULL,
                Proceso           TEXT NOT NULL,
                Aplicacion        TEXT NOT NULL,
                Fecha             TEXT NOT NULL,
                HoraInicio        TEXT NOT NULL,
                HoraFin           TEXT NULL,
                DuracionSegundos  INTEGER NOT NULL DEFAULT 0,
                Estado            TEXT NOT NULL DEFAULT 'Abierto'
            );

            CREATE INDEX IF NOT EXISTS IX_UsoAplicaciones_Fecha ON UsoAplicaciones(Fecha);
            CREATE INDEX IF NOT EXISTS IX_UsoAplicaciones_Usuario ON UsoAplicaciones(Usuario);
            CREATE INDEX IF NOT EXISTS IX_UsoAplicaciones_Aplicacion ON UsoAplicaciones(Aplicacion);
            CREATE INDEX IF NOT EXISTS IX_UsoAplicaciones_Estado ON UsoAplicaciones(Estado);
            """;
        command.ExecuteNonQuery();
    }
}
