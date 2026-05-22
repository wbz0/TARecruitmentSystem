@echo off
REM ========================================
REM Wang Bangzhen Architecture Test - Windows
REM ========================================

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..
set "FRONTEND_TEST_DIR=%PROJECT_ROOT%\frontend\test"

echo.
echo [Wang Bangzhen] Starting contributor architecture test

where node >nul 2>&1
if errorlevel 1 (
    echo [Wang Bangzhen] FAIL - Missing command: node
    exit /b 1
)

node "%FRONTEND_TEST_DIR%\wang-bangzhen-architecture-test.js"
if errorlevel 1 (
    echo [Wang Bangzhen] FAIL - Contributor test failed
    exit /b 1
)
echo [Wang Bangzhen] PASS - Contributor test passed
