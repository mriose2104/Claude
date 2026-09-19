namespace AppUsageMonitor.MonitorService;

public class MonitoringOptions
{
    public int PollIntervalSeconds { get; set; } = 5;

    public string[] ExcludedProcessNames { get; set; } = Array.Empty<string>();
}
