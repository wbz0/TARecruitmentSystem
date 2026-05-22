@echo off
REM ========================================
REM Dev Script - Build + Deploy + Start in one
REM ========================================

REM ==== Load config ====
call "%~dp0config.bat"

setlocal

REM ==== CONFIG ====
set PROJECT_ROOT=%~dp0..\
set SRC_DIR=%PROJECT_ROOT%backend\src
set WEBAPP_DIR=%PROJECT_ROOT%frontend\webapp
set BUILD_DIR=%PROJECT_ROOT%build
set TARGET_DIR=%CATALINA_HOME%\webapps\%APP_NAME%
set FRONTEND_DIR=%PROJECT_ROOT%frontend\webapp
if not defined TOMCAT_HTTP_PORT (
    set "HTTP_PORT=8080"
) else (
    set "HTTP_PORT=%TOMCAT_HTTP_PORT%"
)
set "LOGIN_URL=http://localhost:%HTTP_PORT%/%APP_NAME%/login.jsp"
if not defined PORT_RELEASE_TIMEOUT_SECONDS set "PORT_RELEASE_TIMEOUT_SECONDS=10"
if not defined STARTUP_TIMEOUT_SECONDS set "STARTUP_TIMEOUT_SECONDS=120"

echo ========================================
echo   Dev Script - All in One
echo ========================================
echo.

REM ========================================
REM STEP 1: BUILD
REM ========================================

echo [1/3] Building...
echo.

REM Clean old build directory
if exist "%BUILD_DIR%" (
    echo   Cleaning old build files...
    rmdir /S /Q "%BUILD_DIR%"
)

REM Create output directory
if not exist "%BUILD_DIR%\WEB-INF\classes" mkdir "%BUILD_DIR%\WEB-INF\classes"

REM Check Tomcat path
if not exist "%TOMCAT_HOME%" (
    echo [ERROR] Tomcat not found: %TOMCAT_HOME%
    echo Please check config.bat
    exit /b 1
)

set CLASSPATH=%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes

set SOURCE_LIST=%BUILD_DIR%\java-sources.txt
dir /S /B "%SRC_DIR%\*.java" > "%SOURCE_LIST%"

for %%A in ("%SOURCE_LIST%") do if %%~zA EQU 0 (
    echo [ERROR] No Java source files found under %SRC_DIR%
    exit /b 1
)

echo   Compiling Java source files...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%CLASSPATH%" @"%SOURCE_LIST%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compilation failed!
    exit /b 1
)

echo   Copying resource files...
if exist "%WEBAPP_DIR%" (
    xcopy /Y /E "%WEBAPP_DIR%\*" "%BUILD_DIR%\" >nul
)

echo   Build Complete!
echo.

REM ========================================
REM STEP 2: DEPLOY
REM ========================================

echo [2/3] Deploying...
echo.

REM Check build directory
if not exist "%BUILD_DIR%" (
    echo [ERROR] Build directory not found after build step.
    exit /b 1
)

REM Check Tomcat directory
if not exist "%CATALINA_HOME%" (
    echo [ERROR] Tomcat not found: %CATALINA_HOME%
    echo Please check config.bat
    exit /b 1
)

echo   Stopping Tomcat (if running)...
call "%CATALINA_HOME%\bin\shutdown.bat"

call :KillTomcatProcesses

call :WaitForPortRelease
if errorlevel 1 (
    call :KillPortListeners
)

call :WaitForPortRelease
if errorlevel 1 (
    echo [ERROR] Port %HTTP_PORT% is still occupied after kill attempts.
    call :ShowStartupDiagnostics
    exit /b 1
)

echo   Port %HTTP_PORT% is free.

echo   Deploying to Tomcat...

REM Delete old version
if exist "%TARGET_DIR%" (
    echo   Removing old version...
    rmdir /S /Q "%TARGET_DIR%"
)

REM Use robocopy to avoid xcopy wildcard ambiguity
robocopy "%BUILD_DIR%" "%TARGET_DIR%" /E /NFL /NDL /NJH /NJS /NP >nul
if %ERRORLEVEL% GEQ 8 (
    echo [ERROR] Failed to copy build artifacts to Tomcat webapps.
    exit /b 1
)

REM Safety sync for frontend static assets
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

