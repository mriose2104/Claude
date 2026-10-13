@echo off
setlocal
title Iniciar Tailscale

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
        echo [ERROR] No se pudo iniciar el servicio. Ejecuta este archivo como administrador.
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
