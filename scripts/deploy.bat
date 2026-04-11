@echo off
REM ========================================
REM Deploy Script - Deploy to Tomcat
REM ========================================

REM ==== Load config ====
call "%~dp0config.bat"

setlocal

REM ==== CONFIG ====
set PROJECT_ROOT=%~dp0..\
set BUILD_DIR=%PROJECT_ROOT%build
set TARGET_DIR=%CATALINA_HOME%\webapps\%APP_NAME%
set FRONTEND_DIR=%PROJECT_ROOT%frontend\webapp

echo ========================================
echo   Deploy Script
echo ========================================
echo.

REM Check build directory
if not exist "%BUILD_DIR%" (
    echo [ERROR] Build directory not found. Run build.bat first.
    exit /b 1
)

REM Check Tomcat directory
if not exist "%CATALINA_HOME%" (
    echo [ERROR] Tomcat not found: %CATALINA_HOME%
    echo Please check config.bat
    exit /b 1
)

echo Stopping Tomcat (if running)...
call "%CATALINA_HOME%\bin\shutdown.bat"

timeout /t 2 /nobreak >nul

echo Deploying to Tomcat...

REM Delete old version
if exist "%TARGET_DIR%" (
    echo Removing old version...
    rmdir /S /Q "%TARGET_DIR%"
)

REM Use robocopy to avoid xcopy wildcard ambiguity on first-time target creation.
robocopy "%BUILD_DIR%" "%TARGET_DIR%" /E /NFL /NDL /NJH /NJS /NP >nul
if %ERRORLEVEL% GEQ 8 (
    echo [ERROR] Failed to copy build artifacts to Tomcat webapps.
    exit /b 1
)

REM Safety sync for frontend static assets to prevent css/js missing after deploy.
if exist "%FRONTEND_DIR%\css" (
    robocopy "%FRONTEND_DIR%\css" "%TARGET_DIR%\css" /E /NFL /NDL /NJH /NJS /NP >nul
    if %ERRORLEVEL% GEQ 8 (
        echo [ERROR] Failed to sync frontend css assets.
        exit /b 1
    )
)

if exist "%FRONTEND_DIR%\js" (
    robocopy "%FRONTEND_DIR%\js" "%TARGET_DIR%\js" /E /NFL /NDL /NJH /NJS /NP >nul
    if %ERRORLEVEL% GEQ 8 (
        echo [ERROR] Failed to sync frontend js assets.
        exit /b 1
    )
)

REM Trigger Tomcat context reload when server keeps running after failed shutdown.
if exist "%TARGET_DIR%\WEB-INF\web.xml" (
    powershell -NoProfile -Command "(Get-Item '%TARGET_DIR%\WEB-INF\web.xml').LastWriteTime = Get-Date" >nul
)

echo.
echo ========================================
echo   Deploy Complete!
echo   App path: %TARGET_DIR%
echo ========================================
echo.

endlocal
pause
