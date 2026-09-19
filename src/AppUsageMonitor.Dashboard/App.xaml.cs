using System.Windows;
using AppUsageMonitor.Database;
using AppUsageMonitor.Reports;
using Application = System.Windows.Application;
using MessageBox = System.Windows.MessageBox;

namespace AppUsageMonitor.Dashboard;

public partial class App : Application
{
    private System.Windows.Forms.NotifyIcon? _trayIcon;
    private MainWindow? _mainWindow;
    private Mutex? _singleInstanceMutex;

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        _singleInstanceMutex = new Mutex(true, "AppUsageMonitor.Dashboard.SingleInstance", out var isNew);
        if (!isNew)
        {
            MessageBox.Show("El panel de Monitor de Uso de Aplicaciones ya esta abierto (revisa el area de notificaciones).",
                "Monitor de Uso de Aplicaciones", MessageBoxButton.OK, MessageBoxImage.Information);
            Shutdown();
            return;
        }

        var dbPath = DatabasePathProvider.GetDefaultPath();
        DatabaseInitializer.EnsureCreated(dbPath);
        var repository = new UsageRepository(dbPath);
        var reportService = new ReportService(repository);

        _mainWindow = new MainWindow(reportService);
        _mainWindow.Closing += (_, args) =>
        {
            // En vez de cerrar la app, la minimizamos a la bandeja.
            args.Cancel = true;
            _mainWindow.Hide();
        };

        SetupTrayIcon();

        var startMinimized = e.Args.Contains("--minimized", StringComparer.OrdinalIgnoreCase);
        if (!startMinimized)
        {
            _mainWindow.Show();
        }
    }

    private void SetupTrayIcon()
    {
        _trayIcon = new System.Windows.Forms.NotifyIcon
        {
            Icon = new System.Drawing.Icon("Resources/app.ico"),
            Visible = true,
            Text = "Monitor de Uso de Aplicaciones",
        };

        var menu = new System.Windows.Forms.ContextMenuStrip();
        menu.Items.Add("Abrir panel", null, (_, _) => ShowMainWindow());
        menu.Items.Add(new System.Windows.Forms.ToolStripSeparator());
        menu.Items.Add("Salir", null, (_, _) => ExitApplication());
        _trayIcon.ContextMenuStrip = menu;

        _trayIcon.DoubleClick += (_, _) => ShowMainWindow();
    }

    private void ShowMainWindow()
    {
        if (_mainWindow is null) return;
        _mainWindow.Show();
        _mainWindow.WindowState = WindowState.Normal;
        _mainWindow.Activate();
        _mainWindow.RefreshData();
    }

    private void ExitApplication()
    {
        if (_trayIcon is not null)
        {
            _trayIcon.Visible = false;
            _trayIcon.Dispose();
        }
        _singleInstanceMutex?.ReleaseMutex();
        Shutdown();
    }
}
