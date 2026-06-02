@echo off
setlocal

cd /d "%~dp0.."

echo.
echo Iniciando Espetaria Barbosa...
echo.

docker compose up -d

if errorlevel 1 (
    echo.
    echo Nao foi possivel iniciar o sistema.
    echo Verifique se o Docker Desktop esta aberto e tente novamente.
    pause
    exit /b 1
)

echo.
echo Sistema iniciado.
echo Acesse no computador principal:
echo http://localhost:8080
echo.
echo Para acessar de outros dispositivos, use o IP deste computador na rede local.
echo Exemplo: http://192.168.0.10:8080
echo.
pause
