@echo off
setlocal
title Iniciar Tailscale

rem Pide permisos de administrador si no los tiene (necesarios para el servicio)
net session >nul 2>&1
if errorlevel 1 (
    echo Solicitando permisos de administrador...
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

set "TS=C:\Program Files\Tailscale\tailscale.exe"

if not exist "%TS%" (
    echo [ERROR] No se encontro tailscale.exe en "%TS%".
    echo Verifica que Tailscale este instalado.
    pause
    exit /b 1
)

rem Asegura que el servicio de Tailscale este corriendo
sc query Tailscale | find "RUNNING" >nul
if errorlevel 1 (
    echo Iniciando el servicio de Tailscale...
    net start Tailscale >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] No se pudo iniciar el servicio de Tailscale.
        pause
        exit /b 1
    )
)

echo Conectando Tailscale...
"%TS%" up
if errorlevel 1 (
    echo [ERROR] No se pudo conectar Tailscale.
    pause
    exit /b 1
)

echo.
"%TS%" status
echo.
echo Tailscale conectado.
pause
