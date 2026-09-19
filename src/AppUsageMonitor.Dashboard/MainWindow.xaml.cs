using System.Windows;
using AppUsageMonitor.Database;
using AppUsageMonitor.Database.Models;
using AppUsageMonitor.Dashboard.Converters;
using AppUsageMonitor.Reports;
using AppUsageMonitor.Reports.Exporters;
using MessageBox = System.Windows.MessageBox;
using SaveFileDialog = Microsoft.Win32.SaveFileDialog;

namespace AppUsageMonitor.Dashboard;

public partial class MainWindow : Window
{
    private readonly ReportService _reportService;
    private List<UsageRecord> _ultimoResultado = new();

    public MainWindow(ReportService reportService)
    {
        InitializeComponent();
        _reportService = reportService;
        RutaBaseDatosText.Text = $"Base de datos local: {DatabasePathProvider.GetDefaultPath()}";

        var hoy = DateOnly.FromDateTime(DateTime.Now);
        FechaDesdePicker.SelectedDate = hoy.ToDateTime(TimeOnly.MinValue);
        FechaHastaPicker.SelectedDate = hoy.ToDateTime(TimeOnly.MinValue);

        RefreshData();
    }

    /// <summary>Recarga el panel principal, los combos de filtro y vuelve a ejecutar la ultima busqueda.</summary>
    public void RefreshData()
    {
        CargarPanelPrincipal();
        CargarCombos();
        EjecutarBusqueda(ConstruirFiltroActual());
    }

    private void CargarPanelPrincipal()
    {
        var hoy = DateOnly.FromDateTime(DateTime.Now);
        var resumen = _reportService.GetResumenDiario(hoy);

        TiempoTotalHoyText.Text = SecondsToDurationConverter.FormatDuration(resumen.TotalSegundosUso);
        AplicacionesHoyText.Text = resumen.AplicacionesDistintas.ToString();
        SesionesHoyText.Text = resumen.SesionesTotales.ToString();
        UltimaActividadText.Text = resumen.UltimaActividad?.ToString("HH:mm") ?? "--:--";

        var abiertos = _reportService.GetProgramasAbiertos();
        AbiertosAhoraText.Text = abiertos.Count.ToString();
        AbiertosListView.ItemsSource = abiertos.Select(r => new
        {
            Programa = r.Aplicacion,
            Usuario = r.Usuario,
            Inicio = r.Inicio.ToString("HH:mm"),
            Duracion = SecondsToDurationConverter.FormatDuration(ReportService.DuracionEfectivaSegundos(r)),
        }).ToList();

        var topHoy = _reportService.GetTopAplicaciones(new UsageFilter { FechaDesde = hoy, FechaHasta = hoy }, top: 8);
        TopAppsChart.SetData(topHoy);
        SinDatosHoyText.Visibility = topHoy.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
    }

    private void CargarCombos()
    {
        var usuarioSeleccionado = UsuarioCombo.SelectedItem as string;
        var programaSeleccionado = ProgramaCombo.SelectedItem as string;

        UsuarioCombo.ItemsSource = new[] { string.Empty }.Concat(_reportService.GetUsuarios()).ToList();
        ProgramaCombo.ItemsSource = new[] { string.Empty }.Concat(_reportService.GetAplicaciones()).ToList();

        UsuarioCombo.SelectedItem = usuarioSeleccionado ?? string.Empty;
        ProgramaCombo.SelectedItem = programaSeleccionado ?? string.Empty;
    }

    private UsageFilter ConstruirFiltroActual()
    {
        return new UsageFilter
        {
            FechaDesde = FechaDesdePicker.SelectedDate is { } desde ? DateOnly.FromDateTime(desde) : null,
            FechaHasta = FechaHastaPicker.SelectedDate is { } hasta ? DateOnly.FromDateTime(hasta) : null,
            Usuario = string.IsNullOrWhiteSpace(UsuarioCombo.SelectedItem as string) ? null : (string)UsuarioCombo.SelectedItem,
            Aplicacion = string.IsNullOrWhiteSpace(ProgramaCombo.SelectedItem as string) ? null : (string)ProgramaCombo.SelectedItem,
        };
    }

