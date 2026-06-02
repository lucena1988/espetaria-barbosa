@echo off
setlocal

cd /d "%~dp0.."

set "BACKUP_DIR=%CD%\backups"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "STAMP=%%i"
set "BACKUP_FILE=%BACKUP_DIR%\espetaria-barbosa-%STAMP%.dump"
set "CONTAINER_BACKUP=/tmp/espetaria-barbosa-%STAMP%.dump"

echo.
echo Gerando backup do banco PostgreSQL...
echo Destino: %BACKUP_FILE%
echo.

docker compose up -d postgres
if errorlevel 1 (
    echo.
    echo Nao foi possivel iniciar o PostgreSQL para backup.
    pause
    exit /b 1
)

docker exec espetaria-barbosa-db pg_dump -U espetaria -d espetaria_barbosa -Fc -f "%CONTAINER_BACKUP%"
if errorlevel 1 (
    echo.
    echo Falha ao gerar backup dentro do container.
    pause
    exit /b 1
)

docker cp "espetaria-barbosa-db:%CONTAINER_BACKUP%" "%BACKUP_FILE%"
if errorlevel 1 (
    echo.
    echo Falha ao copiar o backup para a pasta local.
    pause
    exit /b 1
)

docker exec espetaria-barbosa-db rm -f "%CONTAINER_BACKUP%"

echo.
echo Backup concluido com sucesso.
echo Arquivo: %BACKUP_FILE%
echo.
pause
