using System.Globalization;
using System.Windows.Data;

namespace AppUsageMonitor.Dashboard.Converters;

/// <summary>Convierte segundos a un texto legible como "2h 27m" o "45m".</summary>
public class SecondsToDurationConverter : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture)
    {
        long totalSeconds = value switch
        {
            long l => l,
            int i => i,
            TimeSpan ts => (long)ts.TotalSeconds,
            _ => 0,
        };

        return FormatDuration(totalSeconds);
    }

    public static string FormatDuration(long totalSeconds)
    {
        if (totalSeconds < 60) return $"{totalSeconds}s";

        var span = TimeSpan.FromSeconds(totalSeconds);
        if (span.TotalHours >= 1)
        {
            return $"{(int)span.TotalHours}h {span.Minutes}m";
        }
        return $"{span.Minutes}m";
    }

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        throw new NotSupportedException();
}
