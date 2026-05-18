@echo off
REM ========================================
REM Check System Requirements
REM ========================================

setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ========================================
echo   Checking System Requirements
echo ========================================
echo.

REM Check Java
echo [1/3] Checking Java...
java -version >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| find "java version"') do set java_version=%%i
    echo       ✓ Java found
) else (
    echo       ✗ Java NOT found! Please install Java 17+
    echo       Download from: https://oracle.com/java
    pause
    exit /b 1
)

REM Check MySQL
echo [2/3] Checking MySQL...
netstat -ano | findstr ":3306" >nul
if %errorlevel%==0 (
    echo       ✓ MySQL is running (port 3306)
) else (
    echo       ⚠ MySQL might not be running
    echo       Make sure MySQL service is started
    echo.
)

REM Check Python
echo [3/3] Checking Python...
python --version >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=*" %%i in ('python --version 2^>^&1') do set python_version=%%i
    echo       ✓ !python_version! found
) else (
    echo       ✗ Python NOT found!
    echo       Download from: https://python.org
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   All Requirements Met!
echo   Starting application...
echo ========================================
echo.

REM Run the main launcher
call run.bat
