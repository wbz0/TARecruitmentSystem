@echo off
REM ========================================
REM Sheng Yuhan Frontend Test - Windows
REM ========================================

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..
set "FRONTEND_TEST_DIR=%PROJECT_ROOT%\frontend\test"

echo.
echo [Sheng Yuhan] Starting contributor frontend test

where node >nul 2>&1
if errorlevel 1 (
    echo [Sheng Yuhan] FAIL - Missing command: node
    exit /b 1
)

node "%FRONTEND_TEST_DIR%\sheng-yuhan-frontend-test.js"
if errorlevel 1 (
    echo [Sheng Yuhan] FAIL - Contributor test failed
    exit /b 1
)
echo [Sheng Yuhan] PASS - Contributor test passed
