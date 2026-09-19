using AppUsageMonitor.Database.Models;
using ClosedXML.Excel;

namespace AppUsageMonitor.Reports.Exporters;

public static class ExcelExporter
{
    public static void Export(IEnumerable<UsageRecord> records, string filePath)
    {
        using var workbook = new XLWorkbook();
        var sheet = workbook.Worksheets.Add("Uso de Aplicaciones");

        string[] headers = { "Usuario", "Programa", "Proceso", "Fecha", "Inicio", "Fin", "Duracion (min)", "Estado" };
        for (var i = 0; i < headers.Length; i++)
        {
            sheet.Cell(1, i + 1).Value = headers[i];
            sheet.Cell(1, i + 1).Style.Font.Bold = true;
        }

        var row = 2;
        foreach (var r in records)
        {
            var duracionMinutos = Math.Round(ReportService.DuracionEfectivaSegundos(r) / 60.0, 1);
            sheet.Cell(row, 1).Value = r.Usuario;
            sheet.Cell(row, 2).Value = r.Aplicacion;
            sheet.Cell(row, 3).Value = r.Proceso;
            sheet.Cell(row, 4).Value = r.Fecha.ToString("yyyy-MM-dd");
            sheet.Cell(row, 5).Value = r.Inicio.ToString("HH:mm:ss");
            sheet.Cell(row, 6).Value = r.Fin?.ToString("HH:mm:ss") ?? string.Empty;
            sheet.Cell(row, 7).Value = duracionMinutos;
            sheet.Cell(row, 8).Value = r.Estado;
            row++;
        }

        sheet.Columns().AdjustToContents();
        workbook.SaveAs(filePath);
    }
}
