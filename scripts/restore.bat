@echo off
setlocal

cd /d "%~dp0.."

set "BACKUP_DIR=%CD%\backups"

echo.
echo Restauracao de backup - Espetaria Barbosa
echo.

if not exist "%BACKUP_DIR%" (
    echo Pasta de backups nao encontrada:
    echo %BACKUP_DIR%
    pause
    exit /b 1
)

echo Backups disponiveis:
echo.
dir /b /o-d "%BACKUP_DIR%\*.dump"
echo.

set /p "BACKUP_NAME=Digite o nome do arquivo .dump para restaurar: "
set "BACKUP_FILE=%BACKUP_DIR%\%BACKUP_NAME%"

if not exist "%BACKUP_FILE%" (
    echo.
    echo Arquivo nao encontrado:
    echo %BACKUP_FILE%
    pause
    exit /b 1
)

echo.
echo ATENCAO: a restauracao substitui os dados atuais do banco.
set /p "CONFIRMA=Digite RESTAURAR para confirmar: "

if not "%CONFIRMA%"=="RESTAURAR" (
    echo.
    echo Restauracao cancelada.
    pause
    exit /b 0
)

set "CONTAINER_BACKUP=/tmp/%BACKUP_NAME%"

echo.
echo Parando a aplicacao para restaurar o banco...
docker compose stop app

echo.
echo Garantindo PostgreSQL em execucao...
docker compose up -d postgres
if errorlevel 1 (
    echo.
    echo Nao foi possivel iniciar o PostgreSQL.
    pause
    exit /b 1
)

echo.
echo Copiando backup para o container...
docker cp "%BACKUP_FILE%" "espetaria-barbosa-db:%CONTAINER_BACKUP%"
if errorlevel 1 (
    echo.
    echo Falha ao copiar o backup para o container.
    pause
    exit /b 1
)

echo.
echo Restaurando backup...
docker exec espetaria-barbosa-db pg_restore -U espetaria -d espetaria_barbosa --clean --if-exists --no-owner "%CONTAINER_BACKUP%"
if errorlevel 1 (
    echo.
    echo Falha ao restaurar backup.
    pause
    exit /b 1
)

docker exec espetaria-barbosa-db rm -f "%CONTAINER_BACKUP%"

echo.
echo Reiniciando aplicacao...
docker compose up -d app

echo.
echo Restauracao concluida com sucesso.
echo.
pause
