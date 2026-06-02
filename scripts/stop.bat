@echo off
setlocal

cd /d "%~dp0.."

echo.
echo Parando Espetaria Barbosa...
echo.

docker compose down

if errorlevel 1 (
    echo.
    echo Nao foi possivel parar o sistema.
    pause
    exit /b 1
)

echo.
echo Sistema parado.
echo.
pause
