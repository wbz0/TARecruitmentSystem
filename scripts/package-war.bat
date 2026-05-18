@echo off
setlocal

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven ^(mvn^) not found. Please install Maven 3.9+ first.
    exit /b 1
)

echo ========================================
echo   Maven WAR Package Script
echo ========================================
echo.

pushd "%PROJECT_ROOT%"
mvn -DskipTests clean package
if %ERRORLEVEL% NEQ 0 (
    popd
    exit /b 1
)
popd

echo.
echo ========================================
echo  WAR package generated successfully
echo  Output: %PROJECT_ROOT%target\groupproject.war
echo ========================================
endlocal