REM Trigger Tomcat context reload
if exist "%TARGET_DIR%\WEB-INF\web.xml" (
    powershell -NoProfile -Command "(Get-Item '%TARGET_DIR%\WEB-INF\web.xml').LastWriteTime = Get-Date" >nul
)

echo   Deploy Complete!
echo.

REM ========================================
REM STEP 3: START
REM ========================================

echo [3/3] Starting Tomcat...
echo.

if not exist "%CATALINA_HOME%" (
    echo [ERROR] Tomcat not found: %CATALINA_HOME%
    echo Please check config.bat
    exit /b 1
)

call "%CATALINA_HOME%\bin\startup.bat"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to execute Tomcat startup.bat
    exit /b 1
)

call :WaitForStartupVerification
if errorlevel 1 (
    echo [ERROR] Tomcat did not pass startup verification.
    call :ShowStartupDiagnostics
    exit /b 1
)

echo.
echo ========================================
echo   All Done!
echo ========================================
echo.
echo Access URLs:
echo   - Home: http://localhost:%HTTP_PORT%/%APP_NAME%/
echo   - Login: %LOGIN_URL%
echo.
echo Tomcat Manager: http://localhost:%HTTP_PORT%/manager/html
echo.

endlocal
pause
exit /b 0

:KillTomcatProcesses
powershell -NoProfile -ExecutionPolicy Bypass -Command "$pids = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*org.apache.catalina.startup.Bootstrap*' } | Select-Object -ExpandProperty ProcessId; if ($pids) { Write-Host ('  Killing old Tomcat process(es): ' + ($pids -join ' ')); $pids | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue } }"
exit /b 0

:KillPortListeners
powershell -NoProfile -ExecutionPolicy Bypass -Command "$pids = Get-NetTCPConnection -LocalPort %HTTP_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; if ($pids) { Write-Host ('  Port %HTTP_PORT% is still occupied. Killing listener process(es): ' + ($pids -join ' ')); $pids | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue } }"
exit /b 0

:WaitForPortRelease
powershell -NoProfile -ExecutionPolicy Bypass -Command "$deadline = (Get-Date).AddSeconds([int]'%PORT_RELEASE_TIMEOUT_SECONDS%'); $stable = 0; while ((Get-Date) -lt $deadline) { $listeners = Get-NetTCPConnection -LocalPort %HTTP_PORT% -State Listen -ErrorAction SilentlyContinue; if (-not $listeners) { $stable++; if ($stable -ge 2) { exit 0 } } else { $stable = 0 }; Start-Sleep -Seconds 1 }; exit 1"
exit /b %ERRORLEVEL%

:WaitForStartupVerification
powershell -NoProfile -ExecutionPolicy Bypass -Command "$deadline = (Get-Date).AddSeconds([int]'%STARTUP_TIMEOUT_SECONDS%'); while ((Get-Date) -lt $deadline) { $listener = Get-NetTCPConnection -LocalPort %HTTP_PORT% -State Listen -ErrorAction SilentlyContinue; if ($listener) { try { $status = (Invoke-WebRequest -Uri '%LOGIN_URL%' -UseBasicParsing -TimeoutSec 5).StatusCode } catch { $status = 'no response' }; if ($status -eq 200) { Write-Host '  Port %HTTP_PORT% is listening.'; Write-Host '  %LOGIN_URL% returned 200.'; exit 0 }; Write-Host ('  Waiting for %LOGIN_URL% to return 200 (current: ' + $status + ')') } else { Write-Host '  Waiting for port %HTTP_PORT% to start listening...' }; Start-Sleep -Seconds 2 }; exit 1"
exit /b %ERRORLEVEL%

:ShowStartupDiagnostics
echo   Expected port: %HTTP_PORT%
echo   Expected URL: %LOGIN_URL%
echo.
powershell -NoProfile -ExecutionPolicy Bypass -Command "$listeners = Get-NetTCPConnection -LocalPort %HTTP_PORT% -State Listen -ErrorAction SilentlyContinue; Write-Host 'Current port listener:'; if ($listeners) { $listeners | Format-Table -AutoSize } else { Write-Host '  No listener on port %HTTP_PORT%' }; $log = Join-Path '%CATALINA_HOME%' 'logs\catalina.out'; if (Test-Path $log) { Write-Host ''; Write-Host 'Last Tomcat log lines:'; Get-Content $log -Tail 80 }"
exit /b 0