    private void EjecutarBusqueda(UsageFilter filtro)
    {
        _ultimoResultado = _reportService.GetHistorial(filtro).ToList();

        HistorialGrid.ItemsSource = _ultimoResultado.Select(r => new HistorialRow
        {
            Usuario = r.Usuario,
            Programa = r.Aplicacion,
            Proceso = r.Proceso,
            Fecha = r.Fecha.ToString("yyyy-MM-dd"),
            Inicio = r.Inicio.ToString("HH:mm"),
            Fin = r.Fin?.ToString("HH:mm") ?? "-",
            Duracion = SecondsToDurationConverter.FormatDuration(ReportService.DuracionEfectivaSegundos(r)),
            Estado = r.Estado,
        }).ToList();

        var totalSegundos = _ultimoResultado.Sum(ReportService.DuracionEfectivaSegundos);
        TotalFiltradoText.Text = $"Total: {_ultimoResultado.Count} sesiones, {SecondsToDurationConverter.FormatDuration(totalSegundos)}";
    }

    private void RefreshButton_Click(object sender, RoutedEventArgs e) => RefreshData();

    private void SetRango(DateOnly? desde, DateOnly? hasta)
    {
        FechaDesdePicker.SelectedDate = desde?.ToDateTime(TimeOnly.MinValue);
        FechaHastaPicker.SelectedDate = hasta?.ToDateTime(TimeOnly.MinValue);
        EjecutarBusqueda(ConstruirFiltroActual());
    }

    private void FiltroHoy_Click(object sender, RoutedEventArgs e)
    {
        var f = UsageFilter.Hoy();
        SetRango(f.FechaDesde, f.FechaHasta);
    }

    private void FiltroAyer_Click(object sender, RoutedEventArgs e)
    {
        var f = UsageFilter.Ayer();
        SetRango(f.FechaDesde, f.FechaHasta);
    }

    private void FiltroSemana_Click(object sender, RoutedEventArgs e)
    {
        var f = UsageFilter.EstaSemana();
        SetRango(f.FechaDesde, f.FechaHasta);
    }

    private void FiltroMes_Click(object sender, RoutedEventArgs e)
    {
        var f = UsageFilter.EsteMes();
        SetRango(f.FechaDesde, f.FechaHasta);
    }

    private void Buscar_Click(object sender, RoutedEventArgs e) => EjecutarBusqueda(ConstruirFiltroActual());

    private void LimpiarFiltros_Click(object sender, RoutedEventArgs e)
    {
        FechaDesdePicker.SelectedDate = null;
        FechaHastaPicker.SelectedDate = null;
        UsuarioCombo.SelectedItem = string.Empty;
        ProgramaCombo.SelectedItem = string.Empty;
        EjecutarBusqueda(new UsageFilter());
    }

    private void ExportarCsv_Click(object sender, RoutedEventArgs e)
    {
        if (!ValidarHayDatosParaExportar()) return;

        var dialog = new SaveFileDialog
        {
            Filter = "Archivo CSV (*.csv)|*.csv",
            FileName = $"uso_aplicaciones_{DateTime.Now:yyyyMMdd_HHmm}.csv",
        };
        if (dialog.ShowDialog() != true) return;

        try
        {
            CsvExporter.Export(_ultimoResultado, dialog.FileName);
            MessageBox.Show("Reporte exportado correctamente.", "Exportar CSV", MessageBoxButton.OK, MessageBoxImage.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"No se pudo exportar: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    private void ExportarExcel_Click(object sender, RoutedEventArgs e)
    {
        if (!ValidarHayDatosParaExportar()) return;

        var dialog = new SaveFileDialog
        {
            Filter = "Libro de Excel (*.xlsx)|*.xlsx",
            FileName = $"uso_aplicaciones_{DateTime.Now:yyyyMMdd_HHmm}.xlsx",
        };
        if (dialog.ShowDialog() != true) return;

        try
        {
            ExcelExporter.Export(_ultimoResultado, dialog.FileName);
            MessageBox.Show("Reporte exportado correctamente.", "Exportar Excel", MessageBoxButton.OK, MessageBoxImage.Information);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"No se pudo exportar: {ex.Message}", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    private bool ValidarHayDatosParaExportar()
    {
        if (_ultimoResultado.Count == 0)
        {
            MessageBox.Show("No hay datos para exportar con los filtros actuales.", "Exportar", MessageBoxButton.OK, MessageBoxImage.Warning);
            return false;
        }
        return true;
    }
}
