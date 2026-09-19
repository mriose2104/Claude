using AppUsageMonitor.Dashboard.Converters;
using AppUsageMonitor.Reports.Models;

namespace AppUsageMonitor.Dashboard.Controls;

public partial class BarChartControl : System.Windows.Controls.UserControl
{
    private const double MaxBarWidth = 260;

    public BarChartControl()
    {
        InitializeComponent();
    }

    public void SetData(IReadOnlyList<AppUsageSummary> items)
    {
        var maxSeconds = items.Count > 0 ? items.Max(i => i.TotalSegundos) : 0;

        var barItems = items.Select(i => new BarChartItem
        {
            Etiqueta = i.Aplicacion,
            Segundos = i.TotalSegundos,
            DuracionTexto = SecondsToDurationConverter.FormatDuration(i.TotalSegundos),
            AnchoBarra = maxSeconds > 0 ? Math.Max(4, i.TotalSegundos / (double)maxSeconds * MaxBarWidth) : 0,
        }).ToList();

        ItemsHost.ItemsSource = barItems;
    }
}
