namespace AppUsageMonitor.MonitorService;

/// <summary>
/// Logger minimo a un archivo de texto. El monitor corre sin consola
/// (OutputType=WinExe) y como tarea de la sesion del usuario, sin
/// privilegios de administrador, por lo que no puede depender del Visor de
/// eventos de Windows (crear un origen de evento nuevo requiere ser
/// administrador). Este archivo sirve para diagnosticar problemas.
/// </summary>
public sealed class FileLoggerProvider : ILoggerProvider
{
    private readonly string _filePath;
    private readonly object _lock = new();

    public FileLoggerProvider(string filePath)
    {
        _filePath = filePath;
        var directory = Path.GetDirectoryName(filePath);
        if (!string.IsNullOrEmpty(directory))
        {
            Directory.CreateDirectory(directory);
        }
    }

    public ILogger CreateLogger(string categoryName) => new FileLogger(categoryName, _filePath, _lock);

    public void Dispose()
    {
    }

    private sealed class FileLogger : ILogger
    {
        private readonly string _category;
        private readonly string _filePath;
        private readonly object _lock;

        public FileLogger(string category, string filePath, object sharedLock)
        {
            _category = category;
            _filePath = filePath;
            _lock = sharedLock;
        }

        public IDisposable? BeginScope<TState>(TState state) where TState : notnull => null;

        public bool IsEnabled(LogLevel logLevel) => logLevel >= LogLevel.Information;

        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception, Func<TState, Exception?, string> formatter)
        {
            if (!IsEnabled(logLevel)) return;

            var line = $"{DateTime.Now:yyyy-MM-dd HH:mm:ss} [{logLevel}] {_category}: {formatter(state, exception)}";
            if (exception is not null)
            {
                line += Environment.NewLine + exception;
            }

            lock (_lock)
            {
                try
                {
                    File.AppendAllText(_filePath, line + Environment.NewLine, System.Text.Encoding.UTF8);
                }
                catch
                {
                    // Un fallo al escribir el log nunca debe detener el monitoreo.
                }
            }
        }
    }
}
