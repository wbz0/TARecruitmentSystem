@echo off
REM ========================================
REM One-key build + deploy + startup (Windows)
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

echo.
echo ========================================
echo   [STEP 1/3] Building...
echo ========================================
echo.

REM Check Tomcat path
if not exist "%TOMCAT_HOME%" (
    echo [ERROR] Tomcat not found: %TOMCAT_HOME%
    echo Please check config.bat
    exit /b 1
)

REM Clean old build directory
if exist "%BUILD_DIR%" (
    echo [1/3] Cleaning old build files...
    rmdir /S /Q "%BUILD_DIR%"
)

REM Create output directory
if not exist "%BUILD_DIR%\WEB-INF\classes" mkdir "%BUILD_DIR%\WEB-INF\classes"

echo [2/3] Compiling Java source files...

REM First pass: compile model and utility classes (no dependencies on external jars)
echo First pass: compiling StoragePaths and models...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\util\StoragePaths.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar" "%SRC_DIR%\com\example\authlogin\model\User.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar" "%SRC_DIR%\com\example\authlogin\model\Applicant.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar" "%SRC_DIR%\com\example\authlogin\model\Job.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar" "%SRC_DIR%\com\example\authlogin\model\Application.java"

REM Second pass: compile DAO classes
echo Second pass: compiling DAO classes...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\dao\UserDao.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\dao\ApplicantDao.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\dao\JobDao.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\dao\ApplicationDao.java"

REM Third pass: compile remaining utility classes
echo Third pass: compiling utility classes...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\util\SessionUtil.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\util\PermissionUtil.java"

REM Fourth pass: compile filter classes
echo Fourth pass: compiling filter classes...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\filter\AuthFilter.java"

REM Fifth pass: compile servlet classes
echo Fifth pass: compiling servlet classes...
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\LoginServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\RegisterServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\LogoutServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\HelloServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\ApplicantServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\JobServlet.java"
javac -encoding UTF-8 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%TOMCAT_HOME%\lib\servlet-api.jar;%BUILD_DIR%\WEB-INF\classes" "%SRC_DIR%\com\example\authlogin\ApplyServlet.java"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b 1
)

echo [3/3] Copying resource files...

REM Copy all frontend webapp resources
if exist "%WEBAPP_DIR%" (
    xcopy /Y /E "%WEBAPP_DIR%\*" "%BUILD_DIR%\" >nul
)

echo.
echo [BUILD] Complete!
echo.

echo.
echo ========================================
echo   [STEP 2/3] Deploying...
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

REM Trigger Tomcat context reload when server keeps running after failed shutdown.
if exist "%TARGET_DIR%\WEB-INF\web.xml" (
    powershell -NoProfile -Command "(Get-Item '%TARGET_DIR%\WEB-INF\web.xml').LastWriteTime = Get-Date" >nul
)

echo.
echo [DEPLOY] Complete!
echo   App path: %TARGET_DIR%
echo.

echo.
echo ========================================
echo   [STEP 3/3] Starting Tomcat...
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
echo   All Done! Tomcat is starting...
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
