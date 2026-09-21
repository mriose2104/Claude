using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using System.Security.Principal;

namespace AppUsageMonitor.MonitorService;

/// <summary>
/// Interop minimo con Win32 para averiguar a que usuario de Windows
/// pertenece un proceso. No se lee ni se registra ninguna otra
/// informacion del proceso (ni ventanas, ni contenido, ni teclado).
/// </summary>
[SupportedOSPlatform("windows")]
internal static class NativeMethods
{
    private const int ProcessQueryLimitedInformation = 0x1000;
    private const int TokenQuery = 0x0008;

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern IntPtr OpenProcess(int dwDesiredAccess, bool bInheritHandle, int dwProcessId);

    [DllImport("kernel32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool CloseHandle(IntPtr hObject);

    [DllImport("advapi32.dll", SetLastError = true)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool OpenProcessToken(IntPtr processHandle, int desiredAccess, out IntPtr tokenHandle);

    [DllImport("user32.dll")]
    private static extern IntPtr GetForegroundWindow();

    [DllImport("user32.dll", SetLastError = true)]
    private static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint lpdwProcessId);

    /// <summary>
    /// Id del proceso duenio de la ventana actualmente en primer plano (la
    /// que tiene el foco), o null si no hay ninguna (por ejemplo el
    /// escritorio). Solo se usa para saber que aplicacion esta "en uso"
    /// activo en este instante; no se lee el titulo ni el contenido de esa
    /// ventana.
    /// </summary>
    public static int? GetForegroundProcessId()
    {
        var hwnd = GetForegroundWindow();
        if (hwnd == IntPtr.Zero) return null;

        GetWindowThreadProcessId(hwnd, out var pid);
        return pid == 0 ? null : (int)pid;
    }

    /// <summary>
    /// Devuelve el nombre de usuario (sin dominio) dueño del proceso, o
    /// null si no se pudo determinar (por ejemplo procesos protegidos del
    /// sistema, para los cuales no aplica de todos modos el monitoreo).
    /// </summary>
    public static string? GetProcessOwner(int processId)
    {
        var processHandle = IntPtr.Zero;
        var tokenHandle = IntPtr.Zero;
        try
        {
            processHandle = OpenProcess(ProcessQueryLimitedInformation, false, processId);
            if (processHandle == IntPtr.Zero) return null;

            if (!OpenProcessToken(processHandle, TokenQuery, out tokenHandle)) return null;

            using var identity = new WindowsIdentity(tokenHandle);
            var name = identity.Name;
            if (string.IsNullOrEmpty(name)) return null;

            var separatorIndex = name.IndexOf('\\');
            return separatorIndex >= 0 ? name[(separatorIndex + 1)..] : name;
        }
        catch
        {
            return null;
        }
        finally
        {
            if (tokenHandle != IntPtr.Zero) CloseHandle(tokenHandle);
            if (processHandle != IntPtr.Zero) CloseHandle(processHandle);
        }
    }
}
