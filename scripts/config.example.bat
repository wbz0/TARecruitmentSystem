@echo off
REM ========================================
REM CONFIG TEMPLATE - Copy to config.bat and modify
REM ========================================

REM ==== YOUR TOMCAT PATH ====
set CATALINA_HOME=YOUR_TOMCAT_PATH_HERE
set TOMCAT_HOME=%CATALINA_HOME%

REM ==== DATA DIRECTORY ====
REM Sets the data directory path (defaults to Tomcat's data folder)
set TA_HIRING_DATA_DIR=%CATALINA_HOME%\data

REM ==== APP NAME (optional) ====
set APP_NAME=groupproject