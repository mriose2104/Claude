using System.IO;
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

        // Sin esto, cualquier excepcion no manejada cierra toda la app en
        // silencio (incluido el icono de la bandeja) sin dejar ningun
        // rastro de por que. Se registra el detalle en un log y se avisa
        // en pantalla en vez de desaparecer sin explicacion.
        DispatcherUnhandledException += (_, args) =>
        {
            LogCrash(args.Exception, "DispatcherUnhandledException");
            MessageBox.Show(
                $"Ocurrio un error inesperado:\n\n{args.Exception.Message}\n\nEl detalle completo se guardo en el log de diagnostico.",
                "Monitor de Uso de Aplicaciones", MessageBoxButton.OK, MessageBoxImage.Error);
            args.Handled = true;
        };
        AppDomain.CurrentDomain.UnhandledException += (_, args) =>
            LogCrash(args.ExceptionObject as Exception, "AppDomain.UnhandledException");

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

    private static void LogCrash(Exception? ex, string source)
    {
        try
        {
            var dir = Path.Combine(DatabasePathProvider.GetDefaultDirectory(), "logs");
            Directory.CreateDirectory(dir);
            var path = Path.Combine(dir, "dashboard.log");
            File.AppendAllText(path, $"{DateTime.Now:yyyy-MM-dd HH:mm:ss} [{source}]{Environment.NewLine}{ex}{Environment.NewLine}{Environment.NewLine}", System.Text.Encoding.UTF8);
        }
        catch
        {
            // Si ni el log funciona, no hay mucho mas que hacer aqui.
        }
    }
}
