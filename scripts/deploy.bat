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
if exist "%CATALINA_HOME%\webapps\%APP_NAME%" (
    echo Removing old version...
    rmdir /S /Q "%CATALINA_HOME%\webapps\%APP_NAME%"
)

REM Copy new version
xcopy /E /Y "%BUILD_DIR%\*" "%CATALINA_HOME%\webapps\%APP_NAME%\" >nul

echo.
echo ========================================
echo   Deploy Complete!
echo   App path: %CATALINA_HOME%\webapps\%APP_NAME%
echo ========================================
echo.

endlocal
pause
