@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>&1
if errorlevel 1 (
    echo No se encontro Java. Instala Java 21 o superior y agregalo a PATH.
    pause
    exit /b 1
)
if not exist "target\jocote-0.1.0-SNAPSHOT.jar" (
    where mvn >nul 2>&1
    if errorlevel 1 (
        echo No se encontro Maven. Instala Maven 3.9 o compila primero con mvn clean package.
        pause
        exit /b 1
    )
    call mvn -B -ntp package
    if errorlevel 1 (
        echo No fue posible compilar Jocote. Revisa el error anterior.
        pause
        exit /b 1
    )
)
java -jar "target\jocote-0.1.0-SNAPSHOT.jar" %*
if errorlevel 1 (
    echo Jocote termino con un error. Revisa el mensaje anterior.
    pause
    exit /b 1
)
