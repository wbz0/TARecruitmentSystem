@echo off
REM ========================================
REM Zhou Bohan Backend Test - Windows
REM ========================================

REM Load config first to get TOMCAT_HOME
call "%~dp0..\config.bat"

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..
set "BUILD_DIR=%PROJECT_ROOT%\build\contributor-tests\zhou-bohan"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "TEST_CLASSES_DIR=%BUILD_DIR%\test-classes"
set "SERVLET_JAR=%TOMCAT_HOME%\lib\servlet-api.jar"
set "SRC_DIR=%PROJECT_ROOT%\backend\src"
set "TEST_SRC=%PROJECT_ROOT%\backend\test\ZhouBohanBackendTest.java"

echo.
echo [Zhou Bohan] Starting contributor backend test

if not exist "%SERVLET_JAR%" (
    echo [Zhou Bohan] FAIL - Cannot find servlet-api.jar at %SERVLET_JAR%
    echo Please check TOMCAT_HOME in config.bat
    exit /b 1
)

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%CLASSES_DIR%"
mkdir "%TEST_CLASSES_DIR%"

echo.
echo [Zhou Bohan] Compiling backend sources
powershell -NoProfile -Command "Get-ChildItem -Path '%SRC_DIR%' -Filter '*.java' -Recurse -File | Select-Object -ExpandProperty FullName" > "%BUILD_DIR%\sources.txt"

javac -encoding UTF-8 -d "%CLASSES_DIR%" -cp "%SERVLET_JAR%;%CLASSES_DIR%" "@%BUILD_DIR%\sources.txt"
if errorlevel 1 (
    echo [Zhou Bohan] FAIL - Backend source compilation failed
    exit /b 1
)
echo [Zhou Bohan] PASS - Backend source compilation passed

echo.
echo [Zhou Bohan] Compiling contributor test code
javac -encoding UTF-8 -d "%TEST_CLASSES_DIR%" -cp "%SERVLET_JAR%;%CLASSES_DIR%" -sourcepath "%SRC_DIR%" "%TEST_SRC%"
if errorlevel 1 (
    echo [Zhou Bohan] FAIL - Contributor test code compilation failed
    exit /b 1
)
echo [Zhou Bohan] PASS - Contributor test code compilation passed

set "DATA_DIR=%BUILD_DIR%\data"
if exist "%DATA_DIR%" rmdir /s /q "%DATA_DIR%"
mkdir "%DATA_DIR%"

echo.
echo [Zhou Bohan] Running contributor backend test
set "TA_HIRING_DATA_DIR=%DATA_DIR%"
java -cp "%SERVLET_JAR%;%CLASSES_DIR%;%TEST_CLASSES_DIR%" ZhouBohanBackendTest
if errorlevel 1 (
    echo [Zhou Bohan] FAIL - Contributor backend test failed
    exit /b 1
)
echo [Zhou Bohan] PASS - Contributor backend test passed
