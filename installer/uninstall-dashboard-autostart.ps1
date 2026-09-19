<#
.SYNOPSIS
    Elimina el acceso directo de autoarranque del Dashboard creado por
    install-dashboard-autostart.ps1.
#>
$startupFolder = [Environment]::GetFolderPath("Startup")
$shortcutPath = Join-Path $startupFolder "Monitor de Uso de Aplicaciones.lnk"

if (Test-Path $shortcutPath) {
    Remove-Item $shortcutPath -Force
    Write-Host "Acceso directo de autoarranque eliminado." -ForegroundColor Green
} else {
    Write-Host "No habia ningun acceso directo de autoarranque instalado." -ForegroundColor Yellow
}
