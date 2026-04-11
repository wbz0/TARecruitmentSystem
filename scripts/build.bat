@echo off
REM ========================================
REM Build Script - Compile Java Servlet
REM ========================================

REM ==== Load config ====
call "%~dp0config.bat"

setlocal

REM ==== CONFIG ====
set PROJECT_ROOT=%~dp0..\
set SRC_DIR=%PROJECT_ROOT%backend\src
set WEBAPP_DIR=%PROJECT_ROOT%frontend\webapp
set BUILD_DIR=%PROJECT_ROOT%build

echo ========================================
echo   Servlet/JSP Build Script
echo ========================================
echo.

REM Clean old build directory
if exist "%BUILD_DIR%" (
    echo [1/3] Cleaning old build files...
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

echo [2/3] Compiling Java source files...

REM Compile all .java files in subdirectories
REM First pass: compile model and utility classes (no dependencies on external jars)
echo First pass: compiling model and utility classes...
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

REM Copy all frontend webapp resources (JSP/HTML/CSS/JS/images/module folders)
if exist "%WEBAPP_DIR%" (
    xcopy /Y /E "%WEBAPP_DIR%\*" "%BUILD_DIR%\" >nul
)

echo.
echo ========================================
echo   Build Complete!
echo   Output: %BUILD_DIR%
echo ========================================
echo.
echo Next steps:
echo   1. Run deploy.bat to deploy to Tomcat
echo   2. Run startup.bat to start Tomcat
echo.

endlocal
pause
