using System.Globalization;
using System.Text;
using AppUsageMonitor.Database.Models;

namespace AppUsageMonitor.Reports.Exporters;

public static class CsvExporter
{
    public static void Export(IEnumerable<UsageRecord> records, string filePath)
    {
        var sb = new StringBuilder();
        sb.AppendLine("Usuario,Programa,Proceso,Fecha,Inicio,Fin,DuracionMinutos,Estado");

        foreach (var r in records)
        {
            var duracionMinutos = Math.Round(ReportService.DuracionEfectivaSegundos(r) / 60.0, 1);
            sb.AppendLine(string.Join(',',
                Escape(r.Usuario),
                Escape(r.Aplicacion),
                Escape(r.Proceso),
                Escape(r.Fecha.ToString("yyyy-MM-dd")),
                Escape(r.Inicio.ToString("HH:mm:ss")),
                Escape(r.Fin?.ToString("HH:mm:ss") ?? string.Empty),
                duracionMinutos.ToString(CultureInfo.InvariantCulture),
                Escape(r.Estado)));
        }

        File.WriteAllText(filePath, sb.ToString(), new UTF8Encoding(encoderShouldEmitUTF8Identifier: true));
    }

    private static string Escape(string value)
    {
        if (value.Contains(',') || value.Contains('"') || value.Contains('\n'))
        {
            return "\"" + value.Replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
