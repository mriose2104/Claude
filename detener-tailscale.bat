@echo off
setlocal
title Detener Tailscale

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

echo Desconectando Tailscale...
"%TS%" down
if errorlevel 1 (
    echo [ERROR] No se pudo desconectar Tailscale.
    pause
    exit /b 1
)

echo Deteniendo el servicio de Tailscale...
net stop Tailscale >nul 2>&1
sc query Tailscale | find "STOPPED" >nul
if errorlevel 1 (
    echo [ERROR] No se pudo detener el servicio de Tailscale.
    pause
    exit /b 1
)

echo Tailscale desconectado y servicio detenido.
exit /b 0
