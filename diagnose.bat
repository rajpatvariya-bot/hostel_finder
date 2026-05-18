@echo off
REM ========================================
REM Hostel Finder - Diagnostic Test
REM Check if everything is working
REM ========================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

color 0F
cls

echo.
echo =========================================
echo   DIAGNOSTIC CHECK
echo =========================================
echo.

echo [1/5] Checking System Requirements...
echo.

REM Check Java
echo   • Java:
java -version >nul 2>&1
if %errorlevel%==0 (
    echo     ✓ Found
) else (
    echo     ✗ NOT FOUND (Install Java 17+)
)

REM Check Python
echo   • Python:
python --version >nul 2>&1
if %errorlevel%==0 (
    echo     ✓ Found
) else (
    echo     ✗ NOT FOUND (Install from python.org)
)

REM Check MySQL
echo   • MySQL:
netstat -ano | findstr ":3306" >nul 2>&1
if %errorlevel%==0 (
    echo     ✓ Running
) else (
    echo     ⚠ Not detected (Start MySQL service)
)

echo.
echo [2/5] Checking Network Ports...
echo.

REM Check if ports are in use
echo   • Port 3000 (Frontend):
netstat -ano | findstr ":3000" >nul 2>&1
if %errorlevel%==0 (
    echo     ⚠ Already in use
) else (
    echo     ✓ Available
)

echo   • Port 8080 (Backend):
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel%==0 (
    echo     ⚠ Already in use
) else (
    echo     ✓ Available
)

echo.
echo [3/5] Checking Project Structure...
echo.

if exist "backend\mvnw.cmd" (
    echo   ✓ Backend folder OK
) else (
    echo   ✗ Backend folder missing!
)

if exist "frontend\pages\index.html" (
    echo   ✓ Frontend folder OK
) else (
    echo   ✗ Frontend folder missing!
)

echo.
echo [4/5] Checking Database Connection...
echo.

echo   Attempting to connect to MySQL...
REM This will show if MySQL is accessible
if exist "backend\src\main\resources\application.properties" (
    echo   ✓ Config file found
    echo     (Default: localhost:3306, user: root, pass: 1437)
    echo.
    echo   To test connection:
    echo     1. Start MySQL manually
    echo     2. Run: run.bat
    echo     3. Check backend terminal for connection errors
) else (
    echo   ✗ Config file not found
)

echo.
echo [5/5] Recommendations...
echo.

echo   To fix issues:
echo.
echo   1. Backend Port 8080 in use:
echo      • taskkill /F /IM java.exe
echo.
echo   2. Python not found:
echo      • Download from https://python.org
echo      • Add to PATH during installation
echo.
echo   3. MySQL not running:
echo      • Windows: Start MySQL service
echo      • Linux/Mac: brew services start mysql
echo.
echo   4. After fixing, run:
echo      • run.bat
echo.

echo =========================================
echo.
echo   Ready to launch?
echo   Double-click: run.bat
echo.
pause
