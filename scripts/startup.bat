@echo off
REM ========================================
REM Start Tomcat Script
REM ========================================

REM ==== Load config ====
call "%~dp0config.bat"

setlocal

echo ========================================
echo   Start Tomcat
echo ========================================
echo.

if not exist "%CATALINA_HOME%" (
    echo [ERROR] Tomcat not found: %CATALINA_HOME%
    echo Please check config.bat
    exit /b 1
)

echo Starting Tomcat...
echo.

REM Start Tomcat
call "%CATALINA_HOME%\bin\startup.bat"

echo.
echo ========================================
echo   Tomcat Started!
echo ========================================
echo.
echo Access URLs:
echo   - Home: http://localhost:8080/%APP_NAME%/
echo   - JSP:  http://localhost:8080/%APP_NAME%/jsp/welcome.jsp
echo   - Servlet: http://localhost:8080/%APP_NAME%/hello
echo.
echo Tomcat Manager: http://localhost:8080/manager/html
echo.

endlocal
pause
