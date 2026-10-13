@echo off
setlocal
title Detener Tailscale

set "TS=C:\Program Files\Tailscale\tailscale.exe"

if not exist "%TS%" (
    echo [ERROR] No se encontro tailscale.exe en "%TS%".
    echo Verifica que Tailscale este instalado.
    pause
    exit /b 1
)

echo Desconectando Tailscale...
"%TS%" down
if errorlevel 1 (
    echo [ERROR] No se pudo desconectar Tailscale.
    pause
    exit /b 1
)

echo Tailscale desconectado.
pause
