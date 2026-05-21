@echo off
setlocal

set "ROOT_DIR=%~dp0.."
set "CONFIG_FILE=%ROOT_DIR%\scripts\config.bat"
set "OUTPUT_DIR=%ROOT_DIR%\docs\deliverables\technical\javadocs"

if exist "%CONFIG_FILE%" call "%CONFIG_FILE%" >nul 2>nul

if "%TOMCAT_HOME%"=="" goto missing_tomcat
if not exist "%TOMCAT_HOME%\lib\servlet-api.jar" goto missing_tomcat

if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%OUTPUT_DIR%"

javadoc -quiet ^
  -encoding UTF-8 ^
  -charset UTF-8 ^
  -docencoding UTF-8 ^
  -Xdoclint:all,-missing ^
  -sourcepath "%ROOT_DIR%\backend\src" ^
  -classpath "%TOMCAT_HOME%\lib\servlet-api.jar" ^
  -d "%OUTPUT_DIR%" ^
  -subpackages com.example.tarecruitment

if errorlevel 1 exit /b 1
echo JavaDoc generated at: %OUTPUT_DIR%\index.html
exit /b 0

:missing_tomcat
echo TOMCAT_HOME must point to a Tomcat install with lib\servlet-api.jar. 1>&2
echo Copy scripts\config.example.bat to scripts\config.bat and set TOMCAT_HOME first. 1>&2
exit /b 1
