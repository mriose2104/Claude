using AppUsageMonitor.Database.Models;
using Microsoft.Data.Sqlite;

namespace AppUsageMonitor.Database;

/// <summary>
/// Acceso a datos sobre SQLite para la tabla UsoAplicaciones.
/// Las fechas se guardan como texto ISO-8601 para poder ordenar/filtrar
/// con comparaciones de texto simples.
/// </summary>
public class UsageRepository : IUsageRepository
{
    private const string DateFormat = "yyyy-MM-dd";
    private const string DateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private readonly string _dbPath;

    public UsageRepository(string dbPath)
    {
        _dbPath = dbPath;
    }

    public void Initialize() => DatabaseInitializer.EnsureCreated(_dbPath);

    private SqliteConnection OpenConnection(bool readOnly = false)
    {
        var connection = new SqliteConnection(DatabasePathProvider.BuildConnectionString(_dbPath, readOnly));
        connection.Open();
        return connection;
    }

    public long InsertOpenSession(UsageRecord record)
    {
        using var connection = OpenConnection();
        using var command = connection.CreateCommand();
        command.CommandText = """
            INSERT INTO UsoAplicaciones (Usuario, Proceso, Aplicacion, Fecha, HoraInicio, HoraFin, DuracionSegundos, Estado)
            VALUES ($usuario, $proceso, $aplicacion, $fecha, $horaInicio, NULL, 0, 'Abierto');
            SELECT last_insert_rowid();
            """;
        command.Parameters.AddWithValue("$usuario", record.Usuario);
        command.Parameters.AddWithValue("$proceso", record.Proceso);
        command.Parameters.AddWithValue("$aplicacion", record.Aplicacion);
        command.Parameters.AddWithValue("$fecha", record.Fecha.ToString(DateFormat));
        command.Parameters.AddWithValue("$horaInicio", record.Inicio.ToString(DateTimeFormat));

        var result = command.ExecuteScalar();
        return Convert.ToInt64(result);
    }

    public void CloseSession(long id, DateTime fin, long duracionSegundos)
    {
        using var connection = OpenConnection();
        using var command = connection.CreateCommand();
        command.CommandText = """
            UPDATE UsoAplicaciones
            SET HoraFin = $fin, DuracionSegundos = $duracion, Estado = 'Cerrado'
            WHERE Id = $id;
            """;
        command.Parameters.AddWithValue("$fin", fin.ToString(DateTimeFormat));
        command.Parameters.AddWithValue("$duracion", duracionSegundos);
        command.Parameters.AddWithValue("$id", id);
        command.ExecuteNonQuery();
    }

    public List<UsageRecord> GetOpenSessions()
    {
        using var connection = OpenConnection(readOnly: true);
        using var command = connection.CreateCommand();
        command.CommandText = "SELECT * FROM UsoAplicaciones WHERE Estado = 'Abierto' ORDER BY HoraInicio DESC;";
        using var reader = command.ExecuteReader();
        return ReadAll(reader);
    }

    public int CloseDanglingSessions()
    {
        using var connection = OpenConnection();
        using var command = connection.CreateCommand();
        // Las sesiones que quedaron "Abierto" de una ejecucion anterior del
        // servicio (apagado inesperado, reinicio de Windows) se cierran usando
        // su propia hora de inicio como fin, ya que no conocemos el momento
        // real de cierre. Duracion 0 evita inflar estadisticas con tiempo
        // que el equipo pudo haber estado apagado.
        command.CommandText = """
            UPDATE UsoAplicaciones
            SET HoraFin = HoraInicio, DuracionSegundos = 0, Estado = 'Cerrado (interrumpido)'
            WHERE Estado = 'Abierto';
            """;
        return command.ExecuteNonQuery();
    }

    public List<UsageRecord> Query(UsageFilter filter)
    {
        using var connection = OpenConnection(readOnly: true);
        using var command = connection.CreateCommand();

        var where = new List<string>();
        if (filter.FechaDesde is not null)
        {
            where.Add("Fecha >= $fechaDesde");
            command.Parameters.AddWithValue("$fechaDesde", filter.FechaDesde.Value.ToString(DateFormat));
        }
        if (filter.FechaHasta is not null)
        {
            where.Add("Fecha <= $fechaHasta");
            command.Parameters.AddWithValue("$fechaHasta", filter.FechaHasta.Value.ToString(DateFormat));
        }
        if (!string.IsNullOrWhiteSpace(filter.Usuario))
        {
            where.Add("Usuario = $usuario");
            command.Parameters.AddWithValue("$usuario", filter.Usuario);
        }
        if (!string.IsNullOrWhiteSpace(filter.Aplicacion))
        {
            where.Add("Aplicacion LIKE $aplicacion");
            command.Parameters.AddWithValue("$aplicacion", $"%{filter.Aplicacion}%");
        }

        var whereClause = where.Count > 0 ? "WHERE " + string.Join(" AND ", where) : string.Empty;
        command.CommandText = $"SELECT * FROM UsoAplicaciones {whereClause} ORDER BY HoraInicio DESC;";

        using var reader = command.ExecuteReader();
        return ReadAll(reader);
    }

    public List<string> GetDistinctUsuarios()
    {
        using var connection = OpenConnection(readOnly: true);
        using var command = connection.CreateCommand();
        command.CommandText = "SELECT DISTINCT Usuario FROM UsoAplicaciones ORDER BY Usuario;";
        using var reader = command.ExecuteReader();
        var list = new List<string>();
        while (reader.Read()) list.Add(reader.GetString(0));
        return list;
    }

    public List<string> GetDistinctAplicaciones()
    {
        using var connection = OpenConnection(readOnly: true);
        using var command = connection.CreateCommand();
        command.CommandText = "SELECT DISTINCT Aplicacion FROM UsoAplicaciones ORDER BY Aplicacion;";
        using var reader = command.ExecuteReader();
        var list = new List<string>();
        while (reader.Read()) list.Add(reader.GetString(0));
        return list;
    }

    private static List<UsageRecord> ReadAll(SqliteDataReader reader)
    {
        var list = new List<UsageRecord>();
        while (reader.Read())
        {
            list.Add(new UsageRecord
            {
                Id = reader.GetInt64(reader.GetOrdinal("Id")),
                Usuario = reader.GetString(reader.GetOrdinal("Usuario")),
                Proceso = reader.GetString(reader.GetOrdinal("Proceso")),
                Aplicacion = reader.GetString(reader.GetOrdinal("Aplicacion")),
                Fecha = DateOnly.ParseExact(reader.GetString(reader.GetOrdinal("Fecha")), DateFormat),
                Inicio = DateTime.ParseExact(reader.GetString(reader.GetOrdinal("HoraInicio")), DateTimeFormat, null),
                Fin = reader.IsDBNull(reader.GetOrdinal("HoraFin"))
                    ? null
                    : DateTime.ParseExact(reader.GetString(reader.GetOrdinal("HoraFin")), DateTimeFormat, null),
                DuracionSegundos = reader.GetInt64(reader.GetOrdinal("DuracionSegundos")),
                Estado = reader.GetString(reader.GetOrdinal("Estado")),
            });
        }
        return list;
    }
}
