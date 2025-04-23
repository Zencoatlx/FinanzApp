@echo off
TITLE Configuración de Git para Android Studio

echo ===================================
echo Configurando Git para tu proyecto Android
echo ===================================

REM Verificar si git está instalado
where git >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Git no está instalado. Por favor instala Git desde https://git-scm.com/download/win
    pause
    exit /b
)

REM Inicializar Git si no está ya inicializado
if not exist ".git" (
    echo Inicializando repositorio Git...
    git init
    echo Repositorio Git inicializado con éxito.
) else (
    echo El repositorio Git ya está inicializado.
)

REM Verificar si .gitignore existe
if not exist ".gitignore" (
    echo ADVERTENCIA: No se encontró archivo .gitignore. Asegúrate de crearlo primero.
) else (
    echo Archivo .gitignore encontrado.
)

REM Solicitar nombre de usuario y email para la configuración de Git
echo Configurando tu identidad en Git:
set /p GIT_NAME="Introduce tu nombre: "
set /p GIT_EMAIL="Introduce tu email: "

git config user.name "%GIT_NAME%"
git config user.email "%GIT_EMAIL%"
echo Identidad configurada: %GIT_NAME% ^<%GIT_EMAIL%^>

REM Configurar el repositorio remoto
echo Configurando repositorio remoto:
set /p REPO_URL="Introduce la URL de tu repositorio GitHub (ejemplo: https://github.com/usuario/repo.git): "

git remote add origin "%REPO_URL%"
echo Repositorio remoto configurado: %REPO_URL%

REM Realizar primer commit y push
echo Preparando archivos para el primer commit...
git add .

echo Realizando el primer commit...
git commit -m "Commit inicial: Configuración del proyecto"

echo Subiendo cambios a GitHub...
git push -u origin master 2>nul || git push -u origin main

echo ¡Configuración completada con éxito!
echo ===================================
echo Tu proyecto está ahora conectado con GitHub
echo ===================================

pause
